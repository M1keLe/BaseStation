package it.basestation.cmdline;

import java.util.LinkedList;
import java.util.Stack;

public class LastPeriodGlobalRecord {
	
	private String name = "";
	private DataContainer capabilityToStore;// = new Capability("");
	private LinkedList<DataContainer> capabilityToElab = new LinkedList<DataContainer>();
	private String globalOperator = "";
	
	public LastPeriodGlobalRecord(String  capabilityName, LinkedList <DataContainer> capabilityToElab){
		this.name = capabilityName;
		this.capabilityToStore = Configurator.getDataContainerByName(capabilityName);
		this.globalOperator = this.capabilityToStore.globalOperator();
		this.capabilityToElab = capabilityToElab;
	}
	
	public String getName(){	
		return this.name;
	}
	
	public DataContainer getCapabilityToStore(){
		if(this.globalOperator.equals("avg")){
			this.capabilityToStore.setValue(getAvg(this.name));
			
		}else if(this.globalOperator.equals("sum")){
			this.capabilityToStore.setValue(getSumOfValues(this.name));
			
		}else{
			this.capabilityToStore.setValue(getDerivedMeasure());
		}
		
		if(this.capabilityToStore.getValue() < this.capabilityToStore.getMinValue()||
				this.capabilityToStore.getMaxValue() < this.capabilityToStore.getValue()){
			
			this.capabilityToStore.setValue(0.00);
		}
		
		return this.capabilityToStore;
	}

	@Override
	public String toString(){
		return getCapabilityToStore().toString();
	}
	
	
	private double getDerivedMeasure() {
		
		double result = 0.00;
		// lista per non ripere la somma sulle capability con stesso nome
		LinkedList<String> done = new LinkedList<String>();
		//System.out.println("DEBUG: Stringa da splittare: ->"+this.globalOperator);
		String[] tokens = this.globalOperator.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			System.out.println("DEBUG: Tocken n° "+ i + " = " + tokens[i]);
			for (Capability cap : this.capabilityToElab) {
				
				//if(tokens[i].equals(cap.getName())){  
				if(tokens[i].equals(cap.getName()) && !done.contains(cap.getName())){
					Double value = getSumOfValues(cap.getName());
					
					//System.out.println("DEBUG: Sto trasformando il token numero "+i+" con valore "+value+ " in stringa");
					tokens[i] = value.toString();
					//System.out.println("DEBUG: Modificato il token numero "+i+ "in " + tokens[i]);
					done.add(cap.getName());
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
		    }else{
		    	//System.out.println("+-+-+--+-+-+-+-++-+-+-+-+-+-+-+-+-+-+-DijkstraTwoStack: Val to push: " +s );
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

	private double getSumOfValues(String name) {
		double toRet = 0.00;
		for (DataContainer c : this.capabilityToElab) {
			if(c.getName().equals(name)){
				toRet+= c.getValue();
			}
		}
		return toRet;
	}

	private double getAvg(String name) {
		double toRet = 0.00;
		double value = 0.00;
		int counter = 0;
		
		for (DataContainer c : this.capabilityToElab) {
			if(c.getName().equals(name)){
				if(c.getMinValue()<c.getValue() && c.getValue()<c.getMaxValue()){
					value += c.getValue();
					counter++;
				}
				if(counter>0){
					toRet = value/counter;
				}
			}
		}
		return toRet;
	}

	
}