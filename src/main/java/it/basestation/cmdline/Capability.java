package it.basestation.cmdline;

public class Capability {
    
	private String name = "";
	private String localOperator = "";
	private String globalOperator = "";
	private Double minValue = Double.NEGATIVE_INFINITY;
	private Double maxValue = Double.POSITIVE_INFINITY;

/*	public Capability(String name){
		this.name = name;
	}
	
	public Capability(String name, String localOperator, String globalOperator){
		this.name = name;
		this.localOperator = localOperator;
		this.globalOperator = globalOperator;
	}
*/	
	public Capability(String name, String localOperator, String globalOperator, Double minValue, Double maxValue) {
		this.name = name;
		this.localOperator = localOperator;
		this.globalOperator = globalOperator;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

/*	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}

	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	public void setLocalOperator(String local) {
		this.localOperator=local;
	}

	public void setGlobalOperator(String global) {
		this.globalOperator=global;
	}
*/
	
	// get
	public String getName() {
		return this.name;
	}
	
	public double getMinValue() {
		return this.minValue;
	}

	public double getMaxValue() {
		return this.maxValue;
	}

	public String localOperator() {
		return this.localOperator;
	}

	public String globalOperator() {
		return this.globalOperator;
	}
	
	@Override
	public String toString() {
		String toRet = "*********** Capability ***********\n";
		toRet += "Name: " + this.name + "\n";
		toRet += "Local: " + this.localOperator + "\n";
		toRet += "Global: " + this.globalOperator + "\n";
		toRet += "Min Value: " + this.minValue + "\n";
		toRet += "Max Value: " + this.maxValue + "\n";
		toRet += "********* End Capability *********\n";
		
		return toRet;
	}

}