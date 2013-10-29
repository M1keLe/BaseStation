package it.basestation.cmdline;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class LocalStatsManager {
	private static Hashtable<Short, Node> nodeList = new Hashtable<Short, Node>();
	// oppure
	// private static Hashtable<Short, Node> nodeList = Configurator.getNodeList();
	private static LinkedList<Packet> packetsList = new LinkedList<Packet>();
	// ultimi aggiornamenti effettuati sulle fusion Tables
	private static Hashtable<Short, LinkedList<Capability>> lastStoredData = new Hashtable<Short, LinkedList<Capability>>();
	// ultimi valori sommabili e globali
	private static LinkedList<Capability> lastSummableAndGlobalCapabilities = new LinkedList<Capability>(); 
	
	// liste "ping pong"
	private static LinkedList<Packet> pushPacketList = new LinkedList<Packet>();
	private static LinkedList<Packet> pullPacketList = new LinkedList<Packet>();
	
	private static ReentrantLock lock = new ReentrantLock();
	
	// metodo invocato dal thread serial reader
	public static void addNewPacket(Packet p){
		
		lock.lock();
		
		// se il pacchetto proviene da un nodo conosciuto lo aggiungo alle varie liste
		// e aggiorno i contatori routedPackets dei nodi "router"
		if(nodeList.containsKey(p.getSenderID())){
			updateNodeStats(p);
			updateRouterCounter(p);
			
			
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
	
	public static LinkedList<Capability> getLastSummableAndGlobalCapabilities(){
		return lastSummableAndGlobalCapabilities;
	}
	
	public static void setLastSummableAndGlobalCapabilities(LinkedList<Capability> lastPeriodSumAndGlobCapabilities){
		lock.lock();
		lastSummableAndGlobalCapabilities = lastPeriodSumAndGlobCapabilities;
		lock.unlock();
	}
	
	public static Node getNode(Short nodeID){
		return nodeList.get(nodeID);
	}
	
	public static void storeNode(Node n){
		lock.lock();
		nodeList.put(n.getMyID(), n);
		lock.unlock();
	}
	
	// metodo invocato dal thread Resetter
	
	public static void resetAllStats(){
		lock.lock();
		nodeList = Configurator.getNodeList();
		packetsList.clear();
		lastStoredData.clear();
		lastSummableAndGlobalCapabilities.clear();
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
	
	// ------------------------- metodi privati
	
	private static void updateNodeStats(Packet packet){
		packetsList.add(packet);
		// aggiorno la lista mypackets del nodo
		Node n = nodeList.get(packet.getSenderID());
		n.addMyPacket(packet);
		// salvo i relativi dati sommabili del nodo (Sbagliato op da effettuare quando processo la lista pacchetti)
		
/*		if(!packet.getSummableData().isEmpty()){
			LinkedList<Capability> summableData = packet.getSummableData();
			for (Capability c : summableData) {
				n.setLastSummableValue(c.getName(), c.getValue());
			}
		}
		// inserisco nella lista nodi il nodo aggiornato
*/		nodeList.put(n.getMyID(), n);
	}
	
	// aggiornamento contatore routedPacket
	private static void updateRouterCounter(Packet packet){
		LinkedList<Short> routers = packet.getHopsIndexes();
		for (Short nodeID : routers) {
			Node n = nodeList.get(nodeID);
			n.increaseRoutedPackets();
			
			nodeList.put(nodeID, n);
		}
	}

}
