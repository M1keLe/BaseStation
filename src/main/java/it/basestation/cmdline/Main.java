package it.basestation.cmdline;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Timer;


public class Main 
{
	public static final long DAY = 1000*60*60*24;
	
    public static void main( String[] args )
    {
    	if(Configurator.loadConfigFile()){
    		
    		// passare la lista di nodi al local stats manager
    		LocalStatsManager.setNodeList();
    		
    		// controllare e creare tabelle
    		FusionTablesManager.connect();
    		FusionTablesManager.setupTables();
    		/*
    		 * - lanciare serial reader
    		 * 
    		 */
/*    		try {
				SerialCommunication.createCommunication();
			} catch (NoSuchPortException | PortInUseException
					| UnsupportedCommOperationException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		SerialReader sR = new SerialReader(SerialCommunication.getInputStream());
    		sR.start();
 */   		// avvio data Processor e reader
    		//new DataProcessor().start();
    		FileReader fr = new FileReader();
    		fr.start();
    		DataProcessor dp = new DataProcessor();
    		dp.start();   
    		
    		// se specificato nel file di configurazione avvio il thread resetter
    		if(Configurator.getResetTime() != null){
    			Timer resetter = new Timer("resetter");
    			resetter.schedule(new Resetter(), Configurator.getResetTime(), DAY);
    			System.out.println("Primo Reset: " + Configurator.getResetTime());
    		}else{
    			System.out.println("Le statistiche non verranno mai resettate");
    		}
    	}
    }
}
