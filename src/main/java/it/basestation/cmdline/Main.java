package it.basestation.cmdline;

import java.util.Timer;


public class Main 
{
	public static final long DAY = 1000*60*60*24;
	
    public static void main( String[] args )
    {
    	if(!Configurator.loadConfigFile()){
    		System.out.println("Sono presenti errori nel file di configurazione");
    	}else{
    		System.out.println("Il file di configurazione è stato caricato correttamente");
    		// passare la lista di nodi al local stats container
    		LocalStatsManager.setNodeList();
    		
    		
    		// controllare e creare tabelle
    		//FusionTablesManager.connect();
    		//FusionTablesManager.setupTables(Configurator.getNodeList());
    		/*
    		 * - lanciare serial reader
    		 * 
    		 */
    		
    		// avvio data Processor
    		//new DataProcessor().start();
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
