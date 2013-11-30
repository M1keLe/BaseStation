package it.basestation.cmdline;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public class DataProcessor extends Thread {
	
	private static final long DAY = 1000*60*60*24;
	
	private int freqUpdate;
	private Hashtable<Short, LastPeriodNodeRecord> lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
	private LastPeriodGlobalRecord lastPeriodGlobalRecord = null; // per aggiornare people in caso di riavvio basestation
	
	private Hashtable<Short, PeopleCounter> peopleCounters = new Hashtable<Short, PeopleCounter>();
	
	private ReentrantLock lock = new ReentrantLock();
	
		
	public DataProcessor(){
		super("Data Processor");
		this.freqUpdate = Configurator.getFreqDataProcessor();
	}
	
	@Override
	public void run(){
		Date updateTime;		
		// se specificato nel file di configurazione avvio il thread resetter
		if(Configurator.getResetTime() != null){
			Timer resetter = new Timer("Resetter");
			resetter.schedule(new Resetter(), Configurator.getResetTime(), DAY);
			System.out.println("Primo Reset: " + Configurator.getResetTime());
		}else{
			System.out.println("Le statistiche non verranno mai resettate");
		}
		
		while (true) {
			// imposto la frequenza di update
			try {
				Thread.sleep(this.freqUpdate);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// lock			
			this.lock.lock();
			try {
				// update time
				updateTime = new Date();
				System.out.println("Data Processor in esecuzione " + updateTime);
				// inizializzo il global record
				if(this.lastPeriodGlobalRecord == null){
					try {
						this.lastPeriodGlobalRecord = FusionTablesManager.initGlobalRecord();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// Prendo la lista di nodi da elaborare
				LinkedList <Packet> packetsList = LocalStatsManager.getLastPeriodPacketsList();
				
				// se la lista non è vuota
				if(!packetsList.isEmpty()){
					// nuovi record da elaborare
					Hashtable<Short, LastPeriodNodeRecord> newNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
					for (Packet p : packetsList) {
						short nodeID = p.getSenderID();
						//System.out.println("DATA_PROCESSOR STA GESTENDO UN PACCHETTO SPEDITO DAL NODO N° " +nodeID);
						if(!newNodesRecord.containsKey(nodeID)){
							newNodesRecord.put(nodeID, new LastPeriodNodeRecord(nodeID));
						}
						
// metodo add capability						
						// ciclo for su capabilities
						LinkedList<CapabilityInstance> cList = p.getDataList();
						for (CapabilityInstance cI : cList) {
							// controllo su min e max value
							if(cI.getMinValue() <= cI.getValue() && cI.getValue() <= cI.getMaxValue() ){
								//controllo su people
								if(cI.getName().contains("People")){
									if(!this.peopleCounters.containsKey(nodeID)){
										this.peopleCounters.put(nodeID, new PeopleCounter());
									}
									// aggiorno contatore people
									this.peopleCounters.get(nodeID).elabCapabilityInstance(cI);									
								}
								// inserisco capability nel node record
								newNodesRecord.get(nodeID).addCapabilityInstance(cI);	
							}
						}
						
// end metodo add capability
						
						// metodo addPacket()
						// inserisco il pacchetto da gestire nel node record
						// newNodesRecord.get(nodeID).addPacket(p); 	
					}					
					
					// store dei dati locali sulle fusion tables solo per i nuovi dati raccolti 
					Enumeration<Short> e = newNodesRecord.keys();
					while(e.hasMoreElements()){
						short nodeID = e.nextElement();
						// DEBUG
						// System.out.println(newNodesRecord.get(nodeID));
						try {							
							FusionTablesManager.insertData(newNodesRecord.get(nodeID), updateTime);
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						// aggiorno record nodi
						this.lastPeriodNodesRecord.put(nodeID, newNodesRecord.get(nodeID));
					}
					
					// calcolo le medie globali
					
					LinkedList<String> globalCapabilitiesSet = Configurator.getGlobalCapabilitiesSet();
					LastPeriodGlobalRecord newGlobalRecord = new LastPeriodGlobalRecord();
					// per ogni capability "globale" prendo i dati da trattare da i vari node record
					for (String name : globalCapabilitiesSet) {
						// prendo i dati dai vari node records
						Enumeration<Short> nodeID = this.lastPeriodNodesRecord.keys();
						while (nodeID.hasMoreElements()) {
							CapabilityInstance cI = this.lastPeriodNodesRecord.get(nodeID.nextElement()).getCapabilityInstance(name);
							// se cI == null il nodo non ha la capability globale
							if(cI != null && (cI.globalOperator().contains("avg") || 
									cI.globalOperator().contains("sum") || 
									cI.globalOperator().contains("last"))) {
								
								newGlobalRecord.addCapabilityInstance(cI);
							}
						}
					}
					
					// aggiornamento people
					newGlobalRecord = updateGlobalRecord(newGlobalRecord, this.lastPeriodGlobalRecord);					
					// Store capabilities Globali
					// debug
					System.out.println(newGlobalRecord);
					for (String string : globalCapabilitiesSet) {
						if(Configurator.getCapabilityInstance(string).globalOperator().equals("avg")){
							System.out.println("Sigma_"+string+": " + newGlobalRecord.getStandardDeviation(string));
						}
					}
					
					
					try {
						FusionTablesManager.insertData(newGlobalRecord, updateTime);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
										
					//this.lastPeriodGlobalRecord = newGlobalRecord;
					
				}else{
					System.out.println("Nessun pacchetto da gestire");
				}
				
			}finally{
				this.lock.unlock();
			}
		}	
	}
	
	// metodo invocato dal resetter
	private void resetStats(){
		this.lock.lock();
		try{
			this.lastPeriodGlobalRecord = new LastPeriodGlobalRecord();
			this.lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
			this.peopleCounters = new Hashtable<Short, PeopleCounter>();
			System.out.println("Data Processor: statistiche resettate!");
		}finally{
			this.lock.unlock();
		}
		
	}


	private LastPeriodGlobalRecord updateGlobalRecord(LastPeriodGlobalRecord newGlobalRecord, LastPeriodGlobalRecord lastGlobalRecord) {
		
		LinkedList<CapabilityInstance> lastGlobalValues = lastGlobalRecord.getDataListToStore();
		for (CapabilityInstance cI : lastGlobalValues) {
			 
			if(cI.getName().equals("PeopleIn")|| cI.getName().equals("PeopleOut")){
				newGlobalRecord.addCapabilityInstance(cI);
			}
		}
		return newGlobalRecord;
	}
	
	// thread resetter
	private class Resetter extends TimerTask{
		
		@Override
		public void run(){
			System.out.println("Resetter in esecuzione");
			LocalStatsManager.resetAllStats();
			resetStats();	
		}
	}
}
				// ***************************************************************************************************
				
				/* 	 COMPITI DEL THREAD:
				 * - scrivere log su file
				 * - fare medie su ultimo periodo locali
				 * - calcolare grandezze globali
				 * - aggiornare le fusion talbles
				 */
				