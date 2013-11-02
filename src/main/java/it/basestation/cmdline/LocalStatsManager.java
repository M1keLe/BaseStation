package it.basestation.cmdline;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class LocalStatsManager {
	// lista di nodi
	private static Hashtable<Short, Node> nodeList = new Hashtable<Short, Node>();
	// oppure
	// private static Hashtable<Short, Node> nodeList = Configurator.getNodeList();
	
	// lista pacchetti
	private static LinkedList<Packet> packetsList = new LinkedList<Packet>();
	
	// liste "ping pong" ultimo periodo
	private static LinkedList<Packet> pushPacketList = new LinkedList<Packet>();
	private static LinkedList<Packet> pullPacketList = new LinkedList<Packet>();
	
	private static ReentrantLock lock = new ReentrantLock();
	
	// metodo invocato dal thread serial reader
	public static void addNewPacket(Packet p){		
		lock.lock();	
		// se il pacchetto proviene da un nodo conosciuto lo aggiungo alle varie liste
		// e aggiorno i contatori routedPackets dei nodi "router"
		if(nodeList.containsKey(p.getSenderID())){
			updatePacketLists(p);
			updateRouterCounter(p);
			// inserisco il paccketto nella lista push
			pushPacketList.add(p);
			
			
		}else{
			// altrimenti lo scarto
			System.out.println("Il pacchetto non appartiene alla lista di nodi conosciuti");
			System.out.println("Controllare il file di configurazione");
		}
		lock.unlock();
	}
	
	// metodi invocato dal thread DataProcessor
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
		System.out.println("Statistiche resettate");		
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
	
	// ------------------------- metodi privati
	
	private static void updatePacketLists(Packet packet){
		// inserisco il pacchetto nella lista di pacchetti
		packetsList.add(packet);
		// aggiorno la lista mypackets del nodo
		nodeList.get(packet.getSenderID()).addMyPacket(packet);
	}
	
	// aggiornamento contatore routedPacket
	private static void updateRouterCounter(Packet packet){
		LinkedList<Short> routers = packet.getHopsIndexes();
		for (Short nodeID : routers) {
			nodeList.get(nodeID).increaseRoutedPackets();
		}
	}

}
