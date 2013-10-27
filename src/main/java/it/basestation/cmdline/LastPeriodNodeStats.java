package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class LastPeriodNodeStats implements IStats {
	

	public void elabNodeLists(Hashtable<Short, LinkedList<Packet>> packetsOfNodes) {
		// seleziono lista pacchetti di un nodo
		Enumeration<Short> e = packetsOfNodes.keys();
		while (e.hasMoreElements()) {
			Short nodeID =  e.nextElement();
			LinkedList<Packet> packetList = packetsOfNodes.get(nodeID);
			Hashtable<String, LinkedList<Double>> newCapabilityTable = new Hashtable<String, LinkedList<Double>>();
			Iterator<Packet> i = packetList.iterator();
			// scorro lista pacchetti
			while(i.hasNext()){
				//seleziono un pacchetto
				Packet p = i.next();
				// estraggo le capability dal nodo
				LinkedList<Capability> capList = p.getData();
				Iterator<Capability> iCap = capList.iterator();
				// elaboro le capability
				while(iCap.hasNext()){
					
					Capability c = iCap.next();
					
					// controllo se è stato configurato un range e vedo se lo "rispetta"
					if(Configurator.getRangedCapability(c.getName()) != null){
						Capability rangedC = Configurator.getRangedCapability(c.getName());
						double minValue = rangedC.getMinValue();
						double maxValue = rangedC.getMaxValue();
						if(c.getValue() >= minValue && c.getValue()<= maxValue){							
							if(Configurator.isSummableCapability(c.getName())){
								// applicare operazioni per cap summable.
							}else{
								if (!newCapabilityTable.containsKey(c.getName())) {
									newCapabilityTable.put(c.getName(), new LinkedList<Double>());
								}
								newCapabilityTable.get(c.getName()).add(c.getValue());
							}
						}
						
					}else{
						if(Configurator.isSummableCapability(c.getName())){
							// applicare operazioni per cap summable.
						}else{
							if (!newCapabilityTable.containsKey(c.getName())) {
								newCapabilityTable.put(c.getName(), new LinkedList<Double>());
							}
							newCapabilityTable.get(c.getName()).add(c.getValue());
						}
					}
				}
			
			}
			// calcolare medie dalla lista new
		}
		
	}

}
