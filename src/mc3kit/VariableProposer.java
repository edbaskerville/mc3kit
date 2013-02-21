package mc3kit;


public abstract class VariableProposer
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
}
