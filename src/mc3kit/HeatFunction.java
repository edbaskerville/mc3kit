package mc3kit;

public interface HeatFunction
{
	public double[] getPriorHeatExponents(int chainCount);
	public double[] getLikelihoodHeatExponents(int chainCount);
}
