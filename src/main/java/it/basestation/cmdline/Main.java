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
    		
    		/*
    		 * - controllare e creare tabelle
    		 * - lanciare serial reader
    		 * - lanciare data processor
    		 * 
    		 */
    		DataProcessor dp = new DataProcessor();
    		dp.start();
    		
    		if(Configurator.getResetTime() != null){
    			Timer resetter = new Timer("resetter");
    			resetter.schedule(new Resetter(), Configurator.getResetTime(), DAY);
    			System.out.println("Primo Reset: " + Configurator.getResetTime());
    		}else{
    			System.out.println("Le statistiche non verranno mai resettate");
    		}
    	}
        //Configurator.loadConfigFile();
    }
}
