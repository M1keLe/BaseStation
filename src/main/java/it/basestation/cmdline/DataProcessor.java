package it.basestation.cmdline;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public class DataProcessor extends Thread {
	
	
	private Hashtable<Short, LastPeriodNodeRecord> lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
	//private LastPeriodGlobalRecord lastPeriodGlobalRecord = new LastPeriodGlobalRecord();
	private LinkedList <Packet> packetsList = new LinkedList<Packet>();
	//private LastPeriodGlobalRecord lastGlobalRecord = null;
		
	public DataProcessor(){
		super("Data Processor");
	}
	
	@Override
	public void run(){

		while (true) {
			
			// nuovi record da elaborare
			Hashtable<Short, LastPeriodNodeRecord> newNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
			
			try {
				System.out.println("Data Processor in esecuzione " + new Date());
				
				// imposto la frequenza di update
				Thread.sleep(Configurator.getFreqDataProcessor());
				
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
					
					// store dei dati locali sulle fusion tables solo per i nuovi dati reccolti 
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
					// aggiorno record nodi
					Enumeration<Short> nodesID = newNodesRecord.keys();
					while (nodesID.hasMoreElements()) {
						Short nodeID = (Short) nodesID.nextElement();
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
					
					// Store capabilities Globali
					
					//System.out.println(newGlobalRecord);
					try {
						FusionTablesManager.insertData(newGlobalRecord);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}else{
					System.out.println("Nessun pacchetto da gestire");
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}	
	}	

	// metodo non utilizzato
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
				