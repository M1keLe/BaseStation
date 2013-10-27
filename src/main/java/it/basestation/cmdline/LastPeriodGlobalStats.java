package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public class LastPeriodGlobalStats implements IStats {

	@Override
	// public void elabNodeLists(Hashtable<Short, LinkedList<Packet>> packetsOfNodes) {
	public void elabLastPeriodPacketList(LinkedList<Packet> lastPeriodPacketList){
		Hashtable<String, LinkedList<Double>> capToElab = new Hashtable<String, LinkedList<Double>>();
		
		// sto analizzando solo le cap non sommabili (da implementare)
		for (Packet p : lastPeriodPacketList) {
			LinkedList <Capability> c = p.getData();
			for (Capability cap : c) {
				if(cap.isGlobal()){
					if(!capToElab.containsKey(cap.getName())){
						capToElab.put(cap.getName(), new LinkedList<Double>());
					}
					capToElab.get(cap.getName()).add(cap.getValue());					
				}
			} // end for lista capab
			
		} // end for lista pacchetti
		
		LinkedList<Capability> capList = new LinkedList<Capability>();
		if(!capToElab.isEmpty()){
			capList = elabCapabilities(capToElab);
		}
		// capList.add(summableValues) .... aggiungere alla capList i valori sommabili
		// FusionTables.insertDataToGlobalTable(capList)
		
			
		
	}
	
	private LinkedList<Capability> elabCapabilities(Hashtable<String, LinkedList<Double>> capToElab){
		LinkedList<Capability> toRet = new LinkedList<Capability>();
		Enumeration<String> iKey = capToElab.keys();
		while(iKey.hasMoreElements()){
			String capName = iKey.nextElement();
			LinkedList<Double> values = capToElab.get(capName);
			int factor = 0;
			double tot = 0.00;
			while(!values.isEmpty()){ // controllare se corretto
				tot += values.poll();
				factor++;
			}
			tot = tot/factor;
			Capability c = new Capability(capName);
			c.setValue(tot);
			toRet.add(c);
			
		} // end while che cicla su liste double
		return toRet;
	}
}
