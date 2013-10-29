package it.basestation.cmdline;

import java.awt.Point;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class Node {
	private short nodeID;
	private int xValue;
	private int yValue;
	private HashSet<String> capabilities;
	private LinkedList<Packet> myPackets = new LinkedList<Packet>();
	private Hashtable<String,Double> lastSummableValues = new Hashtable<String,Double>();
	private long lastPacketTimeStamp = 0;
	private short routedPackets = 0;

	public Node(short nodeID, int xValue, int yValue, HashSet<String> capabilities) {
		this.nodeID = nodeID;
		this.xValue = xValue;
		this.yValue = yValue;
		this.capabilities = capabilities;
	}
	
	public boolean hasCapability(String c){
		return this.capabilities.contains(c);
	}
	
	public HashSet<String> getCapabilities(){
		return this.capabilities;
	}
	
	public Iterator<String> getCapabilitiesIterator(){
		return this.capabilities.iterator();
	}
	
	public short getMyID(){
		return this.nodeID;
	}
	
	public double getDistance(Node n){
		return new Point(n.xValue, n.yValue).distance(new Point(this.xValue, this.yValue));		
	}
	
	public void addMyPacket(Packet p){
		this.myPackets.add(p);
		this.lastPacketTimeStamp = p.getTime();
	}
	
	public void increaseRoutedPackets(){
		this.routedPackets++;
	}
	
	public short getRoutedPackets(){
		return this.routedPackets;
	}
	
	public long getLastPacketTimeStamp(){
		return this.lastPacketTimeStamp;
	}
	
	public void setLastSummableValue(String name, Double value){
		this.lastSummableValues.put(name, value);
	}
	
	public Double getLastSummableValue(String name){
		return this.lastSummableValues.get(name);
	}
	
	
	public String toString(){
		Iterator<String> i = this.capabilities.iterator();
		String s = "";
		while(i.hasNext()){
			s += i.next() + " ";
		}
		s= s.trim() + ".";
		return 	"********************* Node *********************" +
				"\nID: " +
				this.nodeID +
				"\nxValue: " +
				this.xValue +
				"\nyValue: " +
				this.yValue +
				"\nCapabilities:\n" +
				s +
				"\nPacchetti generati: "+ this.myPackets.size() +
				"\n******************  END Node ******************\n";
	}

}
