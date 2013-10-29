package it.basestation.cmdline;

import java.util.Date;
import java.util.LinkedList;

public class DataProcessor extends Thread {
	
	// Factory Method
	private LinkedList<IStats> lastPeriodStatsElaborator = new LinkedList<IStats>();
	

	public void run(){
		
		// Factory Method
		inizializeElaborator();
		
		while (true) {
			try {
				System.out.println("Data Processor in esecuzione " + new Date());
				Thread.sleep(Configurator.getFreqDataProcessor());
				
				// Switch puntatori liste e prendo la lista di nodi da elaborare
				LinkedList <Packet> newPacketsList = LocalStatsManager.getLastPeriodPacketsList();
				
				// se la lista non è vuota
				if(!newPacketsList.isEmpty()){
					// passare newPacketsList al last period node stats e last period global stats
					for (IStats elaborator : lastPeriodStatsElaborator) {
						elaborator.elabLastPeriodPacketList(newPacketsList);
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
	
	// Factory Method
	private void inizializeElaborator(){
		IStatsCreator[] lastPeriodStatsElaboratorCreator = new  IStatsCreator[2];
		lastPeriodStatsElaboratorCreator[0] = new LastPeriodNodeStatsCreator();
		lastPeriodStatsElaboratorCreator[1] = new LastPeriodGlobalStatsCreator();
		
		for (int i = 0; i < lastPeriodStatsElaboratorCreator.length; i++) {
			this.lastPeriodStatsElaborator.add(lastPeriodStatsElaboratorCreator[i].factoryMethod());
			System.out.println("Creato gestore medie numero " + i);
		}
	}

}

				// ***************************************************************************************************
				// alternativa suddividere la lista pacchetti in hashtable con chiave id sender
				// sbagliato xchè x le stats globali non è necessario tenere i pacchetti suddivisi per senderid
				
/*				
				Hashtable<Short, LinkedList<Packet>> packetsOfNode = new Hashtable<Short, LinkedList<Packet>>();
				
				// se lista non vuota inserisco i pacchetti suddivisi per nodo nella hashtable
				if(!newPacketsList.isEmpty()){
					Iterator<Packet> i = newPacketsList.iterator();
					while (i.hasNext()) {
						Packet p = (Packet) i.next();
						short sender = p.getSenderID();
						if(!packetsOfNode.containsKey(sender)){
							packetsOfNode.put(sender, new LinkedList<Packet>());
						}
						packetsOfNode.get(sender).add(p);
					}
					
					// passare packetsOfNode al last period lode stats e last period global stats
					
					
				}else{
					System.out.println("Nessun pacchetto da gestire");
				}
*/				// ***************************************************************************************************
				
				/* 	 COMPITI DEL THREAD:
				 * - scrivere log su file
				 * - fare medie su ultimo periodo locali
				 * - calcolare grandezze globali
				 * - aggiornare le fusion talbles
				 */
				