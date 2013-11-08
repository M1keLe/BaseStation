package it.basestation.cmdline;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public class DataProcessor extends Thread {
	
	
	private Hashtable<Short, LastPeriodNodeRecord> lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();

	@Override
	public void run(){

		while (true) {
			
			Hashtable<Short, LastPeriodNodeRecord> newNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
			
			try {
				System.out.println("Data Processor in esecuzione " + new Date());
				
				// imposto la frequenza di update
				Thread.sleep(Configurator.getFreqDataProcessor());
				
				// Switch puntatori liste e prendo la lista di nodi da elaborare
				LinkedList <Packet> packetsList = LocalStatsManager.getLastPeriodPacketsList();
				
				// se la lista non è vuota
				if(!packetsList.isEmpty()){
					for (Packet p : packetsList) {
						short nodeID = p.getSenderID();
						System.out.println("DATAPROCESSOR STA GESTENDO UN PACCHETTO SPEDITO DAL NODO N° " +nodeID);
						if(!newNodesRecord.containsKey(nodeID)){
							newNodesRecord.put(nodeID, new LastPeriodNodeRecord(nodeID));
						}
						newNodesRecord.get(nodeID).addPacket(p);
					}
					
					
					
					// store dei dati sulle fusion tables
					Enumeration<Short> e = newNodesRecord.keys();
					LinkedList<Capability> listForGlobal = new LinkedList<Capability>();
					while(e.hasMoreElements()){
						short nodeID = e.nextElement();
						LastPeriodNodeRecord recordToStore = newNodesRecord.get(nodeID);
						
						listForGlobal.addAll(recordToStore.getCapListToStore());
						
						//LinkedList<Capability> toStore = newNodesRecord.get(nodeID).getCapListToStore();
						try {
							FusionTablesManager.insertData(recordToStore);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					
					// store dei valori globali
					Hashtable<String, LastPeriodGlobalRecord> globalRecords = new Hashtable<String, LastPeriodGlobalRecord>();
					for(String s  : Configurator.getGlobalCapabilitiesSet()){
						globalRecords.put(s, new LastPeriodGlobalRecord(Configurator.getCapability(s), listForGlobal));
						//FusionTablesManager.insertData(globalRecords.get(s));
					}
					
					this.lastPeriodNodesRecord = newNodesRecord;
					
					
					
					
					
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
				