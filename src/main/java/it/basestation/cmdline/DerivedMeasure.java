package it.basestation.cmdline;

import java.util.LinkedList;
import java.util.Stack;

public class DerivedMeasure extends Capability {
	private String syntax = "";

	public DerivedMeasure(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public DerivedMeasure(String name, boolean isGlobal, boolean isIndirect) {
		super(name, isGlobal, isIndirect);
		// TODO Auto-generated constructor stub
	}
	
	public void setDerivedMeasure(LinkedList<Capability> capabilityList){
		
		this.syntax = Configurator.getDerivedMeasureSyntax(this.getName());
		if(this.syntax != null){
			// suddivido la stringa in vari tokens
			String[] arrayString = this.syntax.split(" ");
			// sostituisco il nome della capability con il valore
			for (String string : arrayString) {
				for (Capability c : capabilityList) {
					if(string == c.getName()){
						string = "" + c.getValue();
					}
				}
			}
			// calcolo il valore derivato
			double value = dijkstraTwoStack(arrayString);
			// imposto il valore
			this.setValue(value);
		}else{
			System.out.println("Impossibile calcolare questa misura ("+this.getName()+"). Nessuna regola impostata.");
			System.out.println("Controllare il file di configurazione.");
		}
		
	}

	// metodi privati
	
	
	private double dijkstraTwoStack(String[] arrayString) {
		Stack<String> ops = new Stack<String>();
		Stack<Double> vals = new Stack<Double>();
		double result = -1.00;
		for (String s : arrayString) {
			if      (s.equals("("))               ;
            else if (s.equals("+"))    ops.push(s);
            else if (s.equals("-"))    ops.push(s);
            else if (s.equals("*"))    ops.push(s);
            else if (s.equals("/"))    ops.push(s);
            else if (s.equals("sqrt")) ops.push(s);
            else if (s.equals(")")) {
                String op = ops.pop();
                double v = vals.pop();
                if      (op.equals("+"))    v = vals.pop() + v;
                else if (op.equals("-"))    v = vals.pop() - v;
                else if (op.equals("*"))    v = vals.pop() * v;
                else if (op.equals("/"))    v = vals.pop() / v;
                else if (op.equals("sqrt")) v = Math.sqrt(v);
                vals.push(v);
            }
            else vals.push(Double.parseDouble(s));
		}
		if(!vals.isEmpty()){
			result = vals.pop();
		}else{
			System.out.println("DijkstraTwoStack ERROR: impossibile impostare il valore di questa misura");
		}
		return result;
	}

}
