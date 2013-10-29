package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public class LastPeriodGlobalStats implements IStats {

	@Override
	// public void elabNodeLists(Hashtable<Short, LinkedList<Packet>> packetsOfNodes) {
	public void elabLastPeriodPacketList(LinkedList<Packet> lastPeriodPacketList){
		Hashtable<String, LinkedList<Double>> capToElab = new Hashtable<String, LinkedList<Double>>();
		
		// analizzo le cap non sommabili 
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
		
		// aggiungo alla lista di capabilities da elaborare quelle sommabili e globali dell'ultimo periodo
		LinkedList<Capability> summableAndGlobalCapList = LocalStatsContainer.getLastSummableAndGlobalCapabilities();
		if(!summableAndGlobalCapList.isEmpty()){
			for (Capability c : summableAndGlobalCapList) {
				if(!capToElab.containsKey(c.getName())){
					capToElab.put(c.getName(), new LinkedList<Double>());
				}
				capToElab.get(c.getName()).add(c.getValue());
			}
		}
		
		if(!capToElab.isEmpty()){
			LinkedList<Capability> capToStore = elabCapabilities(capToElab);
			
			// FusionTables.insertDataToGlobalTable(capToStore)
		}
	}
	
	private LinkedList<Capability> elabCapabilities(Hashtable<String, LinkedList<Double>> capabilityTable){
		LinkedList<Capability> toRet = new LinkedList<Capability>();
		Enumeration<String> iKey = capabilityTable.keys();
		while(iKey.hasMoreElements()){
			String capName = iKey.nextElement();
			LinkedList<Double> values = capabilityTable.get(capName);
			// la lista non deve essere vuota
			if(!values.isEmpty()){
				int numOfSamples = 0;
				double tot = 0.00;
				for (Double value : values) {
					tot+= value;
					numOfSamples++;
				}
				// calcolo media
				tot = tot/numOfSamples;
				// creo capability
				Capability c = new Capability(capName);
				c.setValue(tot);
				// aggiungo la capability alla lista toRet
				toRet.add(c);
			} // end if
		} // end while che cicla su liste double
		return toRet;
	}
}
