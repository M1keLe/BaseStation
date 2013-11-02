package it.basestation.cmdline;

import java.util.TimerTask;

public class Resetter extends TimerTask{
	
	@Override
	public void run(){
		// reset lista globale pacchetti
		// reset grandezze globali
		LocalStatsManager.resetAllStats();
	}

}
