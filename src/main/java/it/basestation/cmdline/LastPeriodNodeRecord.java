package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

public class LastPeriodNodeRecord {
	
	private Hashtable<String, LinkedList<Capability>> capabilitiesToElab = new Hashtable<String, LinkedList<Capability>>();
	private LinkedList<Capability> capListToStore = new LinkedList<Capability>();
	private LinkedList<Capability> derivedCapListToStore = new LinkedList<Capability>();
	private short nodeID;
	public LastPeriodNodeRecord(short nodeID){
		//Printer.println("Creato node record con id " +nodeID);
		this.nodeID = nodeID;
		LinkedList<String> capabilitiesSet = Configurator.getNode(this.nodeID).getCapabilitiesSet();
		//Printer.println("DEBUG: NodeRecord_"+this.nodeID+" capabilitiesSet = "+capabilitiesSet);
		for (String s : capabilitiesSet) {
			Capability c = Configurator.getCapability(s);
			if(c.localOperator().equals("avg")||c.localOperator().equals("last")){
				this.capListToStore.add(c);
			}else{
				this.derivedCapListToStore.add(c);
			}
			
			
			
		}
	}
	
	public void addPacket(Packet p){
		LinkedList<Capability> data = p.getCapabilityList();
		for (Capability c : data) {
			if(!this.capabilitiesToElab.containsKey(c.getName())){
				this.capabilitiesToElab.put(c.getName(), new LinkedList<Capability>());
			}
			this.capabilitiesToElab.get(c.getName()).add(c);
		}
	}
	
	public LinkedList<Capability> getCapListToStore(){
		LinkedList<Capability> localValuesToStore = new LinkedList<Capability>();
		
		for (Capability c : this.capListToStore) {
			
			if(c.localOperator().contains("avg")){
				c.setValue(getAvg(c.getName()));
			}
				
			if(c.localOperator().contains("last")){
				c.setValue(getLastRecordedValue(c.getName()));
			}
		}
		
		localValuesToStore = this.capListToStore;
		
		// elaboro le misure derivate se presenti
		if(this.derivedCapListToStore != null){
			for(Capability c : this.derivedCapListToStore){
				c.setValue(getDerivedMeasure(c));
				localValuesToStore.add(c);
			}
		}
		return localValuesToStore;
	}
	
	public short getNodeID(){
		return this.nodeID;
	}
	
	public String debug(){
		String toRet = "";
		toRet += "\n =====[NODE_RECORD_ID:"+this.nodeID+"] \n";
		Enumeration<String> e = this.capabilitiesToElab.keys();
		while(e.hasMoreElements()){
			String name = e.nextElement();
			toRet+= "\n["+name+"] ->> [";
			for (Capability c : this.capabilitiesToElab.get(name)) {
				toRet+= " " +c.getValue() + ",";
			}
			toRet = toRet.substring(0, toRet.length() -1);
			toRet+= "]\n";
		}
		toRet +="\n ====[END_NODE_RECORD_ID:"+this.nodeID+"] \n";
		
		return toRet;
	}
	
	private double getLastRecordedValue(String name){
		double toRet = 0.00;
		if(!this.capabilitiesToElab.get(name).isEmpty() && this.capabilitiesToElab.get(name) != null){
			toRet = this.capabilitiesToElab.get(name).getLast().getValue();
			Printer.println("NODE_RECORD_"+this.nodeID+": Ultimo valore registrato value = " +toRet+ " Capability: "+ name);
		}else{
			Printer.println("NODE_RECORD_"+this.nodeID+": Valore non presente!!!  Capability: "+ name);
		}
		return toRet;
	}
	
	private double getAvg(String name) {
		double toRet = 0.00;
		
		if(!this.capabilitiesToElab.get(name).isEmpty() && this.capabilitiesToElab.get(name) != null){
			LinkedList<Capability> capList = this.capabilitiesToElab.get(name);
			double value = 0.00;
			int counter = 0;
			for (Capability c : capList) {
				Printer.println("NODE_RECORD_"+this.nodeID+": Calcolo della media"+ name +": "+ value + " contatore = " +counter);
				if(c.getMinValue()<=c.getValue() && c.getValue()<=c.getMaxValue()){
					value += c.getValue();
					counter++;
					Printer.println("NODE_RECORD_"+this.nodeID+":Aggiornamento media "+ name +": " +value+ " counter = " +counter +" valore appena sommato = "+ c.getValue());
				}
			}
			if(counter>0){
				toRet = value/counter;
			}
		}
		Printer.println("NODE_RECORD_"+this.nodeID+":La Media da registrare è: " +toRet+" Capability: "+ name);
		return toRet;
	}
	
	private double getDerivedMeasure(Capability c) {
		double result = 0.00;
		String syntax = c.globalOperator();
		// suddivido la stringa in vari tokens
        String[] tokens = syntax.split(" ");
        
        // sostituisco il nome della capability con il valore
        
        for (int i = 0; i < tokens.length; i++) {
        	for (Capability cap : this.capListToStore) {
        		if(tokens[i].equals(cap.getName())){
        			Double value = cap.getValue();
        			//Printer.println("DEBUG: Sto trasformando il valore "+value+ "in stringa");
        			tokens[i] = value.toString();
        			//Printer.println("DEBUG: Modificato il token numero "+i+ "in " + tokens[i]);
        		}
        	}
		}
        
        // calcolo il valore derivato
        Stack<String> ops = new Stack<String>();
        Stack<Double> vals = new Stack<Double>();
        
        for (String s : tokens) {
        	if (s.equals("(")) ;
		    else if (s.equals("+")) ops.push(s);
		    else if (s.equals("-")) ops.push(s);
		    else if (s.equals("*")) ops.push(s);
		    else if (s.equals("/")) ops.push(s);
		    else if (s.equals("sqrt")) ops.push(s);
		    else if (s.equals(")")) {
		        String op = ops.pop();
		        double v = vals.pop();
		        if (op.equals("+")) v = vals.pop() + v;
		        else if (op.equals("-")) v = vals.pop() - v;
		        else if (op.equals("*")) v = vals.pop() * v;
		        else if (op.equals("/")) v = vals.pop() / v;
		        else if (op.equals("sqrt")) v = Math.sqrt(v);
		        vals.push(v);
		    }
        	
        	else{
        		
        		vals.push(Double.parseDouble(s));
        	}
        }
        if(!vals.isEmpty()){
        	result = vals.pop();
        }else{
        	Printer.println("DijkstraTwoStack ERROR: impossibile impostare il valore di questa misura");
        }      
        return result;
	
	}
	
}
