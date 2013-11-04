package it.basestation.cmdline;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public class DataProcessor extends Thread {
	
	
	private Hashtable<Short, LastPeriodNodeRecord> lastPeriodNodesRecord = new Hashtable<Short, LastPeriodNodeRecord>();
	 
	private Hashtable<Short, LinkedList<Capability>> lastRecordedValues = new Hashtable<Short, LinkedList<Capability>>();
	
	// non ancora utilizzato
	private LastPeriodGlobalRecord lastPeriodGlobalRecord = new LastPeriodGlobalRecord();

	@Override
	public void run(){

		while (true) {
			try {
				System.out.println("Data Processor in esecuzione " + new Date());
				// imposto la frequenza di update
				Thread.sleep(Configurator.getFreqDataProcessor());
				
				// Switch puntatori liste e prendo la lista di nodi da elaborare
				LinkedList <Packet> packetsList = LocalStatsManager.getLastPeriodPacketsList();
				// hashtable contenente liste pacchetti suddivise per nodo
				Hashtable <Short, LinkedList<Packet>> packetsOfNodes = new Hashtable<Short, LinkedList<Packet>>(); 
				// se la lista non è vuota
				if(!packetsList.isEmpty()){
					// suddivido i pacchetti in base al sender
					for (Packet p : packetsList) {
						short nodeID = p.getSenderID();
						if(!packetsOfNodes.containsKey(nodeID)){
							packetsOfNodes.put(nodeID, new LinkedList<Packet>());
						}
						packetsOfNodes.get(nodeID).add(p);
					}// end foreach (pacchetti suddivisi in liste separate)
					
					// elaboro le statistiche riferite ad ogni nodo
					Enumeration<Short> e = packetsOfNodes.keys();
					while(e.hasMoreElements()){
						short nodeID = e.nextElement();
						// creo record relativo al nodo
						LastPeriodNodeRecord newNodeRecord = new LastPeriodNodeRecord(nodeID);
						// prendo la lista di pacchetti da gestire
						LinkedList<Packet> packetList = packetsOfNodes.get(nodeID);
						// hashtable di double da "mediare"
						Hashtable<String, LinkedList<Double>> capToElab = new Hashtable<String, LinkedList<Double>>();
						Hashtable<String, LinkedList<Double>> indirectMeasuresToElab = new Hashtable<String, LinkedList<Double>>();
						// per ogni pacchetto salvo i dati delle capability da elaborare
						for (Packet p : packetList) {
							LinkedList<Capability> capList = p.getData();
							LinkedList<Capability> indirectMeasures = p.getIndirectMeasures();
							for (Capability c : capList) {
								if(!capToElab.containsKey(c.getName())){
									capToElab.put(c.getName(),  new LinkedList<Double>());
								}
								capToElab.get(c.getName()).add(c.getValue());
							} // end for che cicla sulle capability
							
							for (Capability c : indirectMeasures) {
								if(!indirectMeasuresToElab.containsKey(c.getName())){
									indirectMeasuresToElab.put(c.getName(),  new LinkedList<Double>());
								}
								
								indirectMeasuresToElab.get(c.getName()).add(getIndirectMeasureToStore(nodeID, c));
								
							}// end for che cicla sulle misure "indirette"
						}// end for che cicla sulla lista pacchetti
						// calcolo le medie
						LinkedList<Capability> capToStore = computeTheAverage(capToElab);
						LinkedList<Capability> capToStore2 = elabIndirectMeasures(indirectMeasuresToElab);
																		
						// setto i valori nel node record
						for (Capability c : capToStore) {
							newNodeRecord.setValue(c);
						}
						
						for (Capability c : capToStore2) {
							newNodeRecord.setValue(c);
						}
						// calcolo le grandezze derivate
						newNodeRecord.setDerivedMeasures();
						
						lastPeriodNodesRecord.put(nodeID, newNodeRecord);
						//FusionTablesManager.insertData();
						
						
					}// end while che cicla sulla hashtable
					
					// calcolo delle grandezze globali
					
					Hashtable<String, LinkedList<Double>> globalCapToElab = new Hashtable<String, LinkedList<Double>>();
					Hashtable<String, LinkedList<Double>> globalIndirectMeasuresToElab = new Hashtable<String, LinkedList<Double>>();
					
					for (Packet p : packetsList) {
						LinkedList<Capability> capList = p.getData();
						for (Capability c : capList) {
							if(c.isGlobal()){
								if(!globalCapToElab.containsKey(c.getName())){
									globalCapToElab.put(c.getName(), new LinkedList<Double>());
								}
								globalCapToElab.get(c.getName()).add(c.getValue());
							}
						}
					}
					
					Enumeration<Short> nodes = this.lastPeriodNodesRecord.keys();
					while (nodes.hasMoreElements()) {
						Short nodeID = (Short) nodes.nextElement();
						LastPeriodNodeRecord r = this.lastPeriodNodesRecord.get(nodeID);
						LinkedList<Capability> capList = r.getCapabilitiesList();
						for (Capability c : capList) {
							if(c.isGlobal() && c.isIndirect()){
								if(!globalIndirectMeasuresToElab.containsKey(c.getName())){
									globalIndirectMeasuresToElab.put(c.getName(), new LinkedList<Double>());
								}
								globalIndirectMeasuresToElab.get(c.getName()).add(c.getValue());
							}
						} // end for che cicla sulle capabilities
					} // end while che cicla sulle chiavi
					
					// calcolo le medie globali
					LinkedList<Capability> globalCapToStore = computeTheAverage(globalCapToElab);
					// sommo le misure indirette
					LinkedList<Capability> globalCapToMerge = elabIndirectMeasures(globalIndirectMeasuresToElab);
					
					// fondo le due liste
					for (Capability c : globalCapToMerge) {
						globalCapToStore.add(c);
					}
					
					// creo il record globale
					LastPeriodGlobalRecord newLPGR = new LastPeriodGlobalRecord();
					
					// aggiorno i valori del record globale
					for (Capability c : globalCapToStore) {
						newLPGR.setValue(c.getName(), c.getValue());
					}
					
					// calcolo le misure derivate nel record globale
					newLPGR.setDerivedMeasures();
					
					// salvo il record globale
					this.lastPeriodGlobalRecord = newLPGR;
					
					// effettuo gli aggiornamenti sulle fusion tables 
					// FusionTableManager.insertDataToGlobalTable()
					
									
				}else{
					System.out.println("Nessun pacchetto da gestire");
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	private LinkedList<Capability> computeTheAverage(Hashtable<String, LinkedList<Double>> capToElab){
		// lista di capability da ritornare
		LinkedList<Capability> toRet = new LinkedList<Capability>();
		// seleziono le liste di double da mediare per ogni capability
		Enumeration<String> iKey = capToElab.keys();
		while(iKey.hasMoreElements()){
			String capName = iKey.nextElement();
			LinkedList<Double> values = capToElab.get(capName);
			// la lista non deve essere vuota
			if(!values.isEmpty()){
				int numOfSamples = 0;
				double tot = 0.00;
				for (Double value : values) {
					// i valori da mediare sono quelli che rispettano il range
					// se il range non è rispettato il valore della capability è impostato a -1
					if(value >= 0.00){
						tot+= value;
						numOfSamples++;
					}
				}
				// calcolo media
				if(numOfSamples > 0){
					tot = tot/numOfSamples;
				}else{
					tot = 0.00;
				}
				// creo capability
				Capability c = new Capability(capName);
				c.setValue(tot);
				// aggiungo la capability alla lista toRet
				toRet.add(c);
			} // end if
		} // end while che cicla su liste double
		return toRet;
	}
	
	private double getIndirectMeasureToStore(short nodeID, Capability capability){
		double valueToStore = 0.00;
		LinkedList<Capability> capList = this.lastRecordedValues.get(nodeID);
		if(capList == null){
			this.lastRecordedValues.put(nodeID, new LinkedList<Capability>());
			this.lastRecordedValues.get(nodeID).add(capability);
			valueToStore = capability.getValue();
		}else{
			boolean finded = false;
			for (Capability oldCapability : capList) {
				if(oldCapability.getName() == capability.getName()){
					finded = true;
					double lastValue = oldCapability.getValue();
					oldCapability.setValue(capability.getValue());
					valueToStore = capability.getValue() - lastValue;
					if(valueToStore < 0){
						valueToStore = capability.getValue();
					}
					this.lastRecordedValues.put(nodeID, capList);
					break;
				}
			}
			if(!finded){
				this.lastRecordedValues.get(nodeID).add(capability);
				valueToStore = capability.getValue();
			}
		}
		
		return valueToStore;
	}
	
	private LinkedList<Capability> elabIndirectMeasures(Hashtable<String, LinkedList<Double>> indirectMeasuresToElab){
		LinkedList<Capability> toRet = new LinkedList<Capability>();
		Enumeration<String> iKey = indirectMeasuresToElab.keys();
		while(iKey.hasMoreElements()){
			String capName = iKey.nextElement();
			LinkedList<Double> listToElab = indirectMeasuresToElab.get(capName);
			double result = 0.00;
			for (Double value : listToElab) {
				result += value;
			}
			Capability c = new Capability(capName);
			c.setValue(result);
			toRet.add(c);
		}
		
		return toRet;
	}
}
				// ***************************************************************************************************
				
				/* 	 COMPITI DEL THREAD:
				 * - scrivere log su file
				 * - fare medie su ultimo periodo locali
				 * - calcolare grandezze globali
				 * - aggiornare le fusion talbles
				 */
				