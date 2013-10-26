package it.basestation.cmdline;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class LocalStatsContainer {
	private static Hashtable<Short, Node> nodeList = new Hashtable<Short, Node>();
	private static LinkedList<Packet> packetsList = new LinkedList<Packet>();
	
	private static LinkedList<Packet> pushPacketList = new LinkedList<Packet>();
	private static LinkedList<Packet> pullPacketList = new LinkedList<Packet>();
	
	private static ReentrantLock lock = new ReentrantLock();
	
	public static void addNewPacket(Packet p){
		
		// nota: HO SOLO AGGIUNTO I PACCHETTI ALLE VARIE LISTE --------------------------------NOTA
		// se il pacchetto proviene da un nodo conosciuto lo aggiungo alle varie liste
		if(nodeList.containsKey(p.getSenderID())){
			lock.lock();
			
			packetsList.add(p);
			// aggiorno la lista mypackets del nodo
			Node n = nodeList.get(p.getSenderID());
			n.addMyPacket(p);
			// inserisco nella lista nodi il nodo aggiornato
			nodeList.put(n.getMyID(), n);
			lock.unlock();
		}else{
			// altrimenti lo scarto
			System.out.println("Il pacchetto non appartiene alla lista di nodi conosciuti");
			System.out.println("Controllare il file di configurazione");
		}
		
	}
	
	public static void setNodeList(Hashtable<Short, Node> listOfNodes){
		nodeList = listOfNodes;
	}

}
