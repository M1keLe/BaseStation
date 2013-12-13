package it.basestation.cmdline;

public class Capability {

	// nome capability
	private String name = "";
	// nome colonna fusion table
	private String columnName = "";
	private String localOperator = "";
	private String globalOperator = "";
	// indice su cui effettuare op local o global
	private String index = "";
	// range
	private Double minValue = Double.NEGATIVE_INFINITY;
	private Double maxValue = Double.POSITIVE_INFINITY;
	// finestra media mobile
	private int avgWindow = 0;

/*	public Capability(String name){
		this.name = name;
	}
	
	public Capability(String name, String localOperator, String globalOperator){
		this.name = name;
		this.localOperator = localOperator;
		this.globalOperator = globalOperator;
	}
	
	public Capability(String name, String localOperator, String globalOperator, Double minValue, Double maxValue) {
		this.name = name;
		this.localOperator = localOperator;
		this.globalOperator = globalOperator;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	public Capability(String name, String localOperator, String globalOperator, Double minValue, Double maxValue, int avgWindow) {
		this.name = name;
		this.localOperator = localOperator;
		this.globalOperator = globalOperator;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.avgWindow = avgWindow;
	}
*/	
	public Capability(String name, String columnName, String index, String localOperator, String globalOperator, Double minValue, Double maxValue, int avgWindow) {
		this.name = name;
		this.columnName = columnName;
		this.index = index;
		this.localOperator = localOperator;
		this.globalOperator = globalOperator;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.avgWindow = avgWindow;
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
	
	public String getColumnName(){
		String toRet = this.columnName;
		// quando non specificato nel file di configurazione
		if(toRet.isEmpty()){
			toRet = this.name +"_";
			toRet += this.localOperator + this.globalOperator;
			if(this.avgWindow > 0)
				toRet += "_"+this.avgWindow;
		}
		return toRet;
	}
	
	public String getIndex(){
		return this.index;
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
		toRet += "Index: " + this.index + "\n";
		toRet += "Local: " + this.localOperator + "\n";
		toRet += "Global: " + this.globalOperator + "\n";
		toRet += "Min Value: " + this.minValue + "\n";
		toRet += "Max Value: " + this.maxValue + "\n";
		toRet += "AVG Window: " + this.avgWindow + "\n";
		toRet += "********* End Capability *********\n";
		
		return toRet;
	}

}