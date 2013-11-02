package it.basestation.cmdline;

import java.awt.Point;
import java.util.Enumeration;
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
	private boolean hasDerivedMeasures = false;
	private Hashtable<String,String> derivedMeasures = new Hashtable<String,String>();

	public Node(short nodeID, int xValue, int yValue, HashSet<String> capabilities) {
		this.nodeID = nodeID;
		this.xValue = xValue;
		this.yValue = yValue;
		this.capabilities = capabilities;
	}
	
	// alcuni metodi sono da eliminare ------------------------ NOTA -----------------------
	
	public boolean hasCapability(String c){
		return this.capabilities.contains(c);
	}
	
	public HashSet<String> getCapabilities(){
		return this.capabilities;
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
	
	public boolean hasDerivedMeasures(){
		return this.hasDerivedMeasures;
	}
	
	public void addDerivedMeasure(String measure, String syntax){
		this.hasDerivedMeasures = true;
		this.derivedMeasures.put(measure, syntax);
	}
	
	public Hashtable<String,String> getDerivedMeasures(){
		return this.derivedMeasures;
	}
	
	
	@Override
	public String toString(){
		
		String s = "";
		for (String capName : this.capabilities) {
			s += " " + capName;
		}
		
		Enumeration<String> e = this.derivedMeasures.keys();
		String sD = "";
		while(e.hasMoreElements()){
			sD += " " +e.nextElement();
		}
		
		s= s.trim() + ".";
		if(sD.isEmpty())
			sD="None";
		sD = sD.trim() + ".";
		return 	"********************* Node *********************" +
				"\nID: " +
				this.nodeID +
				"\nxValue: " +
				this.xValue +
				"\nyValue: " +
				this.yValue +
				"\nCapabilities:\n" +
				s +
				"\nDerived Measures:\n" +
				sD +
				"\nPacchetti generati: "+ this.myPackets.size() +
				"\n******************  END Node ******************\n";
	}

}
