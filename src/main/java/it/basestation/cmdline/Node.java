package it.basestation.cmdline;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;

public class Node {
	private short nodeID;
	private int xValue;
	private int yValue;
	private HashSet<String> capabilities;
	//private LinkedList<Packet> myPackets;

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
	
	public double getDistance(Node n){
		return new Point(n.xValue, n.yValue).distance(new Point(this.xValue, this.yValue));		
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
				"\n******************  END Node ******************\n";
	}

}
