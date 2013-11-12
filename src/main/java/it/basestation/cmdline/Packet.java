package it.basestation.cmdline;

import java.util.LinkedList;

public class Packet {
	private long time;
	private short lastRouter;
	private short sender;
	private short counter;
	private short route;
	
	private  LinkedList<Capability> capabilityList;
	
	
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
		capabilityList = new LinkedList<Capability>();
		this.capabilityList = data;
		
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
	
	public LinkedList<Capability> getCapabilityList(){
		return this.capabilityList;
	}
	
	public LinkedList<Short> getHopsIndexes(){
		 LinkedList<Short> list = null;
		 if(this.route != 0){
			 list = new LinkedList<Short>();
			 for(short i = 1; i < 16; i++){
				//Printer.println("comparing "+(0x01 << i)+" and "+this.route+" results: "+((0x01 << i) & this.route));
				if(((0x01 << i) & this.route) == (0x01 << i)){
					//Printer.println(i+" is a router");					
					list.add(new Short(i));
				}
			 }
		 }
		 return list;
	}
	
	private String getCapabilitiesListData(){
		String toRet = "";
		for (Capability c : this.capabilityList) {
			Double value = c.getValue(); 
			toRet += "\n" + c.getName() + ": " + value.toString() +"\n\t- Local: "+ c.localOperator() +"\n\t- Global: "+ c.globalOperator(); 
		}
		return toRet;
	}
	
	public String toString(){
		
		return "\n------------------ PACKET ---------------------\n" +
			   "Sender: " + this.sender + getCapabilitiesListData() +
			   "\nVARIABILI: \n\tCounter: "+ this.counter +
			   "\n\tLast Router: "+this.lastRouter + 
			   "\n\tRoute: "+this.route +
			   "\n\tTime: "+ this.time +
			   "\n-------------------END PACKET--------------------\n";
	}
}
