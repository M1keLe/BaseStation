package it.basestation.cmdline;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;


public class Main 
{
	
	public static boolean debug = true; 
			// true; 
			// false;
    public static void main( String[] args )
    {
    	if(Configurator.loadConfigFile()){
    		
    		// passare la lista di nodi al local stats manager
    		LocalStatsManager.setNodeList();
    		
    		// controllare e creare tabelle
    		FusionTablesManager.connect();
    		FusionTablesManager.setupTables();
    		
    		if(debug){
    			// avvio file reader inserire file da analizzare nel costruttore
        		FileReader fr = new FileReader("06-dic-2013.log_debug");
        		fr.start();
    		}else{
    			// serial reader
		  		try {
		  			SerialComunication.createComunication();	
		  		} catch (NoSuchPortException | PortInUseException
							| UnsupportedCommOperationException | IOException e) {
					e.printStackTrace();
		  		}  		
	    		SerialReader sR = new SerialReader(SerialComunication.getInputStream());
	    		sR.start();
    		} 		
    		// avvio data processor
    		DataProcessor dp = new DataProcessor();
    		dp.start();     		
    	}
    }
}
