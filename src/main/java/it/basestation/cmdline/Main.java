package it.basestation.cmdline;

/**
 * Hello world!
 *
 */
public class Main 
{
    public static void main( String[] args )
    {
    	if(!Configurator.loadConfigFile()){
    		System.out.println("Sono presenti errori nel file di configurazione");
    	}else{
    		System.out.println("Il file di configurazione è stato caricato correttamente");
    	}
        //Configurator.loadConfigFile();
    }
}
