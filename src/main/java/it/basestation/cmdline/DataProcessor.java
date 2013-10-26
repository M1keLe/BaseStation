package it.basestation.cmdline;

import java.util.Date;

public class DataProcessor extends Thread {
	
	public void run(){
		while (true) {
			try {
				System.out.println("Data Processor in esecuzione" + new Date());
				Thread.sleep(Configurator.getFreqDataProcessor());
				
				/*
				 * - scrivere log su file
				 * - fare medie su ultimo periodo locali
				 * - calcolare grandezze globali
				 * - aggiornare le fusion talbles
				 */
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
