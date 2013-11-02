package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class LastPeriodNodeStats implements IStats {
	

	//public void elabNodeLists(Hashtable<Short, LinkedList<Packet>> packetsOfNodes) {
	
	@Override
	public void elabLastPeriodPacketList(LinkedList<Packet> lastPeriodPacketList){
		Hashtable<Short, LinkedList<Packet>> packetsOfNodes = new Hashtable<Short, LinkedList<Packet>>();
		// lista cap sommabili e globali
		LinkedList<Capability> sumAndGlobCapabilitiesList = new LinkedList<Capability>(); 
		// suddivido la lista dei pacchetti in diverse liste identificate dal nodeID (sender)
		for (Packet p : lastPeriodPacketList) {
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
			// estraggo la lista di pacchetti da elaborare
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
				LinkedList<Capability> summableCapList = p.getIndirectMeasures();
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
				
				// lista di capability da passare al FusionTableManager
				LinkedList<Capability> capListToStore = new LinkedList<Capability>();
				// il metodo elabCapabilities calcola le medie
				if(!capToElab.isEmpty()){
					capListToStore = elabCapabilities(capToElab);
				}
				
				// elaboro le capability "summable" se non vuota
				if(!summableCapList.isEmpty()){
					
					while(iteratorSumCap.hasNext()){
						// codice nel caso in cui i controlli sui param di config siano effettuati dall'entità che genera i pacchetti 
						// e scarta le capability non "idonee"
						// seleziono la capability
						Capability c = iteratorSumCap.next();
						// prendo il delta
						Double delta = LocalStatsManager.getCapabilityDelta(nodeID, c.getName(), c.getValue());
						// faccio la differenza e setto il nuovo valore
						c.setValue(c.getValue() - delta);
						
						// aggiungo la capability alla lista
						capListToStore.add(c);
						// se globale la aggiungo alla lista di cap globali
						if(c.isGlobal()){
							sumAndGlobCapabilitiesList.add(c);
						}

					} // end While capability summable
					
				} // end if
			} // end while iterazione sui pacchetti
			
			// passo la caplListToStore al FusionTablesManager che effettua l'aggiornamento sulle fusionTables
			// FusionTablesManager.insertData(nodeID, capListToStore)
			
		} // end while iterazione sui nodi
		// aggiorno le ultime cap globali e sommabili
		LocalStatsManager.setLastSummableAndGlobalCapabilities(sumAndGlobCapabilitiesList);
	} // end metodo elabLastPeriodPacketList
	
	private LinkedList<Capability> elabCapabilities(Hashtable<String, LinkedList<Double>> capabilityTable){
		// lista di capability da ritornare
		LinkedList<Capability> toRet = new LinkedList<Capability>();
		// seleziono le liste di double da mediare per ogni capability
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
				