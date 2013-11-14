package it.basestation.cmdline;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public class DataProcessor extends Thread {
	
	
	//private Hashtable<Short, LastPeriodNodeRecord> lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
	//private Hashtable<String, LastPeriodGlobalRecord> lastPeriodGlobalRecord = new Hashtable<String, LastPeriodGlobalRecord>();
	private LinkedList <Packet> packetsList = new LinkedList<Packet>();
	
	private Hashtable<Short, RebootFixer> lastRecordedValues = new Hashtable<Short, RebootFixer>();
	
	// costruttore che inizializza le hashtables?
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
						//newNodesRecord.get(nodeID).addPacket(p); metodo senza "fix del reboot"
						
						// fix people in e out
						LinkedList<DataContainer> dCList = p.getDataList();
						for (DataContainer dC : dCList) {
							// se devo controllare gli ultimi valori registrati...
							if(dC.needFixReboot()){
								if(!lastRecordedValues.containsKey(nodeID)){
									lastRecordedValues.put(nodeID, new RebootFixer(nodeID));
								}
								//System.out.println("[DEBUG nodeID_"+ nodeID +"_"+dC.getName()+"]-> Valore prima del fix: " + dC.getValue());
								//double newValue = lastRecordedValues.get(nodeID).fixReboot(dC);
								lastRecordedValues.get(nodeID).fixReboot(dC);
								//dC.setValue(newValue);
								//System.out.println("[DEBUG nodeID_"+ nodeID +"_"+dC.getName()+"]-> Valore dopo il fix: " + dC.getValue());
							}
							newNodesRecord.get(nodeID).addDataContainer(dC);
						}
						// end fix in e out
						
					}
					
					
					
					// store dei dati sulle fusion tables
					
					LinkedList<DataContainer> globalList = new LinkedList<DataContainer>();
					Enumeration<Short> e = newNodesRecord.keys();
					while(e.hasMoreElements()){
						short nodeID = e.nextElement();
						LastPeriodNodeRecord recordToStore = newNodesRecord.get(nodeID);
						// Debug
						//System.out.println(recordToStore);
						globalList.addAll(recordToStore.getDataListToStore());
//						for (DataContainer c : recordToStore.getDataListToStore()) {
//							listForGlobal.add(c);
//						}
						
						
						// DEBUG
/*						System.out.println("================= ELENCO CAPABILITY DA SALVARE RISPETTIVE AL NODO NUMERO " + recordToStore.getNodeID());
						for (Capability c : recordToStore.getDataListToStore()) {
							System.out.println(c.toString());
						}
						System.out.println("================= FINE ELENCO CAPABILITY DA SALVARE RISPETTIVE AL NODO NUMERO " + recordToStore.getNodeID());
*/									

						
						try {
							FusionTablesManager.insertData(recordToStore);
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
 						
					}
	
					// store dei valori globali
				Hashtable<String, LastPeriodGlobalRecord> newGlobalRecords = new Hashtable<String, LastPeriodGlobalRecord>();
					for(String s  : Configurator.getGlobalCapabilitiesSet()){
						newGlobalRecords.put(s, new LastPeriodGlobalRecord(s, globalList));
						//newGlobalRecords.put(s, new LastPeriodGlobalRecord(Configurator.getCapability(s), listForGlobal));
						//FusionTablesManager.insertData(globalRecords.get(s));
						
						System.out.println("+++++++++++++++++++++ Global_record +++++++++++++++++++++");
						System.out.println(newGlobalRecords.get(s).getCapabilityToStore().toString());
						System.out.println("++++++++++++++++++++ End_Global_record ++++++++++++++++++");
					}
					
					//this.lastPeriodNodesRecord = newNodesRecord;
					//this.lastPeriodGlobalRecord = newGlobalRecords;
					
					
					
					
				}else{
					System.out.println("Nessun pacchetto da gestire");
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
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
				