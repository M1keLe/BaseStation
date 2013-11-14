package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

public class LastPeriodNodeRecord {
	
	private Hashtable<String, LinkedList<DataContainer>> dataToElab = new Hashtable<String, LinkedList<DataContainer>>();
	private LinkedList<DataContainer> dataListToStore = new LinkedList<DataContainer>();
	private short nodeID;
	public LastPeriodNodeRecord(short nodeID){
		//System.out.println("Creato node record con id " +nodeID);
		this.nodeID = nodeID;
		LinkedList<String> capabilitiesSet = Configurator.getNode(this.nodeID).getCapabilitiesSet();
		//System.out.println("DEBUG: NodeRecord_"+this.nodeID+" capabilitiesSet = "+capabilitiesSet);
		for (String s : capabilitiesSet) {
			DataContainer c = Configurator.getDataContainerByName(s);
			this.dataListToStore.add(c);	
		}
	}
	
	public void addDataContainer(DataContainer dC){
		if(!this.dataToElab.containsKey(dC.getName())){
			this.dataToElab.put(dC.getName(), new LinkedList<DataContainer>());
		}
		this.dataToElab.get(dC.getName()).add(dC);
	}
	
	public void addPacket(Packet p){
		LinkedList<DataContainer> dataList = p.getDataList();
		for (DataContainer dC : dataList) {
			if(!this.dataToElab.containsKey(dC.getName())){
				this.dataToElab.put(dC.getName(), new LinkedList<DataContainer>());
			}
			this.dataToElab.get(dC.getName()).add(dC);
		}
	}
	
	public LinkedList<DataContainer> getDataListToStore(){
		//LinkedList<DataContainer> localValuesToStore = new LinkedList<DataContainer>();
		boolean needToElabDerivedMeasure = false;
		for (DataContainer c : this.dataListToStore) {			
			if(c.localOperator().contains("avg")){
				// inserire controlli su min e max value
				c.setValue(getAvg(c.getName()));
			}	
			else if(c.localOperator().contains("last")){
				// inserire controlli su min e max value
				c.setValue(getLastRecordedValue(c.getName()));
			}else{
				needToElabDerivedMeasure = true;
			}
			
		}
		if(needToElabDerivedMeasure){
			
			for (DataContainer c : this.dataListToStore) {
				if(!c.localOperator().contains("avg") && !c.localOperator().contains("last")){
					// inserire controlli su min e max value
					c.setValue(getDerivedMeasure(c));
				}
			}
		}
		
		return this.dataListToStore;
	}
	
	public short getNodeID(){
		return this.nodeID;
	}
	
	private double getLastRecordedValue(String name){
		double toRet = 0.00;
		if(!this.dataToElab.get(name).isEmpty() && this.dataToElab.get(name) != null){
			toRet = this.dataToElab.get(name).getLast().getValue();
			//System.out.println("NODE_RECORD_"+this.nodeID+": Ultimo valore registrato value = " +toRet+ " Capability: "+ name);
		}else{
			//System.out.println("NODE_RECORD_"+this.nodeID+": Valore non presente!!!  Capability: "+ name);
		}
		return toRet;
	}
	
	private double getAvg(String name) {
		double toRet = 0.00;
		
		if(!this.dataToElab.get(name).isEmpty() && this.dataToElab.get(name) != null){
			LinkedList<DataContainer> capList = this.dataToElab.get(name);
			double value = 0.00;
			int counter = 0;
			for (DataContainer c : capList) {
				//System.out.println("NODE_RECORD_"+this.nodeID+": Calcolo della media"+ name +": "+ value + " contatore = " +counter);
				if(c.getMinValue()<c.getValue() && c.getValue()<c.getMaxValue()){
					value += c.getValue();
					counter++;
					//System.out.println("NODE_RECORD_"+this.nodeID+":Aggiornamento media "+ name +": " +value+ " counter = " +counter +" valore appena sommato = "+ c.getValue());
				}
			}
			if(counter>0){
				toRet = value/counter;
			}
		}
		//System.out.println("NODE_RECORD_"+this.nodeID+":La Media da registrare è: " +toRet+" Capability: "+ name);
		return toRet;
	}
	
	private double getDerivedMeasure(Capability c) {
		double result = 0.00;
		String syntax = c.localOperator();
		// suddivido la stringa in vari tokens
        String[] tokens = syntax.split(" ");
        
        // sostituisco il nome della capability con il valore
        
        for (int i = 0; i < tokens.length; i++) {
        	for (DataContainer cap : this.dataListToStore) {
        		if(tokens[i].equals(cap.getName())){
        			Double value = cap.getValue();
        			//System.out.println("DEBUG: Sto trasformando il valore "+value+ "in stringa");
        			tokens[i] = value.toString();
        			//System.out.println("DEBUG: Modificato il token numero "+i+ "in " + tokens[i]);
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
        	System.out.println("DijkstraTwoStack ERROR: impossibile impostare il valore di questa misura");
        }      
        return result;
	}
	
	@Override
	public String toString(){
		String toRet = "";
		toRet += "\n =====[NODE_RECORD_ID:"+this.nodeID+"] \n";
		Enumeration<String> e = this.dataToElab.keys();
		while(e.hasMoreElements()){
			String name = e.nextElement();
			toRet+= "\n["+name+"] ->> [";
			for (DataContainer c : this.dataToElab.get(name)) {
				toRet+= " " +c.getValue() + ",";
			}
			toRet = toRet.substring(0, toRet.length() -1);
			toRet+= "]\n";
		}
		toRet +="\n ====[END_NODE_RECORD_ID:"+this.nodeID+"] \n";
		
		return toRet;
	}
	
}
