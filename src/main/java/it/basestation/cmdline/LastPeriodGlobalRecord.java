package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

public class LastPeriodGlobalRecord {
	
	private Hashtable<Short, LastPeriodNodeRecord> lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
	
	private LinkedList<Capability> globalCapabilitiesListToStore = new LinkedList<Capability>();
	private LinkedList<Capability> derivedCapabilitiesListToStore = new LinkedList<Capability>();
	
	public LastPeriodGlobalRecord(){
		HashSet<String> globalCapabilitiesSet = new HashSet<String>();
		globalCapabilitiesSet = Configurator.getGlobalCapabilitiesSet();
		for (String s : globalCapabilitiesSet) {
			Capability c = Configurator.getCapability(s);
			if(c.getGlobalRule().equals("avg")|| c.getGlobalRule().equals("sum")){
				//System.out.println("DEBUG: globalCapabilitiesListToStore = " + c.getName());
				this.globalCapabilitiesListToStore.add(c);
			}else{
				this.derivedCapabilitiesListToStore.add(c);
				//System.out.println("DEBUG: derivedCapabilitiesListToStore = " + c.getName());
				
			}
			
		}
	}
	
	//public LinkedList<Capability> getGlobalValuesToStore(Hashtable<Short, LastPeriodNodeRecord> lastPeriodNodesRecord){
	public LinkedList<Capability> getGlobalValuesToStore(){
		
		LinkedList<Capability> globalValuesToStore = new LinkedList<Capability>();
		//this.lastPeriodNodesRecord = lastPeriodNodesRecord;
		
		for (Capability c : this.globalCapabilitiesListToStore) {
			
			if(c.getGlobalRule().equals("avg")){
				c.setValue(getAvg(c.getName()));
				
			}else if(c.getGlobalRule().equals("sum")){
				c.setValue(getSumOfValues(c.getName()));
				
			}				
		} // end foreach capabilitiestostore
		globalValuesToStore = this.globalCapabilitiesListToStore;
		
		// elaboro le misure derivate
		for (Capability c : this.derivedCapabilitiesListToStore) {
			c.setValue(getDerivedMeasure(c));
			globalValuesToStore.add(c);
		}
		return globalValuesToStore;
		
	}

	private double getDerivedMeasure(Capability c) {
		double result = 0.00;
		String syntax = c.getGlobalRule();
		// suddivido la stringa in vari tokens
        String[] tokens = syntax.split(" ");
        
        // sostituisco il nome della capability con il valore
        
        for (int i = 0; i < tokens.length; i++) {
        	for (Capability cap : globalCapabilitiesListToStore) {
        		if(tokens[i].equals(cap.getName())){
        			Double value = cap.getValue();
        			//System.out.println("DEBUG: Sto trasformando il valore "+value+ "in stringa");
        			tokens[i] = value.toString();
        			//System.out.println("DEBUG: Modificato il token numero "+i+ "in " + tokens[i]);
        		}
        	}
		}
/*        for (String string : tokens) {
        	for (Capability cap : globalCapabilitiesListToStore) {
        		if(string.equals(cap.getName())){
        			Double value = cap.getValue();
        			//System.out.println("DEBUG: Sto trasformando il valore "+value+ "in stringa");
        			string = value.toString();
        		}
        	}
        }
*/        // calcolo il valore derivato
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

	private double getSumOfValues(String name) {
		double toRet = 0.00;
		Enumeration<Short> e = this.lastPeriodNodesRecord.keys();
		while(e.hasMoreElements()){
			short nodeID = e.nextElement();
			
			if(Configurator.getNode(nodeID).hasCapability(name)){
				LastPeriodNodeRecord nodeRecord = this.lastPeriodNodesRecord.get(nodeID);
					
				if(nodeRecord != null){
					System.out.println("ID DEL NODE_RECORD DA ESTRARRE = "+ nodeID + ", ID DEL NODE_RECORD ESTRATTO = "+ nodeRecord.getNodeID());
					toRet += nodeRecord.getLastRecordedValue(name);
				}
				
			}
		}
		return toRet;
	}

	private double getAvg(String name) {
		Enumeration<Short> e = this.lastPeriodNodesRecord.keys();
		double toRet = 0.00;
		int counter = 0;
		double value = 0.00;
		while(e.hasMoreElements()){
			short nodeID = e.nextElement();
			
			if(Configurator.getNode(nodeID).hasCapability(name)){
				LastPeriodNodeRecord nodeRecord = this.lastPeriodNodesRecord.get(nodeID);
				
				if(nodeRecord != null){
					value+= nodeRecord.getAvg(name);
					counter++;
				}
			}
		}
		if(counter>0){
			toRet = value/counter;
		}
		
		return toRet;
	}

	public void setLastNodesRecord(	Hashtable<Short, LastPeriodNodeRecord> newNodesRecord) {
		this.lastPeriodNodesRecord = newNodesRecord;
		
	}

}
