package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

public class LastPeriodNodeRecord {
	
	private Hashtable<String, LinkedList<CapabilityInstance>> capabilityInstancesList = new Hashtable<String, LinkedList<CapabilityInstance>>();
	private Hashtable<String, CapabilityInstance> dataToStore = new Hashtable<String, CapabilityInstance>();
	private Hashtable<String, Integer> counters = new Hashtable<String, Integer>();
	private short nodeID;
	public LastPeriodNodeRecord(short nodeID){
		//System.out.println("Creato node record con id " +nodeID);
		this.nodeID = nodeID;
		LinkedList<String> capabilitiesSet = Configurator.getNode(this.nodeID).getCapabilitiesSet();
		//System.out.println("DEBUG: NodeRecord_"+this.nodeID+" capabilitiesSet = "+capabilitiesSet);
		for (String s : capabilitiesSet) {
			CapabilityInstance c = Configurator.getCapabilityInstance(s);
			this.dataToStore.put(s, c);
			this.counters.put(s, 0);
		}
	}
	
	public void addPacket(Packet p){
		LinkedList<CapabilityInstance> dataList = p.getDataList();
		for (CapabilityInstance cI : dataList) {
			// store su lista debug
			if(!this.capabilityInstancesList.containsKey(cI.getName())){
				this.capabilityInstancesList.put(cI.getName(), new LinkedList<CapabilityInstance>());
			}
			this.capabilityInstancesList.get(cI.getName()).add(cI);
			// end store su lista debug
			
			// controllo se il valore è da mediare
			if(cI.localOperator().contains("avg") && cI.getMinValue()< cI.getValue() && cI.getValue() < cI.getMaxValue() ){
				int lastCounter = this.counters.get(cI.getName()).intValue();
				int newCounter = lastCounter +1 ;
				double lastAvg = this.dataToStore.get(cI.getName()).getValue();
				double temp = lastAvg * lastCounter;
				temp += cI.getValue();
				double neWAvg = temp / newCounter;
				// aggiornamento valori
				this.counters.put(cI.getName(), newCounter);
				this.dataToStore.get(cI.getName()).setValue(neWAvg);
				
			}else if(cI.localOperator().contains("last")){
				// salvo l'ultimo valore
				this.dataToStore.put(cI.getName(), cI);
			}
			
			// aggiorno dati derivati
			
			Enumeration<String> e = this.dataToStore.keys();
			while(e.hasMoreElements()){
				String name = e.nextElement();
				//controllo se è un valore derivato
				if(!this.dataToStore.get(name).localOperator().contains("avg") &&
						!this.dataToStore.get(name).localOperator().contains("last")){
					this.dataToStore.get(name).setValue(this.getDerivedMeasure(this.dataToStore.get(name)));
					
				}
			}
			
			
		}
	}
	
	public LinkedList<CapabilityInstance> getDataListToStore(){
		LinkedList<CapabilityInstance> toRet = new LinkedList<CapabilityInstance>();
		Enumeration<String> e = this.dataToStore.keys();
		while(e.hasMoreElements()){
			String name = e.nextElement();
			toRet.add(this.dataToStore.get(name));
		}
		return toRet;
	}
	
	public short getNodeID(){
		return this.nodeID;
	}
	
	
	private double getDerivedMeasure(CapabilityInstance cI) {
		double result = 0.00;
		String syntax = cI.localOperator();
		// suddivido la stringa in vari tokens
        String[] tokens = syntax.split(" ");
        
        // sostituisco il nome della capability con il valore
        
        for (int i = 0; i < tokens.length; i++) {
        	if(this.dataToStore.get(tokens[i]) != null){
        		Double value = this.dataToStore.get(tokens[i]).getValue();
        		tokens[i] = value.toString();
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
		Enumeration<String> e = this.capabilityInstancesList.keys();
		while(e.hasMoreElements()){
			String name = e.nextElement();
			toRet+= "\n["+name+"] ->> [";
			for (CapabilityInstance c : this.capabilityInstancesList.get(name)) {
				toRet+= " " +c.getValue() + ",";
			}
			toRet = toRet.substring(0, toRet.length() -1);
			toRet+= "]\n";
		}
		
		toRet += "\n -------------- Data To Store --------------\n"; 
		
		for (CapabilityInstance cI : this.getDataListToStore()) {
			toRet += cI.getName() +": " + cI.getValue() + ", ";
		}
		
		toRet +="\n ====[END_NODE_RECORD_ID:"+this.nodeID+"] \n";
		
		return toRet;
	}
	
}
