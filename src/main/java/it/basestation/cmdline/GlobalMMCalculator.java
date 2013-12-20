package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public class GlobalMMCalculator {
	private Hashtable<String, Double[]> avgWindows = new Hashtable<String, Double[]>();
	private Hashtable<String, Integer> avgIndexes = new Hashtable<String, Integer>();
	private Hashtable<String, CapabilityInstance> mobileAvgs = new Hashtable<String, CapabilityInstance>();
	
	public GlobalMMCalculator() {
		LinkedList<Capability> mmList = Configurator.getMMCapabilityList("global");
		for (Capability c : mmList) {
			Double[] avgWindow = new Double[c.getAvgWindow()];
			for (int i = 0; i < avgWindow.length; i++) {
				avgWindow[i] = 0.00;
			}
			this.avgWindows.put(c.getName(), avgWindow);
			this.avgIndexes.put(c.getName(), 0);
			this.mobileAvgs.put(c.getName(), new CapabilityInstance(c.getName(), c.getColumnName(),c.getTarget(), c.localOperator(), c.globalOperator(), c.getMinValue(), c.getMaxValue(), c.getAvgWindow()));
			
		}
	}
	
	public void setListToCalculate(LinkedList<CapabilityInstance> caplistToStore){
		for (CapabilityInstance cI : caplistToStore) {
			if(this.avgWindows.containsKey(cI.getName())){
				Double[] avgWindow = new Double[this.avgWindows.get(cI.getName()).length];
				avgWindow = this.avgWindows.get(cI.getName());
				// debug
				for (Double value : avgWindow) {
					System.out.println("Scorro la lista globalavgWindow: "+cI.getName()+" "+value);
				}
				// end debug
				int avgIndex = this.avgIndexes.get(cI.getName());
				avgWindow[avgIndex] = cI.getValue();
				avgIndex = (avgIndex + 1) % avgWindow.length;
				this.avgWindows.put(cI.getName(), avgWindow);
				this.avgIndexes.put(cI.getName(), avgIndex);
				double sum = 0;
				int counter = 0;
				for (int i = 0; i < avgWindow.length; i++) {
					System.out.println("INDEX= "+i);
					sum += avgWindow[i];
					if(avgWindow[i] != 0)
						counter++;
				}
				this.mobileAvgs.get(cI.getName()).setValue(sum/counter);
			}			
		}
	}
	
	public LinkedList<CapabilityInstance> getMMListToStore(){
		LinkedList<CapabilityInstance> toRet = new LinkedList<CapabilityInstance>();
		Enumeration<String> e = this.mobileAvgs.keys();
		while(e.hasMoreElements()){
			toRet.add(this.mobileAvgs.get(e.nextElement()));
		}
		return toRet;
	}

}
