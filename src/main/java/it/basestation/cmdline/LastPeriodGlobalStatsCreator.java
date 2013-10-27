package it.basestation.cmdline;

public class LastPeriodGlobalStatsCreator  implements IStatsCreator{

	@Override
	public IStats factoryMethod() {
		
		return new LastPeriodGlobalStats();
	}

}
