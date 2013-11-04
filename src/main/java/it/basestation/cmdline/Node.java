package it.basestation.cmdline;

import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;

public class Node {
	private short nodeID;
	private int xValue;
	private int yValue;
	private HashSet<String> capabilities;
	private LinkedList<Packet> myPackets = new LinkedList<Packet>();
	private long lastPacketTimeStamp = 0;
	private short routedPackets = 0;
	// misure derivate
	private boolean hasDerivedMeasures = false;
	private HashSet<String> derivedMeasures = new HashSet<String>();
	

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
	
	public boolean hasDerivedMeasures(){
		return this.hasDerivedMeasures;
	}
	
	public void addDerivedMeasure(String measure){
		this.hasDerivedMeasures = true;
		this.derivedMeasures.add(measure);
	}
	
	public HashSet<String> getDerivedMeasures(){
		return this.derivedMeasures;
	}
	
	
	@Override
	public String toString(){
		
		String s = "";
		for (String capName : this.capabilities) {
			s += " " + capName;
		}
		
		String sD = "";
		for (String derivedMeasure : this.derivedMeasures) {
			sD += " " + derivedMeasure;
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
