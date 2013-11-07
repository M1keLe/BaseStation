package it.basestation.cmdline;

public class Capability {
	private String name = "";
	private String local = "";
	private String global = "";
	private Double minValue = Double.NEGATIVE_INFINITY;
	private Double maxValue = Double.POSITIVE_INFINITY;
	private double value = 0.00;
	public Capability(String name){
		this.name = name;
	}
	
	public Capability(String name, String local, String global){
		this.name = name;
		this.local = local;
		this.global = global;
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
	
	public void setLocalRule(String local){
		this.local=local;
	}

	public void setGlobalRule(String global){
		this.global=global;
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
	
	public String getLocalRule(){
		return this.local;
	}
	
	public boolean isGlobal(){
		boolean toRet = false;
		if(!this.global.isEmpty())
			toRet = true;
		return toRet;
	}
	
	public String getGlobalRule(){
		return this.global;
	}
	
	@Override
	public String toString(){
		String toRet = "*********** Capability ***********\n";
		toRet += "Name: " + this.name + "\n";
		toRet += "Local: " + this.local + "\n";
		toRet += "Global: " + this.global + "\n";
		toRet += "Min Value: " + this.minValue + "\n";
		toRet += "Max Value: " + this.maxValue + "\n";
		toRet += "Value: " + this.value + "\n";
		toRet += "********* End Capability *********\n";
		
		return toRet;
	}
	
}
