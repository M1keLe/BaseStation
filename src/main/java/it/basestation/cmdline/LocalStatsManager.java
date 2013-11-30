package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class LocalStatsManager {
	// lista di nodi
	private static Hashtable<Short, Node> nodeList = new Hashtable<Short, Node>();
	
	// al momento non utilizzati
	private static Hashtable<Short, PeopleCounter> peopleCounters = new Hashtable<Short, PeopleCounter>();
	private static Hashtable<Short, DeltaCounter> deltaCounters = new Hashtable<Short, DeltaCounter>();
	
	// lista pacchetti
	private static LinkedList<Packet> packetsList = new LinkedList<Packet>();
	
	// liste ultimo periodo
	private static LinkedList<Packet> pushPacketsList = new LinkedList<Packet>();
	private static LinkedList<Packet> pullPacketsList = new LinkedList<Packet>();		
	
	private static ReentrantLock lock = new ReentrantLock();
	
	
	public static void addNewPacket(Packet p){		
		lock.lock();
		try{
			// se il pacchetto proviene da un nodo conosciuto lo aggiungo alle varie liste
			// e aggiorno i contatori routedPackets dei nodi "router"
			if(nodeList.containsKey(p.getSenderID())){
				packetsList.add(p);
				pushPacketsList.add(p);
				// System.out.println("============= [Nuovo pacchetto appena inserito:]" +pushPacketList.getLast());
				//System.out.println("DEBUG: La lista per le fusion Tables contiene " + pushPacketList.size()+ " pacchetti");
				
				// aggiorno la lista mypackets del nodo
				nodeList.get(p.getSenderID()).addMyPacket(p);
				updateRouterCounter(p);
				//Debug
				//System.out.println("Pacchetti in packetsList = " + packetsList.size());
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

	
	public static LinkedList<Packet> getLastPeriodPacketsList(){
		lock.lock();
		try{
			pullPacketsList = pushPacketsList;
			pushPacketsList = new LinkedList<Packet>();
			//Debug
			System.out.println("DEBUG: DATAPROCESSOR HA PRESO "+pullPacketsList.size()+" pacchetti da gestire");
/*			for (Packet p : pullPacketList) {
				System.out.println("");
				System.out.println(p);
				System.out.println("");
			}
*/			
			// END DEBUG
			return pullPacketsList;
		}finally{
			lock.unlock();
		}
	}
	
	
	// al momento non utilizzato
	public static void countPeople(Short nodeID, CapabilityInstance cI){
		if(!peopleCounters.containsKey(nodeID)){
			peopleCounters.put(nodeID, new PeopleCounter());
		}
		peopleCounters.get(nodeID).elabCapabilityInstance(cI);
	}
	
	// al momento non utilizzato
	public static void elabDelta(Short nodeID, CapabilityInstance cI){
		if(!deltaCounters.containsKey(nodeID)){
			deltaCounters.put(nodeID, new DeltaCounter());
		}
		deltaCounters.get(nodeID).elabDelta(cI);
	}	
	
	// metodo invocato dal thread Resetter	
	public static void resetAllStats(){
		lock.lock();
		try{
			
			packetsList = new LinkedList<Packet>();
			pushPacketsList = new LinkedList<Packet>();
			// people counter istanziati in DataProcessor
			// peopleCounters = new Hashtable<Short, PeopleCounter>();
			// deltaCounters = new Hashtable<Short, DeltaCounter>();

			// resetto le stats di ogni nodo
			Enumeration<Short> e = nodeList.keys();
			while (e.hasMoreElements()) {
				Short nodeID = (Short) e.nextElement();
				nodeList.get(nodeID).resetStats();				
			}					
			System.out.println("Local Stats Manager: Statistiche resettate");
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
