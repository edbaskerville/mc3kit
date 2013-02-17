package mc3kit;


public abstract class VariableProposer<V extends Variable<?>>
{
	private String name;
	
	private int proposalCount;
	private int acceptanceCount;
	
	protected VariableProposer(String name) {
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
	
	public abstract void step(Model model)
	    throws MC3KitException;
	public void tune(double targetRate) throws MC3KitException {}
	

  
  public static double adjustTuningParameter(double param, double measuredRate, double targetRate)
  {
    if(measuredRate == 0)
    {
      return param / 2;
    }
    else if(measuredRate == 1)
    {
      return param * 2;
    }
    
    // Infer target rate using linear interpolation between
    // (lambda = 0, rate = 1) and (lambda = lambda, rate = measuredRate):
    // lambda is updated to be the value on that line where rate = targetRate.
    return param * (1.0 - targetRate) / (1.0 - measuredRate);
  }
}
