package it.basestation.cmdline;

import java.util.Hashtable;
import java.util.LinkedList;

public class RebootFixer {
	public short nodeID;
	private Hashtable<String, LinkedList<Double>> lastRecordedValues = new Hashtable<String, LinkedList<Double>>();

	public RebootFixer(short nodeID) {
		// TODO Auto-generated constructor stub
		this.nodeID = nodeID;
	}
	
	public void fixReboot(CapabilityInstance dC){
		double newValue = dC.getValue();
		// se il valore  è presente effettuo il controllo se si è verificato un reboot del nodo
		if(!this.lastRecordedValues.containsKey(dC.getName())){
			this.lastRecordedValues.put(dC.getName(), new LinkedList<Double>());
			
		}else if(this.lastRecordedValues.containsKey(dC.getName()) && !this.lastRecordedValues.get(dC.getName()).isEmpty()){
			
			double lastValue = this.lastRecordedValues.get(dC.getName()).removeLast();
			if(lastValue > newValue){
				newValue += lastValue;
			}
			
		}
		this.lastRecordedValues.get(dC.getName()).add(newValue);
		dC.setValue(newValue);
		//return newValue;
	}
}
