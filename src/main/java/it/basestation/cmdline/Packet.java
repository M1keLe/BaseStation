package it.basestation.cmdline;

import java.util.LinkedList;

public class Packet {
	private long time;
	private short lastRouter;
	private short sender;
	private short counter;
	private short route;
	
	private LinkedList<Capability> data = new LinkedList<Capability>();
	
	
	public Packet(long time,
			short lastRouter, 
			short sender, 
			short counter, 
			short route, 
			LinkedList<Capability> data){
		
		this.time = time;
		this.lastRouter = lastRouter;
		this.sender = sender;
		this.counter = counter;
		this.route = route;
		this.data = data;
		
	}
	
	public long getTime(){
		return this.time;
	}
	
	public short getLastRouter(){
		return this.lastRouter;
	}
	
	public short getSenderID(){
		return this.sender;
	}
	
	public short getCounter(){
		return this.counter;
	}
	
	public LinkedList<Capability> getData(){
		return this.data;
	}
	
	public LinkedList<Short> getHopsIndexes(){
		 LinkedList<Short> list = null;
		 if(this.route != 0){
			 list = new LinkedList<Short>();
			 for(short i = 1; i < 16; i++){
				//System.out.println("comparing "+(0x01 << i)+" and "+this.route+" results: "+((0x01 << i) & this.route));
				if(((0x01 << i) & this.route) == (0x01 << i)){
					//System.out.println(i+" is a router");					
					list.add(new Short(i));
				}
			 }
		 }
		 return list;
	}
	
	private String getCapabilitiesListData(){
		String toRet = "";
		for (Capability c : this.data) {
			Double value = c.getValue(); 
			toRet += "\nCapName=" + c.getName() + "\nValue: " + value.toString(); 
		}
		return toRet;
	}
	
	public String toString(){
		
		return "Packet Sender: " + this.sender + getCapabilitiesListData();
	}
}
