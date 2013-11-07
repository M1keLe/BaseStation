package it.basestation.cmdline;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

public class LastPeriodNodeRecord {
	
	private Hashtable<String, LinkedList<Capability>> capabilitiesToElab = new Hashtable<String, LinkedList<Capability>>();
	private LinkedList<Capability> capListToStore = new LinkedList<Capability>();
	private short nodeID;
	public LastPeriodNodeRecord(short nodeID){
		System.out.println("Creato node record con id " +nodeID);
		this.nodeID = nodeID;
/*		HashSet<String> capabilitiesSet = Configurator.getNode(nodeID).getCapabilitiesSet();
		System.out.println("DEBUG: NodeRecord_"+nodeID+" capabilitiesSet = "+capabilitiesSet);
		for (String c : capabilitiesSet) {
			this.capabilitiesToElab.put(c, new LinkedList<Capability>());
			this.capListToStore.add(Configurator.getCapability(c));
			
		}
*/	}
	
	public void addPacket(Packet p){
		LinkedList<Capability> data = p.getData();
		for (Capability c : data) {
			if(!this.capabilitiesToElab.containsKey(c.getName())){
				this.capabilitiesToElab.put(c.getName(), new LinkedList<Capability>());
			}
			this.capabilitiesToElab.get(c.getName()).add(c);
		}
	}
	
/*	public void addCapability (Capability capability){
		System.out.println("DEBUG: NODE_RECORD ID = "+this.nodeID+" Sto aggiungendo alla lista da elaborare la capability: \n" +capability);
		this.capabilitiesToElab.get(capability.getName()).add(capability);
	}
	
	public LinkedList<Capability> getCapListToStore(){
		HashSet<String> capSet = Configurator.getNode(nodeID).getCapabilitiesSet();
		LinkedList<Capability> toRet = new LinkedList<Capability>();
		for (String c : capSet) {
			Capability cap = Configurator.getCapability(c);
			if(cap.getLocalRule().equals("avg")){
				cap.setValue(getAvg(cap.getName()));
				
			} else if(cap.getLocalRule().equals("last")){
				cap.setValue(getLastRecordedValue(cap.getName()));
			}
			toRet.add(cap);
		}
		return toRet;
	}
*/	
	public LinkedList<Capability> getCapListToStore(){
		HashSet<String> capabilitiesSet = Configurator.getNode(this.nodeID).getCapabilitiesSet();
		System.out.println("DEBUG: NodeRecord_"+this.nodeID+" capabilitiesSet = "+capabilitiesSet);
		for (String c : capabilitiesSet) {
			
			this.capListToStore.add(Configurator.getCapability(c));
			
		}
		
		for (Capability c : this.capListToStore) {
			
			if(c.getLocalRule().equals("avg")){
				c.setValue(getAvg(c.getName()));
				
			} else if(c.getLocalRule().equals("last")){
				c.setValue(getLastRecordedValue(c.getName()));
			}
		}
		return this.capListToStore;
	}
	
	public double getLastRecordedValue(String name){
		double toRet = 0.00;
		if(!this.capabilitiesToElab.get(name).isEmpty() && this.capabilitiesToElab.get(name) != null){
			toRet = this.capabilitiesToElab.get(name).getLast().getValue();
			System.out.println("NODE_RECORD_"+this.nodeID+": Ultimo valore registrato value = " +toRet+ " Capability: "+ name);
		}else{
			System.out.println("NODE_RECORD_"+this.nodeID+": Valore non presente!!!  Capability: "+ name);
		}
		return toRet;
	}
	
	public double getAvg(String name) {
		double toRet = 0.00;
		
		if(!this.capabilitiesToElab.get(name).isEmpty() && this.capabilitiesToElab.get(name) != null){
			LinkedList<Capability> capList = this.capabilitiesToElab.get(name);
			double value = 0.00;
			int counter = 0;
			for (Capability c : capList) {
				System.out.println("NODE_RECORD_"+this.nodeID+": Calcolo della media value = " +value+ " counter = " +counter+" Capability: "+ name);
				if(c.getMinValue()<=c.getValue() && c.getValue()<=c.getMaxValue()){
					value += c.getValue();
					counter++;
					System.out.println("NODE_RECORD_"+this.nodeID+":Aggiornamento media value = " +value+ " counter = " +counter +" valore aggiunto = "+ c.getValue()+" Capability: "+ name);
				}
			}
			if(counter>0){
				toRet = value/counter;
			}
		}
		System.out.println("NODE_RECORD_"+this.nodeID+":La Media da registrare è: " +toRet+" Capability: "+ name);
		return toRet;
	}
	
	public short getNodeID(){
		return this.nodeID;
	}
	
	
	
}
