package it.basestation.cmdline;

import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;

public class Node {
	private short nodeID = 0;
	private int xValue = 0;
	private int yValue = 0;
	private HashSet<String> capabilities = new HashSet<String>();
	private LinkedList<Packet> myPackets = new LinkedList<Packet>();
	private long lastPacketTimeStamp = 0;
	private short routedPackets = 0;
	

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
	
	public HashSet<String> getCapabilitiesSet(){
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
	
	
	@Override
	public String toString(){
		
		String s = "";
		for (String capName : this.capabilities) {
			s += " " + capName;
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
