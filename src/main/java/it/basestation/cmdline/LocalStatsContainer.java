package it.basestation.cmdline;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class LocalStatsContainer {
	private static Hashtable<Short, Node> nodeList = new Hashtable<Short, Node>();
	// oppure
	// private static Hashtable<Short, Node> nodeList = Configurator.getNodeList();
	private static LinkedList<Packet> packetsList = new LinkedList<Packet>();
	
	private static LinkedList<Packet> pushPacketList = new LinkedList<Packet>();
	private static LinkedList<Packet> pullPacketList = new LinkedList<Packet>();
	
	private static ReentrantLock lock = new ReentrantLock();
	
	// metodo invocato dal thread serial reader
	public static void addNewPacket(Packet p){
		
		lock.lock();
		// nota: HO SOLO AGGIUNTO I PACCHETTI ALLE VARIE LISTE --------------------------------NOTA
		// manca l'aggiornamento dei contatori locali (routed packets etc)
		// se il pacchetto proviene da un nodo conosciuto lo aggiungo alle varie liste
		if(nodeList.containsKey(p.getSenderID())){
			packetsList.add(p);
			// aggiorno la lista mypackets del nodo
			Node n = nodeList.get(p.getSenderID());
			n.addMyPacket(p);
			// inserisco nella lista nodi il nodo aggiornato
			nodeList.put(n.getMyID(), n);
			
		}else{
			// altrimenti lo scarto
			System.out.println("Il pacchetto non appartiene alla lista di nodi conosciuti");
			System.out.println("Controllare il file di configurazione");
		}
		lock.unlock();
	}
	
	// metodo invocato dal thread DataProcessor
	public static LinkedList<Packet> getLastPeriodPacketsList(){
		lock.lock();
		pullPacketList = pushPacketList;
		pushPacketList.clear();
		lock.unlock();
		return pullPacketList;
	}
	
	// metodo invocato dal thread Resetter
	
	public static void resetAllStats(){
		lock.lock();
		nodeList = Configurator.getNodeList();
		packetsList.clear();
		
		// anche queste?
		pushPacketList.clear();
		pullPacketList.clear();
		
		// altri da inserire ...
		lock.unlock();
		
	}
	
	public static void setNodeList(){
		lock.lock();
		nodeList = Configurator.getNodeList();
		lock.unlock();
	}
	
	public static void setNodeList(Hashtable<Short, Node> listOfNodes){
		lock.lock();
		nodeList = listOfNodes;
		lock.unlock();
	}

}
