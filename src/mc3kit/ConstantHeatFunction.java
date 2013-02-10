package mc3kit;

public class ConstantHeatFunction implements HeatFunction
{
  double priorHeatExponent;
  double likelihoodHeatExponent;
  
  public ConstantHeatFunction() {
    this(1.0, 1.0);
  }
  
  public ConstantHeatFunction(double priorHeatExponent, double likelihoodHeatExponent) {
    this.priorHeatExponent = 1.0;
    this.likelihoodHeatExponent = 1.0;
  }
  
	@Override
	public double[] getPriorHeatExponents(int chainCount)
	{
		double[] heatExponents = new double[chainCount];
		for(int i = 0; i < chainCount; i++)
			heatExponents[i] = priorHeatExponent;
		return heatExponents;
	}

	@Override
	public double[] getLikelihoodHeatExponents(int chainCount)
	{
    double[] heatExponents = new double[chainCount];
    for(int i = 0; i < chainCount; i++)
      heatExponents[i] = likelihoodHeatExponent;
    return heatExponents;
	}

}
