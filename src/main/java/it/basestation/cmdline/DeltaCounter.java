package it.basestation.cmdline;

import java.util.Hashtable;
// non utilizzata
public class DeltaCounter {
	private Hashtable<String, Double> lastRecordedValues = new Hashtable<String, Double>();

	public DeltaCounter() {
		// TODO Auto-generated constructor stub
	}
	
	public void elabDelta(CapabilityInstance cI){
		if(!this.lastRecordedValues.containsKey(cI.getName())){
			this.lastRecordedValues.put(cI.getName(), cI.getValue());
			cI.setValue(0);
		}else{
			double lastValue = this.lastRecordedValues.get(cI.getName());
			double newValue = cI.getValue();
			if(lastValue <= newValue){ // nessun reboot del nodo
				double delta = newValue - lastValue;
				cI.setValue(delta);
				this.lastRecordedValues.put(cI.getName(), newValue);
			}else{ // reboot del nodo
				
				this.lastRecordedValues.put(cI.getName(), cI.getValue());
			}
		}
	}
}
