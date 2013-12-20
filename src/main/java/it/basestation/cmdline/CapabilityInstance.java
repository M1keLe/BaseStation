package it.basestation.cmdline;

public class CapabilityInstance extends Capability {
	private double value = 0.00;
	
/*	public CapabilityInstance(String name){
		super(name);
	}
	
	public CapabilityInstance(String name, String localOperator, String globalOperator){
		super(name, localOperator, globalOperator);
	}
		
	public CapabilityInstance(String name, String localOperator, String globalOperator, Double minValue, Double maxValue){
		super(name, localOperator, globalOperator, minValue, maxValue);
	}
	
	public CapabilityInstance(String name, String localOperator, String globalOperator, Double minValue, Double maxValue, int avgWindow){
		super(name, localOperator, globalOperator, minValue, maxValue, avgWindow);
	}
*/	
	public CapabilityInstance(String name, String columnName, String index,
			String localOperator, String globalOperator, double minValue,
			double maxValue, int avgWindow) {
		super(name, columnName, index, localOperator, globalOperator, minValue, maxValue, avgWindow);
	}

	public void setValue(double value){
			this.value = value;
	}
	
	public double getValue(){
		return this.value;
	}
	
	@Override
	public String toString() {
		String toRet = "*********** Capability Instance ***********\n";
		toRet += "Name: " + getName() + "\n";
		toRet += "Column Name: " + getColumnName() + "\n";
		toRet += "Index: " + getTarget() + "\n";
		toRet += "Local: " + localOperator() + "\n";
		toRet += "Global: " + globalOperator() + "\n";
		toRet += "Min Value: " + getMinValue() + "\n";
		toRet += "Max Value: " + getMaxValue() + "\n";
		toRet += "AVG Window: " + getAvgWindow() + "\n";
		toRet += "Value: " + this.value + "\n";
		toRet += "********* End Capability Instance *********\n";
		
		return toRet;
	}
	
}
