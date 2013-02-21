package mc3kit;

import static java.lang.Math.pow;

public class PowerHeatFunction implements HeatFunction
{
	double likeHeatPower = 1.0;
  double minLikeHeatExponent = 0.0;
  
  double priorHeatPower = 1.0;
  double minPriorHeatExponent = 1.0;
  
  public PowerHeatFunction(double likeHeatPower, double minLikeHeatExponent, double priorHeatPower, double minPriorHeatExponent) {
    this.likeHeatPower = likeHeatPower;
    this.minLikeHeatExponent = minLikeHeatExponent;
    this.priorHeatPower = priorHeatPower;
    this.minPriorHeatExponent = minPriorHeatExponent;
  }
  
  public PowerHeatFunction(double heatPower, double minHeatExponent) {
    this.likeHeatPower = heatPower;
    this.minLikeHeatExponent = minHeatExponent;
  }
  
	public double getHeatPower()
	{
		return likeHeatPower;
	}

	public void setHeatPower(double heatPower)
	{
		this.likeHeatPower = heatPower;
	}

	public double getMinHeatExponent()
	{
		return minLikeHeatExponent;
	}
	
	public void setMinHeatExponent(double minHeatExponent)
	{
		this.minLikeHeatExponent = minHeatExponent;
	}
	
	@Override
	public double[] getPriorHeatExponents(int chainCount)
	{
    double[] heatExponents = new double[chainCount];
    heatExponents[0] = 1.0;
    if(chainCount > 1)
    {
      for(int i = 1; i < chainCount; i++)
      {
        double linearValue = 1.0 - i / ((double)chainCount-1);
        heatExponents[i] = minPriorHeatExponent + pow(linearValue, priorHeatPower) * (1.0 - minPriorHeatExponent);
      }
    }
      
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
				heatExponents[i] = minLikeHeatExponent + pow(linearValue, likeHeatPower) * (1.0 - minLikeHeatExponent);
			}
		}
			
		return heatExponents;
	}

}
