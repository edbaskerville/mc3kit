package mc3kit;

import static java.lang.Math.pow;

public class PowerHeatFunction implements HeatFunction
{
	double heatPower = 1.0;
  double minHeatExponent = 0.0;
  
  public PowerHeatFunction(double heatPower, double minHeatExponent) {
    this.heatPower = heatPower;
    this.minHeatExponent = minHeatExponent;
  }
  
	public double getHeatPower()
	{
		return heatPower;
	}

	public void setHeatPower(double heatPower)
	{
		this.heatPower = heatPower;
	}

	public double getMinHeatExponent()
	{
		return minHeatExponent;
	}
	
	public void setMinHeatExponent(double minHeatExponent)
	{
		this.minHeatExponent = minHeatExponent;
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
		double[] heatExponents = new double[chainCount];
		heatExponents[0] = 1.0;
		if(chainCount > 1)
		{
			for(int i = 1; i < chainCount; i++)
			{
				double linearValue = 1.0 - i / ((double)chainCount-1);
				heatExponents[i] = minHeatExponent + pow(linearValue, heatPower) * (1.0 - minHeatExponent);
			}
		}
			
		return heatExponents;
	}

}
