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
	
	// ultimi aggiornamenti effettuati sulle fusion Tables ------------------------------NOTA---------------Può servire?
	//private static Hashtable<Short, LinkedList<Capability>> lastStoredData = new Hashtable<Short, LinkedList<Capability>>();
	
	// ultimi valori sommabili e globali 
	private static LinkedList<Capability> lastSummableAndGlobalCapabilities = new LinkedList<Capability>();
	
	// hashtable globale per medie globali
	private static Hashtable<String, LinkedList<Double>> globalCapToElab = new Hashtable<String, LinkedList<Double>>();
	
	
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
	
	public static LinkedList<Capability> getLastSummableAndGlobalCapabilities(){
		lock.lock();
		LinkedList<Capability> toRet = lastSummableAndGlobalCapabilities;
		lock.unlock();
		return toRet;
	}
	
	public static void setLastSummableAndGlobalCapabilities(LinkedList<Capability> lastPeriodSumAndGlobCapabilities){
		lock.lock();
		lastSummableAndGlobalCapabilities = lastPeriodSumAndGlobCapabilities;
		lock.unlock();
	}
	
	// metodi set e get per lista capabilities globali
	public static void setGlobalCapToElab(Hashtable<String, LinkedList<Double>> newGlobalCapToElab){
		lock.lock();
		globalCapToElab = newGlobalCapToElab;
		lock.unlock();
	}
	
	public static Hashtable<String, LinkedList<Double>> getGlobalCapToElab(){
		lock.lock();
		Hashtable<String, LinkedList<Double>> newGlobalCapToElab= globalCapToElab;
		lock.unlock();
		return newGlobalCapToElab;
	}
	
	
	public static Double getCapabilityDelta( Short nodeID, String capName, Double value){
		lock.lock();
		// prendo dal nodo l'ultimo valore summable
		Double lastSummableValue = nodeList.get(nodeID).getLastSummableValue(capName);
		Double delta = 0.00;
		// la funzione get restituisce null se non trova la capability nell'hashtable
		// in questo caso significa che le stats sono state resettate quindi non è presente un valore
		// per quella capability
		if( lastSummableValue != null){
			delta = value - lastSummableValue;
			// controllo se il nodo si è riavviato -----------------------MANCANO I CONTATORI SUL NODO
			if(delta < 0.00){
				delta = 0.00;
			}
		}
		nodeList.get(nodeID).setLastSummableValue(capName, value);
		lock.unlock();
		return delta;
	}
	
//	public static Node getNode(Short nodeID){
//		return nodeList.get(nodeID);
//	}
	
//	public static void storeNode(Node n){
//		lock.lock();
//		nodeList.put(n.getMyID(), n);
//		lock.unlock();
//	}
	
	// metodo invocato dal thread Resetter
	
	public static void resetAllStats(){
		lock.lock();
		nodeList = Configurator.getNodeList();
		packetsList.clear();
//		lastStoredData.clear();
		lastSummableAndGlobalCapabilities.clear();
		globalCapToElab.clear();
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
