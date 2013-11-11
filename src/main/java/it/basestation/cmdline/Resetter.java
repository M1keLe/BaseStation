package it.basestation.cmdline;

import java.util.TimerTask;

public class Resetter extends TimerTask{
	
	public Resetter(){
		
	}
	
	@Override
	public void run(){
		// reset lista globale pacchetti
		// reset grandezze globali
		LocalStatsManager.resetAllStats();
	}

}
