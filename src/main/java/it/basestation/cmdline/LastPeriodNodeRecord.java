package it.basestation.cmdline;

import java.util.HashSet;
import java.util.LinkedList;

public class LastPeriodNodeRecord {
	private Node n;
	private LinkedList<Capability> capabilities = new LinkedList<Capability>();
	
	public LastPeriodNodeRecord(short nodeID){
		this.n = Configurator.getNode(nodeID);
		HashSet<String> capabilities = n.getCapabilities();
		for (String c : capabilities) {
			if(Configurator.getRangedCapability(c) != null){
				this.capabilities.add(Configurator.getRangedCapability(c));
			}else{
				Capability cap = new Capability(c,Configurator.isGlobalCapability(c), Configurator.isIndirectMeasure(c));
				this.capabilities.add(cap);
			}
		}
	}
	
	public void setValue (Capability capToStore){
		for (Capability c : this.capabilities) {
			if(capToStore.getName() == c.getName()){
				c.setValue(capToStore.getValue());
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
		if(this.n.hasDerivedMeasures()){
			
			HashSet <String> derivedMeasures = this.n.getDerivedMeasures();
			
			for (String dMeasureName : derivedMeasures) {
				DerivedMeasure dM = new DerivedMeasure(dMeasureName);
				dM.setDerivedMeasure(this.capabilities);
				this.setValue(dM);
			}
		}
		
	}
	
	public LinkedList<Capability> getCapabilitiesList(){
		return this.capabilities;
	}

}
