package it.basestation.cmdline;

public class Capability {

	// nome capability
	private String name = "";
	// nome colonna fusion table
	private String columnName = "";
	private String localOperator = "";
	private String globalOperator = "";
	// indice su cui effettuare op local o global
	private String target = "";
	// range
	private Double minValue = Double.NEGATIVE_INFINITY;
	private Double maxValue = Double.POSITIVE_INFINITY;
	// finestra media mobile
	private int avgWindow = 0;

	public Capability(String name,
					  String columnName,
					  String target,
					  String localOperator,
					  String globalOperator,
					  Double minValue,
					  Double maxValue,
					  int avgWindow) {
		
		this.name = name;
		this.columnName = columnName;
		this.target = target;
		this.localOperator = localOperator;
		this.globalOperator = globalOperator;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.avgWindow = avgWindow;
	}

	// get

	public String getName() {
		return this.name;
	}
	
	public String getColumnName(){
		return this.columnName;
	}
	
	public String getTarget(){
		return this.target;
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
	
	public int getAvgWindow(){
		return this.avgWindow;
	}
	
	@Override
	public String toString() {
		String toRet = "*********** Capability ***********\n";
		toRet += "Name: " + this.name + "\n";
		toRet += "Column Name: " + getColumnName() + "\n";
		toRet += "Target: " + this.target + "\n";
		toRet += "Local: " + this.localOperator + "\n";
		toRet += "Global: " + this.globalOperator + "\n";
		toRet += "Min Value: " + this.minValue + "\n";
		toRet += "Max Value: " + this.maxValue + "\n";
		toRet += "AVG Window: " + this.avgWindow + "\n";
		toRet += "********* End Capability *********\n";
		
		return toRet;
	}
}