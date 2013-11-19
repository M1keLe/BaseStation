package it.basestation.cmdline;

import java.util.Hashtable;

public class DeltaCounter {
	public short nodeID;
	private Hashtable<String, Double> lastRecordedValues = new Hashtable<String, Double>();
	//private Hashtable<String, Double> sumOfDelta = new Hashtable<String, Double>();
	public DeltaCounter(short nodeID) {
		// TODO Auto-generated constructor stub
		this.nodeID = nodeID;
	}
	
	public void elabDelta(CapabilityInstance cI){
		if(!this.lastRecordedValues.containsKey(cI.getName())){
			this.lastRecordedValues.put(cI.getName(), cI.getValue());
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
