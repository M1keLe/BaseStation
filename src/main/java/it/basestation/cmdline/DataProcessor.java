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
	
	// frequenza resetter
	private static final long DAY = 1000*60*60*24;
	
	// frequenza data processor
	private int freqUpdate;
	private Hashtable<Short, LastPeriodNodeRecord> lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
	
	// per aggiornare people in caso di riavvio basestation e people inside < 0
	private LastPeriodGlobalRecord debugGlobalRecord = null;
	
	private Hashtable<Short, PeopleCounter> peopleCounters = new Hashtable<Short, PeopleCounter>();
	private Hashtable<Short, LocalMMCalculator> localMMCalculators = new Hashtable<Short, LocalMMCalculator>();
	// non utilizzato
	private GlobalMMCalculator GlobalMMCalculator = new GlobalMMCalculator();
	
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
				if(this.debugGlobalRecord == null){
					try {
						this.debugGlobalRecord = FusionTablesManager.initGlobalRecord();
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
					
					// calcolo delle medie mobili e
					// store dei dati locali sulle fusion tables solo per i nuovi dati raccolti 
					Enumeration<Short> e = newNodesRecord.keys();
					while(e.hasMoreElements()){
						short nodeID = e.nextElement();
						if(!this.localMMCalculators.containsKey(nodeID))
							this.localMMCalculators.put(nodeID, new LocalMMCalculator(nodeID));
						this.localMMCalculators.get(nodeID).setListToCalculate(newNodesRecord.get(nodeID).getDataListToStore());
						newNodesRecord.get(nodeID).setMMListToStore(this.localMMCalculators.get(nodeID).getMMListToStore());
						// DEBUG
						System.out.println(newNodesRecord.get(nodeID));
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
					
					// LinkedList<String> globalCapabilitiesSet = Configurator.getGlobalCapabilitiesSet();
					LinkedList<Capability> globalCapabilitieslist = Configurator.getGlobalCapabilitiesList(false);
					
					LastPeriodGlobalRecord newGlobalRecord = new LastPeriodGlobalRecord();
					// per ogni capability "globale" prendo i dati da trattare dai vari node record
					for (Capability c : globalCapabilitieslist) {
						if(!c.getIndex().equals("formula")){
							// prendo i dati dai vari node records
							Enumeration<Short> nodeID = this.lastPeriodNodesRecord.keys();
							while (nodeID.hasMoreElements()) {
								CapabilityInstance cI = this.lastPeriodNodesRecord.get(nodeID.nextElement()).getCapabilityInstance(c.getName(), c.getIndex());
								// se cI == null il nodo non ha la capability globale
								if(cI != null){
									CapabilityInstance gCI = new CapabilityInstance(c.getName(), c.getColumnName(), c.getIndex(), c.localOperator(), c.globalOperator(), c.getMinValue(), c.getMaxValue(), c.getAvgWindow());
									gCI.setValue(cI.getValue());
									newGlobalRecord.addCapabilityInstance(gCI);
								}
							}
						}
					}
					
					// aggiornamento people
					newGlobalRecord = updateGlobalRecord(newGlobalRecord, this.debugGlobalRecord);
					
					// calcolo medie mobili (per ora non funziona)
					this.GlobalMMCalculator.setListToCalculate(newGlobalRecord.getDataListToStore());
					newGlobalRecord.setMMListToStore(this.GlobalMMCalculator.getMMListToStore());
					
					// Store capabilities Globali
					// debug
					System.out.println(newGlobalRecord);					
					
					try {
						FusionTablesManager.insertData(newGlobalRecord, updateTime);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
										
					
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
			this.debugGlobalRecord = new LastPeriodGlobalRecord();
			this.lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
			this.peopleCounters = new Hashtable<Short, PeopleCounter>();
			this.localMMCalculators = new Hashtable<Short, LocalMMCalculator>();
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
		} // end foreach
		
		// se people inside < 0
		double peopleInside = newGlobalRecord.getPeopleInsideValue();
		if(peopleInside < 0){
			//System.out.println("DEBUG: ============= People Inside < 0!!!! " + peopleInside );
			CapabilityInstance peopleIn = new CapabilityInstance("PeopleIn","PeopleIn_sum","last","","sum",0.00,Double.POSITIVE_INFINITY,0);
			peopleIn.setValue(Math.abs(peopleInside));
			newGlobalRecord.addCapabilityInstance(peopleIn);
			this.debugGlobalRecord.addCapabilityInstance(peopleIn);
		}			

		return newGlobalRecord;
	}
	
	// thread resetter
	private class Resetter extends TimerTask{
		
		@Override
		public void run(){
			System.out.println("Resetter in esecuzione");
			LocalStatsManager.printNodesLog();
			LocalStatsManager.resetAllStats();
			resetStats();	
		}
	}
}