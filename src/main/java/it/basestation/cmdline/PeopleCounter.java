package it.basestation.cmdline;

import java.util.Hashtable;

public class PeopleCounter {
	private Hashtable<String, Double> lastRecordedValues = new Hashtable<String, Double>();
	private Hashtable<String, Double> delta = new Hashtable<String, Double>();

	
	public void elabCapabilityInstance(CapabilityInstance cI){
		if(!this.lastRecordedValues.containsKey(cI.getName())){
			this.lastRecordedValues.put(cI.getName(), cI.getValue());
			this.delta.put(cI.getName(), 0.00);
			cI.setValue(0);
		}else{
			double lastValue = this.lastRecordedValues.get(cI.getName());
			double newValue = cI.getValue();
			double newDelta;
			if(lastValue <= newValue){ // nessun reboot del nodo
				double d = newValue - lastValue;
				newDelta =  d + this.delta.get(cI.getName());
				this.lastRecordedValues.put(cI.getName(), newValue);
			}else{ // reboot del nodo
				this.lastRecordedValues.put(cI.getName(), cI.getValue());
				newDelta = this.delta.get(cI.getName()) + cI.getValue();
			}
			this.delta.put(cI.getName(), newDelta);
			cI.setValue(newDelta);
		}
	}
}
