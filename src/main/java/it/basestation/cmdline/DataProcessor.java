package it.basestation.cmdline;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public class DataProcessor extends Thread {
	
	
	//private Hashtable<Short, LastPeriodNodeRecord> lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
	//private LastPeriodGlobalRecord lastPeriodGlobalRecord = new LastPeriodGlobalRecord();
	private LinkedList <Packet> packetsList = new LinkedList<Packet>();
	private LastPeriodGlobalRecord lastGlobalRecord = null;
		
	public DataProcessor(){
		super("Data Processor");
	}
	
	@Override
	public void run(){

		while (true) {
			
			Hashtable<Short, LastPeriodNodeRecord> newNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
			//Hashtable<Short, RebootFixer> lastRecordedValues = new Hashtable<Short, RebootFixer>();
			try {
				System.out.println("Data Processor in esecuzione " + new Date());
				
				// imposto la frequenza di update
				Thread.sleep(Configurator.getFreqDataProcessor());
				
				// Switch puntatori liste e prendo la lista di nodi da elaborare
				this.packetsList = LocalStatsManager.getLastPeriodPacketsList();
				
				// se la lista non è vuota
				if(!packetsList.isEmpty()){
					for (Packet p : packetsList) {
						short nodeID = p.getSenderID();
						//System.out.println("DATA_PROCESSOR STA GESTENDO UN PACCHETTO SPEDITO DAL NODO N° " +nodeID);
						if(!newNodesRecord.containsKey(nodeID)){
							newNodesRecord.put(nodeID, new LastPeriodNodeRecord(nodeID));
						}
						newNodesRecord.get(nodeID).addPacket(p); 	
					}					
					
					// store dei dati locali sulle fusion tables 
					Enumeration<Short> e = newNodesRecord.keys();
					while(e.hasMoreElements()){
						short nodeID = e.nextElement();
						LastPeriodNodeRecord recordToStore = newNodesRecord.get(nodeID);
						// DEBUG
						//System.out.println(recordToStore);
						
						try {
							
							FusionTablesManager.insertData(recordToStore);
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
 						
					}
					// calcolo le medie globali
					
					LinkedList<String> globalCapabilitiesSet = Configurator.getGlobalCapabilitiesSet();
					LastPeriodGlobalRecord newGlobalRecord = new LastPeriodGlobalRecord();
					for (String name : globalCapabilitiesSet) {
						// prendo i dati dai vari node records
						Enumeration<Short> nodeID = newNodesRecord.keys();
						while (nodeID.hasMoreElements()) {
							CapabilityInstance cI = newNodesRecord.get(nodeID.nextElement()).getCapabilityInstance(name);
							if(cI != null && (cI.globalOperator().contains("avg") || 
									cI.globalOperator().contains("sum") || 
									cI.globalOperator().contains("last"))) {
								
								newGlobalRecord.addCapabilityInstance(cI);
							}
						}
					}
					
					// Store capabilities Globali
					// update global stats
					newGlobalRecord = updateGlobalRecord(newGlobalRecord, this.lastGlobalRecord);
					
					//System.out.println(newGlobalRecord);
					try {
						FusionTablesManager.insertData(newGlobalRecord);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					this.lastGlobalRecord = newGlobalRecord;
					
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
		if(lastGlobalRecord != null){
			LinkedList<CapabilityInstance> lastGlobalValues = lastGlobalRecord.getDataListToStore();
			for (CapabilityInstance cI : lastGlobalValues) {
				if(cI.globalOperator().contains("avg") || cI.globalOperator().contains("sum") || cI.globalOperator().contains("last")){ 
				//if(cI.getName().equals("PeopleIn")|| cI.getName().equals("PeopleOut")){
					newGlobalRecord.addCapabilityInstance(cI);
				}
			}
		//}else{
		//	for (CapabilityInstance cI : newGlobalRecord.getDataListToStore()) {
		//		if(cI.getName().contains("People")){
		//			cI.setValue(0);
		//		}
		//	}
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
				