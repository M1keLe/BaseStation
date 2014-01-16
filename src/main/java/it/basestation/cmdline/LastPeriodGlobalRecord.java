package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

public class LastPeriodGlobalRecord {
	
	
	private Hashtable<String, LinkedList<CapabilityInstance>> globalCapabilityInstancesList = new Hashtable<String, LinkedList<CapabilityInstance>>();
	
	private Hashtable<String, CapabilityInstance> globalDataToStore = new Hashtable<String, CapabilityInstance>();
	private LinkedList<CapabilityInstance> globalMMList = new LinkedList<CapabilityInstance>();
	private Hashtable<String, Integer> counters = new Hashtable<String, Integer>();
	
	public LastPeriodGlobalRecord(){
		LinkedList<Capability> globalCapabilitiesList = Configurator.getGlobalCapabilitiesList(false);
		for (Capability c : globalCapabilitiesList) {
			// lista debug
			this.globalCapabilityInstancesList.put(c.getColumnName(), new LinkedList<CapabilityInstance>());
			// capabilities globali
			this.globalDataToStore.put(c.getColumnName(), new CapabilityInstance(c.getName(), c.getColumnName(),c.getTarget(), c.localOperator(), c.globalOperator(), c.getMinValue(), c.getMaxValue(), c.getAvgWindow()));
			// contatore per calcolo media
			if(c.globalOperator().equals("avg")){
				this.counters.put(c.getColumnName(), 0);
			}			
		}		
	}
	
	
	public void addCapabilityInstance(CapabilityInstance cI){
		
		// controllo su min e max value
		if(cI.getMinValue()<= cI.getValue() && cI.getValue() <= cI.getMaxValue() ){
			
			this.globalCapabilityInstancesList.get(cI.getColumnName()).add(cI);
			
			// controllo se il valore è da mediare
			if(cI.globalOperator().equals("avg")){
				int lastCounter = this.counters.get(cI.getColumnName()).intValue();
				int newCounter = lastCounter +1 ;
				double lastAvg = this.globalDataToStore.get(cI.getColumnName()).getValue();
				double temp = lastAvg * lastCounter;
				temp += cI.getValue();
				double neWAvg = temp / newCounter;
				// aggiornamento valori
				this.counters.put(cI.getColumnName(), newCounter);
				this.globalDataToStore.get(cI.getColumnName()).setValue(neWAvg);
				
			// controllo se il valore è da sommare
			}else if(cI.globalOperator().equals("sum")){
				// faccio la somma dei valori
				double lastValue = this.globalDataToStore.get(cI.getColumnName()).getValue();
				double newValue = lastValue + cI.getValue();
				this.globalDataToStore.get(cI.getColumnName()).setValue(newValue);
				
			// controllo se devo prendere l'ultimo valore
			}else if(cI.globalOperator().equals("last")){
				this.globalDataToStore.get(cI.getColumnName()).setValue(cI.getValue());
				
			// controllo se devo calcolare la deviazione standard
			}else if(cI.globalOperator().equals("stddev")){
				// x ora tutti i valori sono già salvati nella lista di debug
				//this.globalCapabilityInstancesList.get(cI.getColumnName()).add(cI);
				CapabilityInstance stdDev = this.globalDataToStore.get(cI.getColumnName());
				stdDev.setValue(this.getStdDev(cI.getColumnName()));
				this.globalDataToStore.put(cI.getColumnName(), stdDev);
			}
			
			// aggiorno dati derivati e stddev
			Enumeration<String> e = this.globalDataToStore.keys();
			while(e.hasMoreElements()){
				String name = e.nextElement();
				//controllo se è un valore derivato
				//if(!this.globalDataToStore.get(name).globalOperator().equals("avg") && !this.globalDataToStore.get(name).globalOperator().equals("sum") && !this.globalDataToStore.get(name).globalOperator().equals("last")){
				if(this.globalDataToStore.get(name).getTarget().equals("formula")){		
					this.globalDataToStore.get(name).setValue(this.getDerivedMeasure(this.globalDataToStore.get(name)));
				}
				//if(!this.globalDataToStore.get(name).globalOperator().equals("stddev")){
				//	this.globalDataToStore.get(name).setValue(this.getStandardDeviation(name));
				//}
			}
		} // end if min max value
	}
	
	
	private double getDerivedMeasure(CapabilityInstance cI) {
		double result = 0.00;
		String syntax = cI.globalOperator();
		// suddivido la stringa in vari tokens
        String[] tokens = syntax.split(" ");
        
        // sostituisco il nome della capability con il valore
        
        for (int i = 0; i < tokens.length; i++) {
        	// se il token restituisce null non è una capability ma una parentesi o un simbolo (/,*,-,+)
        	//if(this.globalDataToStore.get(tokens[i]+"_"+cI.getIndex()) != null){
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
	
	public LinkedList<CapabilityInstance> getDataListToStore(){
		LinkedList<CapabilityInstance> toRet = new LinkedList<CapabilityInstance>();
		Enumeration<String> e = this.globalDataToStore.keys();
		while(e.hasMoreElements()){
			String name = e.nextElement();
			toRet.add(this.globalDataToStore.get(name));
		}
		return toRet;
	}
	
	public Hashtable<String, LinkedList<CapabilityInstance>> getHashTableToStore(){
		Hashtable<String, LinkedList<CapabilityInstance>> toRet = new Hashtable<String, LinkedList<CapabilityInstance>>(); 
		Enumeration<String> e = this.globalDataToStore.keys();
		while(e.hasMoreElements()){
			String localName = e.nextElement();
			String globalName = this.globalDataToStore.get(localName).getName();
			if(!toRet.containsKey(globalName)){
				toRet.put(globalName, new LinkedList<CapabilityInstance>());
			}
			toRet.get(globalName).add(this.globalDataToStore.get(localName));
		}
		return toRet;
	}
	
/*	public double getStandardDeviation(String name) {
		double variance = 0.00;
		// media
		Double avg = this.globalDataToStore.get(name).getValue();
		// lista campioni
		LinkedList<CapabilityInstance> samples = this.globalCapabilityInstancesList.get(name);
		
		if(samples != null && avg != null && samples.size()>0){
			double temp = 0.00;
			for (CapabilityInstance cI : samples) {
				temp += (cI.getValue() - avg)*(cI.getValue() - avg); 
			}
			variance = temp/samples.size();
		}
		
		return Math.sqrt(variance);
	}
*/	
	private double getStdDev(String name) {
		double variance = 0.00;
		// media
		//Double avg = this.globalDataToStore.get(name).getValue();
		// lista campioni
		LinkedList<CapabilityInstance> samples = this.globalCapabilityInstancesList.get(name);
		double avg = 0.0;
		double sum = 0.0;
		int counter = 0;
		// calcolo media
		if(samples != null){
			for (CapabilityInstance cI : samples) {
				sum += cI.getValue();
				counter++;
			}
			if (counter > 0)
				avg = sum/counter;
		//}
		
		
		//if(samples != null && avg > 0 && samples.size()>0){
			double temp = 0.00;
			for (CapabilityInstance cI : samples) {
				temp += (cI.getValue() - avg)*(cI.getValue() - avg); 
			}
			variance = temp/counter;
		}
		
		return Math.sqrt(variance);
	}
	
	public void setMMListToStore(LinkedList<CapabilityInstance> mmListToStore) {
		this.globalMMList = mmListToStore; 
		
	}
	
	public LinkedList<CapabilityInstance> getMMListToStore(){
		return this.globalMMList;
	}


	// try to fix peopleInside 
	public double getPeopleInsideValue() {
		double toRet = 0;
		CapabilityInstance peopleInside = this.globalDataToStore.get("PeopleInside_C_Raffaello"); // Inserirci il nome colonna
		if(peopleInside != null){
			toRet = peopleInside.getValue();
		}
		return toRet;
	}
	
	
	@Override
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
			if(toRet.lastIndexOf(',') == toRet.length() -1){
				toRet = toRet.substring(0, toRet.length() -1);
			}
			toRet+= "]\n";
		}
		
		toRet += "\n -------------- Data To Store --------------\n"; 
		//toRet = "\n -------------- Data To Store --------------\n";
		for (CapabilityInstance cI : this.getDataListToStore()) {
			toRet += cI.getColumnName() +": " + cI.getValue() + ", ";
		}
		
		toRet +="\n ====[END_GLOBAL_RECORD] \n";
		
		return toRet;
	}
}