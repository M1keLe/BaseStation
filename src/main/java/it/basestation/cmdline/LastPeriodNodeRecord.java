package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

public class LastPeriodNodeRecord {
	// lista debug
	private Hashtable<String, LinkedList<CapabilityInstance>> capabilityInstancesList = new Hashtable<String, LinkedList<CapabilityInstance>>();

	private Hashtable<String, CapabilityInstance> dataToStore = new Hashtable<String, CapabilityInstance>();
	private LinkedList<CapabilityInstance> mMListToStore = new LinkedList<CapabilityInstance>();
	private Hashtable<String, Integer> counters = new Hashtable<String, Integer>();
	private short nodeID;
	public LastPeriodNodeRecord(short nodeID){
		//System.out.println("Creato node record con id " +nodeID);
		this.nodeID = nodeID;
		LinkedList<String> capabilitiesSet = Configurator.getNode(this.nodeID).getCapabilitiesSet();
		//System.out.println("DEBUG: NodeRecord_"+this.nodeID+" capabilitiesSet = "+capabilitiesSet);
		for (String name : capabilitiesSet) {
			LinkedList<CapabilityInstance> cList = Configurator.getCapabilityInstanceList(name, "local", false);
			for (CapabilityInstance cI : cList) {
				this.dataToStore.put(cI.getColumnName(), cI);
				// se li valore è da mediare inizializzo il contatore
				if(cI.localOperator().equals("avg")){
					this.counters.put(cI.getColumnName(), 0);
				}
			}
		}
	}
	
	
	public void addCapabilityInstance(CapabilityInstance cI){
		// store su lista debug
		if (!this.capabilityInstancesList.containsKey(cI.getColumnName())){
			this.capabilityInstancesList.put(cI.getColumnName(), new LinkedList<CapabilityInstance>());
		}
		this.capabilityInstancesList.get(cI.getColumnName()).add(cI);
		
		// controllo se il valore è da mediare
		if(cI.localOperator().equals("avg")){
			int lastCounter = this.counters.get(cI.getColumnName()).intValue();
			int newCounter = lastCounter + 1;
			double lastAvg = this.dataToStore.get(cI.getColumnName()).getValue();
			double temp = lastAvg * lastCounter;
			temp += cI.getValue();
			double neWAvg = temp / newCounter;
			// aggiornamento valori
			this.counters.put(cI.getColumnName(), newCounter);
			this.dataToStore.get(cI.getColumnName()).setValue(neWAvg);
		
		// controllo se il valore da prendere è l'ultimo	
		}else if(cI.localOperator().equals("last")){
			// salvo l'ultimo valore
			this.dataToStore.put(cI.getColumnName(), cI);
			
		// controllo se il valore è da sommare	
		}else if(cI.localOperator().equals("sum")){
			// lo sommo ai valori precedenti
			double lastValue = this.dataToStore.get(cI.getColumnName()).getValue();
			double sum = lastValue + cI.getValue();
			this.dataToStore.get(cI.getColumnName()).setValue(sum);
		}
		
		// aggiornamento  dati derivati			
		Enumeration<String> e = this.dataToStore.keys();
		
		while(e.hasMoreElements()){
			String name = e.nextElement();
			//controllo se è un valore derivato
			if(this.dataToStore.get(name).getTarget().equals("formula")){
				// aggiornamento misura derivata
				this.dataToStore.get(name).setValue(this.getDerivedMeasure(this.dataToStore.get(name)));					
			}
		}
	}
	
	// lista capability da salvare
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
	
	// invocato per inserire valori globali sul global record
	public CapabilityInstance getCapabilityInstance(String columnName){
		return this.dataToStore.get(columnName);
	}
	
	// set medie mobili
	public void setMMListToStore(LinkedList<CapabilityInstance> mMListToStore){
		this.mMListToStore = mMListToStore;
	}
	
	public LinkedList<CapabilityInstance> getMMListToStore(){
		return this.mMListToStore;
	}
	
	
	// metodo privato per calcolare misure derivate
	private double getDerivedMeasure(CapabilityInstance cI) {
		double result = 0.00;
		String expression = cI.localOperator();
		// suddivido la stringa in vari tokens
        String[] tokens = expression.split(" ");        
        // sostituisco il nome della capability con il valore        
        for (int i = 0; i < tokens.length; i++) {
        	// se null il token è una parentesi oppure uno dei simboli op (/, *, -, +, ...)
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
		    else if (s.equals("ln")) ops.push(s);
		    else if (s.equals("log10")) ops.push(s);
		    else if (s.equals(")")) {
		        String op = ops.pop();
		        double v = vals.pop();
		        if (op.equals("+")) v = vals.pop() + v;
		        else if (op.equals("-")) v = vals.pop() - v;
		        else if (op.equals("*")) v = vals.pop() * v;
		        else if (op.equals("/")) v = vals.pop() / v;
		        else if (op.equals("sqrt")) v = Math.sqrt(v);
		        else if (op.equals("ln")) v = Math.log(v);
		        else if (op.equals("log10")) v = Math.log10(v);
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
			if(toRet.lastIndexOf(',') == toRet.length() -1){
				toRet = toRet.substring(0, toRet.length() -1);
			}
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
