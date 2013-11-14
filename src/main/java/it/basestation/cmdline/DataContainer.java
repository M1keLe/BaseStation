package it.basestation.cmdline;

public class DataContainer extends Capability {
	private double value = 0.00;
	
	public DataContainer(String name){
		super(name);
	}
	
	public DataContainer(String name, String localOperator, String globalOperator){
		super(name, localOperator, globalOperator);
	}
	
	public DataContainer(String name, String sensorID, boolean fixReboot, String localOperator, String globalOperator, Double minValue, Double maxValue){
		super(name, sensorID, fixReboot, localOperator, globalOperator, minValue, maxValue);
	}
	// set
	
	@Override
	public void setValue(double value){
		//if(this.minValue <= value && value <= maxValue){
			this.value = value;
		//}else{
			//System.out.println("DEBUG: Il valore " +value+ "è out of range. CAPABILITY NAME = "+this.name);
		//}
	}
	
	@Override
	public double getValue(){
		return this.value;
	}
	
	@Override
	public String toString() {
		String toRet = "*********** DataContainer ***********\n";
		toRet += "Name: " + getName() + "\n";
		toRet += "Sensor ID: " + getSensorID() + "\n";
		toRet += "Local: " + localOperator() + "\n";
		toRet += "Global: " + globalOperator() + "\n";
		toRet += "Min Value: " + getMinValue() + "\n";
		toRet += "Max Value: " + getMaxValue() + "\n";
		toRet += "Value: " + this.value + "\n";
		toRet += "********* End DataContainer *********\n";
		
		return toRet;
	}
	
}
