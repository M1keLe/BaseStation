package it.basestation.cmdline;

public class Capability {
	private String name;
	private double value;
	private boolean isGlobal;
	private boolean isSummable;
	private boolean hasARange;
	private double minValue;
	private double maxValue;
	
	public Capability(String name){
		this.name = name;
		this.isGlobal = false;
		this.isSummable = false;
		this.hasARange = false;
		this.value = 0.00;
		this.minValue = 0.00;
		this.maxValue = 0.00;
	}
	
	public Capability(String name, boolean isGlobal, boolean isSummable){
		this.name = name;
		this.isGlobal = isGlobal;
		this.isSummable = isSummable;
		this.hasARange = false;
		this.value = 0.00;
		this.minValue = 0.00;
		this.maxValue = 0.00;
	}
	
	// set
	
	public void setValue(double value){
		this.value = value;
	}
	
	public void setRangeValues(double minValue, double maxValue){
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.hasARange = true;
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
		
	public boolean isGlobal(){
		return this.isGlobal;
	}
	
	public boolean isSummable(){
		return this.isSummable;
	}
	
	public boolean hasARange(){
		return this.hasARange;
	}
	
	public double getValue(){
		return this.value;
	}
	
	public String toString(){
		String toRet = "*********** Capability ***********\n";
		toRet += "Name: " + this.name + "\n";
		toRet += "Is Global: " + this.isGlobal + "\n";
		toRet += "Is Summable: " + this.isSummable + "\n";
		toRet += "Has a Range: " + this.hasARange + "\n";
		toRet += "Min Value: " + this.minValue + "\n";
		toRet += "Max Value: " + this.maxValue + "\n";
		toRet += "********* End Capability *********\n";
		
		return toRet;
	}
	
}
