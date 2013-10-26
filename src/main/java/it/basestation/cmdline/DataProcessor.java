package it.basestation.cmdline;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class DataProcessor extends Thread {
	private LinkedList <Packet> lastPeriodPacketsList = new LinkedList <Packet>();
	private Hashtable<Short,Hashtable<String,LinkedList<Capability>>> nodeCapabilities = new Hashtable<Short,Hashtable<String,LinkedList<Capability>>>();

	public void run(){
		while (true) {
			try {
				System.out.println("Data Processor in esecuzione" + new Date());
				Thread.sleep(Configurator.getFreqDataProcessor());
				
				// Switch puntatori liste
				LinkedList <Packet> newPacketsList = LocalStatsContainer.getLastPeriodPacketsList();
				
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
					
					
				}else{
					System.out.println("Nessun pacchetto da gestire");
				}
				
				
				/*
				 * - scrivere log su file
				 * - fare medie su ultimo periodo locali
				 * - calcolare grandezze globali
				 * - aggiornare le fusion talbles
				 */
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
