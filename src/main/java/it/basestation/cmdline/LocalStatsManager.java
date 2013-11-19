package it.basestation.cmdline;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class LocalStatsManager {
	// lista di nodi
	private static Hashtable<Short, Node> nodeList = new Hashtable<Short, Node>();
	// oppure
	// private static Hashtable<Short, Node> nodeList = Configurator.getNodeList();
	
	private static Hashtable<Short, DeltaCounter> deltaCounters = new Hashtable<Short, DeltaCounter>();
	
	// lista pacchetti
	private static LinkedList<Packet> packetsList = new LinkedList<Packet>();
	
	// liste ultimo periodo
	private static LinkedList<Packet> pushPacketList = new LinkedList<Packet>();
	private static LinkedList<Packet> pullPacketList = new LinkedList<Packet>();
	private static boolean listSelector = false;
	
	private static ReentrantLock lock = new ReentrantLock();
	
	// metodo invocato dal thread serial reader
	public static void addNewPacketNew(Packet p){		
		lock.lock();
		try{
			// se il pacchetto proviene da un nodo conosciuto lo aggiungo alle varie liste
			// e aggiorno i contatori routedPackets dei nodi "router"
			if(nodeList.containsKey(p.getSenderID())){
				packetsList.add(p);
				if(listSelector){
					pushPacketList.add(p);
					// System.out.println("============= [Nuovo pacchetto appena inserito:]" +pushPacketList.getLast());
				}else{
					pullPacketList.add(p);
					// System.out.println("============= [Nuovo pacchetto appena inserito:]" +pullPacketList.getLast());
				}
				
				//System.out.println("DEBUG: La lista per le fusion Tables contiene " + pushPacketList.size()+ " pacchetti");
				// aggiorno la lista mypackets del nodo
				nodeList.get(p.getSenderID()).addMyPacket(p);
				updateRouterCounter(p);
				
				
			}else{
				// altrimenti lo scarto
				System.out.println("Il pacchetto non appartiene alla lista di nodi conosciuti");
				System.out.println(p);
				System.out.println("Controllare il file di configurazione");
			}
			
		}finally{
			lock.unlock();
		}
	}
	public static void addNewPacket(Packet p){		
		lock.lock();
		try{
			// se il pacchetto proviene da un nodo conosciuto lo aggiungo alle varie liste
			// e aggiorno i contatori routedPackets dei nodi "router"
			if(nodeList.containsKey(p.getSenderID())){
				packetsList.add(p);
				pushPacketList.add(p);
					// System.out.println("============= [Nuovo pacchetto appena inserito:]" +pushPacketList.getLast());
				
				
				//System.out.println("DEBUG: La lista per le fusion Tables contiene " + pushPacketList.size()+ " pacchetti");
				// aggiorno la lista mypackets del nodo
				nodeList.get(p.getSenderID()).addMyPacket(p);
				updateRouterCounter(p);
				
				
			}else{
				// altrimenti lo scarto
				System.out.println("Il pacchetto non appartiene alla lista di nodi conosciuti");
				System.out.println(p);
				System.out.println("Controllare il file di configurazione");
			}
			
		}finally{
			lock.unlock();
		}
	}

	// metodi invocato dal thread DataProcessor
	public static  LinkedList<Packet> getLastPeriodPacketsListNew(){
		lock.lock();
		LinkedList<Packet> toRet = new LinkedList<Packet>();
		try{
		
			if(listSelector){
				toRet= pushPacketList;
				pullPacketList.clear();
			}else{
				toRet= pullPacketList;
				pushPacketList.clear();
			}
			
			listSelector = (listSelector)? false : true;
			// DEBUG
			System.out.println("DEBUG: iL DATAPROCESSOR HA PRESO "+toRet.size()+" pacchetti da gestire");
//			for (Packet p : toRet) {
//				System.out.println("");
//				System.out.println(p);
//				System.out.println("");
//			}
			
			// END DEBUG
			return toRet;
			
		}finally{
			lock.unlock();
		}
	}
	
	public static LinkedList<Packet> getLastPeriodPacketsList(){
		lock.lock();
		try{
			pullPacketList = pushPacketList;
			pushPacketList = new LinkedList<Packet>();
			System.out.println("DEBUG: DATAPROCESSOR HA PRESO "+pullPacketList.size()+" pacchetti da gestire");
/*			for (Packet p : pullPacketList) {
				System.out.println("");
				System.out.println(p);
				System.out.println("");
			}
*/			
			// END DEBUG
			return pullPacketList;
		}finally{
			lock.unlock();
		}
	}
	
	public static void elabDelta(Short nodeID, CapabilityInstance cI){
		if(!deltaCounters.containsKey(nodeID)){
			deltaCounters.put(nodeID, new DeltaCounter(nodeID));
		}
		deltaCounters.get(nodeID).elabDelta(cI);
	}
	
	// metodo invocato dal thread Resetter
	
	public static void resetAllStats(){
		lock.lock();
		try{
			nodeList = Configurator.getNodeList();
			packetsList.clear();
			pushPacketList.clear();
			pullPacketList.clear();
			System.out.println("Statistiche resettate");
		}finally{
			lock.unlock();
		}			
	}
	
	public static void setNodeList(){
		lock.lock();
		try{
			nodeList = Configurator.getNodeList();
		}finally{
			lock.unlock();
		}		
	}
	
	public static void setNodeList(Hashtable<Short, Node> listOfNodes){
		lock.lock();
		try{
			nodeList = listOfNodes;
		}finally{
			lock.unlock();
		}
		
	}
	
	// ------------------------- metodi privati
	
	
	// aggiornamento contatore routedPacket
	private static void updateRouterCounter(Packet packet){
		lock.lock();
		try{
			LinkedList<Short> routers = packet.getHopsIndexes();
			if(routers != null){
				for (Short nodeID : routers) {
					nodeList.get(nodeID).increaseRoutedPackets();
				}
			}
		}finally{
			lock.unlock();
		}
	}

}
