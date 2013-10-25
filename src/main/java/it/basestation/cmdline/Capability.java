package it.basestation.cmdline;

public class Capability {
	private String name;
	private double value;
	private boolean isGlobal;
	private boolean isSummable;
	private double minValue;
	private double maxValue;
	
	public Capability(String name){
		this.name = name;
		this.isGlobal = false;
		this.isSummable = false;
		this.value = 0.00;
		this.minValue = 0.00;
		this.maxValue = 0.00;
	}
	
	public Capability(String name, boolean isGlobal, boolean isSummable){
		this.name = name;
		this.isGlobal = isGlobal;
		this.isSummable = isSummable;
		this.value = 0.00;
		this.minValue = 0.00;
		this.maxValue = 0.00;
	}
	
	// set
	
	public void setValue(double value){
		this.value = value;
	}
	
	public void setRangeValue(double minValue, double maxValue){
		this.minValue = minValue;
		this.maxValue = maxValue;
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
	
	public double getValue(){
		return this.value;
	}
	
}
