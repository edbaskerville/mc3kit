package mc3kit.proposal;

import mc3kit.*;

import cern.jet.random.engine.RandomEngine;

public abstract class VariableProposer<V extends Variable<?>>
{
	private String name;
	
	private int proposalCount;
	private int acceptanceCount;
	
	protected VariableProposer(Model model, String name) {
	  this.name = name;
	}
	
	public String getName() {
	  return name;
	}
	
	public void resetTuningPeriod()
	{
		proposalCount = 0;
		acceptanceCount = 0;
	}
	
	public double getAcceptanceRate()
	{
		return (proposalCount == 0)
			? 0.0
			: acceptanceCount / (double)proposalCount;
	}
	
	protected void recordRejection()
	{
		proposalCount++;
	}
	
	protected void recordAcceptance()
	{
		proposalCount++;
		acceptanceCount++;
	}
	
	public abstract void step(Model model, double priorHeatExp, double likeHeatExp, RandomEngine rng)
	    throws MC3KitException;
	public void tune(double targetRate) throws MC3KitException {}
}
