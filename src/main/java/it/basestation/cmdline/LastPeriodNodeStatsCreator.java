package it.basestation.cmdline;

public class LastPeriodNodeStatsCreator implements IStatsCreator{

	@Override
	public IStats factoryMethod() {

		return new LastPeriodNodeStats();
	}

}
