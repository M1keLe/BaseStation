package it.basestation.cmdline;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

public class RebootFixer {
	public short nodeID;
	private Hashtable<String, LinkedList<Double>> lastRecordedValues = new Hashtable<String, LinkedList<Double>>();

	public RebootFixer(short nodeID) {
		// TODO Auto-generated constructor stub
		this.nodeID = nodeID;
	}
	
	public double fixReboot(DataContainer dC){
		double newValue = dC.getValue();
		// se il valore  è presente effettuo il controllo se si è verificato un reboot del nodo
		if(this.lastRecordedValues.containsKey(dC.getName())){
			if(!this.lastRecordedValues.get(dC.getName()).isEmpty()){
				double lastValue = this.lastRecordedValues.get(dC.getName()).getLast();
				System.out.println("Capability: "+dC.getName()+" Last Value: " +lastValue+ " New Value: " + newValue );
				if(lastValue > newValue){
					// reboot del nodo aggiorno i valori:
					
					newValue += lastValue;
					//double newValue = lastValue + dC.getValue();
					//dC.setValue(newValue);
					System.out.println("REBOOT DEL NODO! "+ nodeID+" nuovo value di dC: " + newValue);
				}
			}
			this.lastRecordedValues.get(dC.getName()).add(newValue);
		}else{
			// store dell'ultimo valore registrato
			this.lastRecordedValues.put(dC.getName(), new LinkedList<Double>());
			this.lastRecordedValues.get(dC.getName()).add(newValue);
			System.out.println("nessun reboot");
		}
		
		return newValue;
	}
}
