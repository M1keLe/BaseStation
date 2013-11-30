package it.basestation.cmdline;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Timer;


public class Main 
{
	
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
				SerialComunication.createComunication();
			} catch (NoSuchPortException | PortInUseException
					| UnsupportedCommOperationException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		SerialReader sR = new SerialReader(SerialComunication.getInputStream());
    		sR.start();
 */   		// avvio data Processor e reader
    		FileReader fr = new FileReader();
    		fr.start();
    		DataProcessor dp = new DataProcessor();
    		dp.start();   
    		
    	}
    }
}
