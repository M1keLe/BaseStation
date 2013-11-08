package it.basestation.cmdline;

public class Capability {
	private String name = "";
	private String localOperator = "";
	private String globalOperator = "";
	private Double minValue = Double.NEGATIVE_INFINITY;
	private Double maxValue = Double.POSITIVE_INFINITY;
	private double value = 0.00;
	public Capability(String name){
		this.name = name;
	}
	
	public Capability(String name, String local, String global){
		this.name = name;
		this.localOperator = local;
		this.globalOperator = global;
	}
	
	// set
	
	public void setValue(double value){
		//if(this.minValue <= value && value <= maxValue){
			this.value = value;
		//}else{
			//System.out.println("DEBUG: Il valore " +value+ "è out of range. CAPABILITY NAME = "+this.name);
		//}
	}
	
	public void setMinValue(Double minValue){
		this.minValue = minValue;
	}
	
	public void setMaxValue(Double maxValue){
		this.maxValue = maxValue;
	}
	
	public void setLocalOperator(String local){
		this.localOperator=local;
	}

	public void setGlobalOperator(String global){
		this.globalOperator=global;
	}
	// get
	
	public String getName(){
		return this.name;
	}
	
	public double getMinValue(){
		return this.minValue;
	}
	
	public double getMaxValue(){
		return this.maxValue;
	}
		
	
	public double getValue(){
		return this.value;
	}
	
	public String localOperator(){
		return this.localOperator;
	}
	
	public boolean isGlobal(){
		boolean toRet = false;
		if(!this.globalOperator.isEmpty())
			toRet = true;
		return toRet;
	}
	
	public String globalOperator(){
		return this.globalOperator;
	}
	
	@Override
	public String toString(){
		String toRet = "*********** Capability ***********\n";
		toRet += "Name: " + this.name + "\n";
		toRet += "Local: " + this.localOperator + "\n";
		toRet += "Global: " + this.globalOperator + "\n";
		toRet += "Min Value: " + this.minValue + "\n";
		toRet += "Max Value: " + this.maxValue + "\n";
		toRet += "Value: " + this.value + "\n";
		toRet += "********* End Capability *********\n";
		
		return toRet;
	}
	
}
