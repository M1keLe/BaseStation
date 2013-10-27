package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class LastPeriodNodeStats implements IStats {
	

	//public void elabNodeLists(Hashtable<Short, LinkedList<Packet>> packetsOfNodes) {
	
	public void elabLastPeriodPacketList(LinkedList<Packet> lastPeriodPacketList){
		Hashtable<Short, LinkedList<Packet>> packetsOfNodes = new Hashtable<Short, LinkedList<Packet>>();
		
		// suddivido la lista dei pacchetti in diverse liste identificate dal nodeID (sender)
		
		Iterator<Packet> iterator = lastPeriodPacketList.iterator();
		while (iterator.hasNext()) {
			Packet p = (Packet) iterator.next();
			short sender = p.getSenderID();
			if(!packetsOfNodes.containsKey(sender)){
				packetsOfNodes.put(sender, new LinkedList<Packet>());
			}
			packetsOfNodes.get(sender).add(p);
		}
		
		// seleziono lista pacchetti di un nodo
		Enumeration<Short> e = packetsOfNodes.keys();
		while (e.hasMoreElements()) {
			Short nodeID =  e.nextElement();
			LinkedList<Packet> packetList = packetsOfNodes.get(nodeID);
			
			// tabella per calcolare le medie dei dati
			Hashtable<String, LinkedList<Double>> capToElab = new Hashtable<String, LinkedList<Double>>();
			
			Iterator<Packet> i = packetList.iterator();
			// scorro lista pacchetti
			while(i.hasNext()){
				//seleziono un pacchetto
				Packet p = i.next();
				// estraggo le capability dal nodo in 2 liste (summable e non)
				LinkedList<Capability> capList = p.getData();
				LinkedList<Capability> summableCapList = p.getSummableData();
				Iterator<Capability> iCap = capList.iterator();
				Iterator<Capability> iteratorSumCap = summableCapList.iterator();
				
				// elaboro le capability non "summable"
				while(iCap.hasNext()){
					// codice nel caso in cui i controlli sui param di config siano effettuati dall'entità che genera i pacchetti 
					// e scarta le capability non "idonee"
					
					Capability c = iCap.next();
					if (!capToElab.containsKey(c.getName())) {
						capToElab.put(c.getName(), new LinkedList<Double>());
					}
					capToElab.get(c.getName()).add(c.getValue());
				} // end while capability not summable
				
				// elaboro le capability "summable"
				while(iteratorSumCap.hasNext()){
					// codice nel caso in cui i controlli sui param di config siano effettuati dall'entità che genera i pacchetti 
					// e scarta le capability non "idonee"
					
					Capability c = iteratorSumCap.next();
					
					// codice da definire
					
				} // end While capability summable
			} // end while iterazione sui pacchetti
			
			// lista di capability da passare al fusion table
			LinkedList<Capability> capList = new LinkedList<Capability>();
			if(!capToElab.isEmpty()){
				capList = elabCapabilities(capToElab);
			}
			// capList.add(summableValues) .... aggiungere alla cpList i valori sommabili
			// FusionTables.insertData(nodeID, capList)
			
			
		} // end while iterazione sui nodi
	} // end metodo elabLastPeriodPacketList
	
	private LinkedList<Capability> elabCapabilities(Hashtable<String, LinkedList<Double>> capabilityTable){
		LinkedList<Capability> toRet = new LinkedList<Capability>();
		Enumeration<String> iKey = capabilityTable.keys();
		while(iKey.hasMoreElements()){
			String capName = iKey.nextElement();
			LinkedList<Double> values = capabilityTable.get(capName);
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
					
					
	/*			//-----------------------------	 NOTE  ---------------------------- // 
					if(!c.hasARange()){
						// inseri
					}
					
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
-------------------------------------------- FINE NOTE  */
				