package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

public class LastPeriodGlobalRecord {
	
	// lista debug
	private Hashtable<String, LinkedList<CapabilityInstance>> globalCapabilityInstancesList = new Hashtable<String, LinkedList<CapabilityInstance>>();
	
	private Hashtable<String, CapabilityInstance> globalDataToStore = new Hashtable<String, CapabilityInstance>();
	private Hashtable<String, Integer> counters = new Hashtable<String, Integer>();
	
	public LastPeriodGlobalRecord(){
		LinkedList<String> globalCapabilitiesSet = Configurator.getGlobalCapabilitiesSet();
		for (String s : globalCapabilitiesSet) {
			CapabilityInstance c = Configurator.getCapabilityInstance(s);
			this.globalCapabilityInstancesList.put(s, new LinkedList<CapabilityInstance>());
			this.globalDataToStore.put(s, c);
			if(c.globalOperator().equals("avg")){
				this.counters.put(s, 0);
			}			
		}
		
	}
	
	
	public void addCapabilityInstance(CapabilityInstance cI){
		// aggiorno lista debug
		this.globalCapabilityInstancesList.get(cI.getName()).add(cI);
		// controllo se il valore è da mediare
		if(cI.globalOperator().equals("avg") && cI.getMinValue()< cI.getValue() && cI.getValue() < cI.getMaxValue() ){
			int lastCounter = this.counters.get(cI.getName()).intValue();
			int newCounter = lastCounter +1 ;
			double lastAvg = this.globalDataToStore.get(cI.getName()).getValue();
			double temp = lastAvg * lastCounter;
			temp += cI.getValue();
			double neWAvg = temp / newCounter;
			// aggiornamento valori
			this.counters.put(cI.getName(), newCounter);
			this.globalDataToStore.get(cI.getName()).setValue(neWAvg);
						
		}else if(cI.globalOperator().equals("sum")){
			// faccio la somma dei valori
			double lastValue = this.globalDataToStore.get(cI.getName()).getValue();
			double newValue = lastValue + cI.getValue();
			this.globalDataToStore.get(cI.getName()).setValue(newValue);
		}else if(cI.globalOperator().equals("last")){
			this.globalDataToStore.get(cI.getName()).setValue(cI.getValue());
		}
		// aggiorno dati derivati
		Enumeration<String> e = this.globalDataToStore.keys();
		while(e.hasMoreElements()){
			String name = e.nextElement();
			//controllo se è un valore derivato
			if(!this.globalDataToStore.get(name).globalOperator().equals("avg") &&
					!this.globalDataToStore.get(name).globalOperator().equals("sum") &&
					!this.globalDataToStore.get(name).globalOperator().equals("last")){
					
				this.globalDataToStore.get(name).setValue(this.getDerivedMeasure(this.globalDataToStore.get(name)));
			}
		}
	}
	
	
	private double getDerivedMeasure(CapabilityInstance cI) {
		double result = 0.00;
		String syntax = cI.globalOperator();
		// suddivido la stringa in vari tokens
        String[] tokens = syntax.split(" ");
        
        // sostituisco il nome della capability con il valore
        
        for (int i = 0; i < tokens.length; i++) {
        	if(this.globalDataToStore.get(tokens[i]) != null){
        		Double value = this.globalDataToStore.get(tokens[i]).getValue();
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
	
	public LinkedList<CapabilityInstance> getDataListToStore(){
		LinkedList<CapabilityInstance> toRet = new LinkedList<CapabilityInstance>();
		Enumeration<String> e = this.globalDataToStore.keys();
		while(e.hasMoreElements()){
			String name = e.nextElement();
			toRet.add(this.globalDataToStore.get(name));
		}
		return toRet;
	}
	
	public String toString(){		
		String toRet = "";
		toRet += "\n =====[GLOBAL_RECORD] \n";
		Enumeration<String> e = this.globalCapabilityInstancesList.keys();
		while(e.hasMoreElements()){
			String name = e.nextElement();
			toRet+= "\n["+name+"] ->> [";
			for (CapabilityInstance c : this.globalCapabilityInstancesList.get(name)) {
				toRet+= " " +c.getValue() + ",";
			}
			toRet = toRet.substring(0, toRet.length() -1);
			toRet+= "]\n";
		}
		
		toRet += "\n -------------- Data To Store --------------\n"; 
		//toRet = "\n -------------- Data To Store --------------\n";
		for (CapabilityInstance cI : this.getDataListToStore()) {
			toRet += cI.getName() +": " + cI.getValue() + ", ";
		}
		
		toRet +="\n ====[END_GLOBAL_RECORD] \n";
		
		return toRet;
	}
}