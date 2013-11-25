package it.basestation.cmdline;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public class DataProcessor extends Thread {
	
	
	private Hashtable<Short, LastPeriodNodeRecord> lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
	private LastPeriodGlobalRecord lastPeriodGlobalRecord = null;
	private LinkedList <Packet> packetsList = new LinkedList<Packet>();
	
		
	public DataProcessor(){
		super("Data Processor");
	}
	
	@Override
	public void run(){
		Date updateTime;
		while (true) {
			
			// nuovi record da elaborare
			Hashtable<Short, LastPeriodNodeRecord> newNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
			// update time
			updateTime = new Date();
			try {
				System.out.println("Data Processor in esecuzione " + new Date());
				
				// imposto la frequenza di update
				Thread.sleep(Configurator.getFreqDataProcessor());

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
				this.packetsList = LocalStatsManager.getLastPeriodPacketsList();
				
				// se la lista non è vuota
				if(!this.packetsList.isEmpty()){
					for (Packet p : this.packetsList) {
						short nodeID = p.getSenderID();
						//System.out.println("DATA_PROCESSOR STA GESTENDO UN PACCHETTO SPEDITO DAL NODO N° " +nodeID);
						if(!newNodesRecord.containsKey(nodeID)){
							newNodesRecord.put(nodeID, new LastPeriodNodeRecord(nodeID));
						}
						// inserisco il pacchetto da gestire nel node record
						newNodesRecord.get(nodeID).addPacket(p); 	
					}					
					
					// store dei dati locali sulle fusion tables solo per i nuovi dati raccolti 
					Enumeration<Short> e = newNodesRecord.keys();
					while(e.hasMoreElements()){
						short nodeID = e.nextElement();

						// DEBUG
						System.out.println(newNodesRecord.get(nodeID));
						//TestWriter.write(newNodesRecord.get(nodeID));
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
					// TestWriter.write(newGlobalRecord);
					try {
						FusionTablesManager.insertData(newGlobalRecord, updateTime);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					
					this.lastPeriodGlobalRecord = newGlobalRecord;
					
				}else{
					System.out.println("Nessun pacchetto da gestire");
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}	
	}	


	private LastPeriodGlobalRecord updateGlobalRecord(LastPeriodGlobalRecord newGlobalRecord, LastPeriodGlobalRecord lastGlobalRecord) {
		
		LinkedList<CapabilityInstance> lastGlobalValues = lastGlobalRecord.getDataListToStore();
		for (CapabilityInstance cI : lastGlobalValues) {
			//if(cI.globalOperator().contains("avg") || cI.globalOperator().contains("sum") || cI.globalOperator().contains("last")){ 
			if(cI.getName().equals("PeopleIn")|| cI.getName().equals("PeopleOut")){
				newGlobalRecord.addCapabilityInstance(cI);
			}
		}
		return newGlobalRecord;
	}
}
				// ***************************************************************************************************
				
				/* 	 COMPITI DEL THREAD:
				 * - scrivere log su file
				 * - fare medie su ultimo periodo locali
				 * - calcolare grandezze globali
				 * - aggiornare le fusion talbles
				 */
				