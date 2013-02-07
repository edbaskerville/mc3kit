package mc3kit;

import java.io.Serializable;

import cern.jet.random.engine.RandomEngine;

@SuppressWarnings("serial")
public class Chain implements Serializable
{
	MCMC mcmc;
	int chainId;
	int chainCount;
	double priorHeatExponent;
	double likelihoodHeatExponent;
	RandomEngine rng;
	
	long iterationCount;
	
	Model model;
	
	Chain(MCMC mcmc, int chainId, int chainCount, double priorHeatExponent, double likelihoodHeatExponent, RandomEngine rng)
	{
		this.mcmc = mcmc;
		this.chainId = chainId;
		this.chainCount = chainCount;
		this.priorHeatExponent = priorHeatExponent;
		this.likelihoodHeatExponent = likelihoodHeatExponent;
		this.rng = rng;
	}
	
	public MCMC getMCMC()
	{
		return mcmc;
	}
  
  void setModel(Model model)
  {
    this.model = model;
  }
	
	public Model getModel()
	{
		return model;
	}
	
	public RandomEngine getRng()
	{
		return rng;
	}
	
	public int getChainId()
	{
		return chainId;
	}
	
	public int getChainCount()
	{
		return chainCount;
	}
	
	public double getPriorHeatExponent()
	{
		return priorHeatExponent;
	}
	
	public double getLikelihoodHeatExponent()
	{
		return likelihoodHeatExponent;
	}
	
	public Chain getChainAbove()
	{
		if(chainId < chainCount - 1)
		{
			return mcmc.getChain(chainId + 1);
		}
		return null;
	}
	
	public void iterate()
	{
		iterationCount++;
	}
	
	public long getIterationCount()
	{
		return iterationCount;
	}
	
	public ErrorLogger getLogger()
	{
		return mcmc.getLogger();
	}
}
