package it.basestation.cmdline;

import java.util.LinkedList;

public class Packet {
	private long time;
	private short lastRouter;
	private short sender;
	private short counter;
	private short route;
	// distinzione tra due tipi di capability
	private LinkedList<Capability> data = new LinkedList<Capability>();
	private LinkedList<Capability> summableData = new LinkedList<Capability>();
	
	public Packet(long time,
			short lastRouter, 
			short sender, 
			short counter, 
			short route, 
			LinkedList<Capability> data, 
			LinkedList<Capability> summableData){
		
		this.time = time;
		this.lastRouter = lastRouter;
		this.sender = sender;
		this.counter = counter;
		this.route = route;
		this.data = data;
		this.summableData = summableData;
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
	
	public LinkedList<Capability> getSummableData(){
		return this.summableData;
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
}
