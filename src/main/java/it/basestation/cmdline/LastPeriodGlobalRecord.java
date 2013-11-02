package it.basestation.cmdline;

import java.util.HashSet;
import java.util.LinkedList;

public class LastPeriodGlobalRecord {
	private LinkedList<Capability> capabilities = new LinkedList<Capability>();
	private HashSet<String> capabilitiesSet = new HashSet<String>();
	public LastPeriodGlobalRecord() {
		this.capabilitiesSet = Configurator.getGlobalCapabilitiesSet();
		for (String capName : this.capabilitiesSet) {
			this.capabilities.add(new Capability(capName));
		}
	}
	
	public void setValue(String capName, double value){
		for (Capability c : this.capabilities) {
			if(capName == c.getName()){
				c.setValue(value);
				break;
			}
		}
	}
	
	public double getValue(String capName){
		double value = 0;
		for (Capability c : this.capabilities) {
			if(capName == c.getName()){
				value = c.getValue();
				break;
			}
		}
		return value;
	}
	
	public void setDerivedMeasures(){
		for (String capName : this.capabilitiesSet) {
			if(Configurator.isADerivedMeasure(capName)){
				DerivedMeasure dM = new DerivedMeasure(capName);
				dM.setDerivedMeasure(this.capabilities);
				setValue(dM.getName(),dM.getValue());
			}
			
		}
		
	}
	

}
