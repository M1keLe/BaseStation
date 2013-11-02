package it.basestation.cmdline;

public class Capability {
	private String name;
	private double value;
	private boolean isGlobal;
	private boolean isIndirect;
	private boolean hasARange;
	private double minValue;
	private double maxValue;
	
	public Capability(String name){
		this.name = name;
		this.isGlobal = false;
		this.isIndirect = false;
		this.hasARange = false;
		this.value = 0.00;
		this.minValue = 0.00;
		this.maxValue = 0.00;
	}
	
	public Capability(String name, boolean isGlobal, boolean isIndirect){
		this.name = name;
		this.isGlobal = isGlobal;
		this.isIndirect = isIndirect;
		this.hasARange = false;
		this.value = 0.00;
		this.minValue = 0.00;
		this.maxValue = 0.00;
	}
	
	// set
	
	public void setValue(double value){
		if(this.hasARange){
			if(value >= this.minValue && value <= this.maxValue){
				this.value = value;
			}else{
				this.value = -1.00;
			}
		}else{
			this.value = value;
		}
		
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
	
	public boolean isIndirect(){
		return this.isIndirect;
	}
	
	public boolean hasARange(){
		return this.hasARange;
	}
	
	public double getValue(){
		return this.value;
	}
	
	@Override
	public String toString(){
		String toRet = "*********** Capability ***********\n";
		toRet += "Name: " + this.name + "\n";
		toRet += "Is Global: " + this.isGlobal + "\n";
		toRet += "Is Indirect: " + this.isIndirect + "\n";
		toRet += "Has a Range: " + this.hasARange + "\n";
		toRet += "Min Value: " + this.minValue + "\n";
		toRet += "Max Value: " + this.maxValue + "\n";
		toRet += "Value: " + this.value + "\n";
		toRet += "********* End Capability *********\n";
		
		return toRet;
	}
	
}
