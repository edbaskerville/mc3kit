package mc3kit;

import static java.lang.Math.pow;

public class PowerHeatFunction implements HeatFunction
{
	double heatPower = 1.0;
	public double getHeatPower()
	{
		return heatPower;
	}

	public void setHeatPower(double heatPower)
	{
		this.heatPower = heatPower;
	}

	double minHeatExponent = 0.0;
	public double getMinHeatExponent()
	{
		return minHeatExponent;
	}
	public void setMinHeatExponent(double minHeatExponent)
	{
		this.minHeatExponent = minHeatExponent;
	}
	
	Integer chainCountForCalculation = null;
	public Integer getChainCountForCalculation()
	{
		return chainCountForCalculation;
	}
	public void setChainCountForCalculation(Integer chainCountForCalculation)
	{
		this.chainCountForCalculation = chainCountForCalculation;
	}
	
	@Override
	public double[] getPriorHeatExponents(int chainCount)
	{
		double[] heatExponents = new double[chainCount];
		for(int i = 0; i < chainCount; i++)
			heatExponents[i] = 1.0;
		return heatExponents;
	}

	@Override
	public double[] getLikelihoodHeatExponents(int chainCount)
	{
		int chainCountForCalc = chainCountForCalculation != null ? chainCountForCalculation : chainCount;
		
		double[] heatExponents = new double[chainCount];
		heatExponents[0] = 1.0;
		if(chainCount > 1)
		{
			for(int i = 1; i < chainCount; i++)
			{
				double linearValue = 1.0 - i / ((double)chainCountForCalc-1);
				heatExponents[i] = minHeatExponent + pow(linearValue, heatPower) * (1.0 - minHeatExponent);
			}
		}
			
		return heatExponents;
	}

}
