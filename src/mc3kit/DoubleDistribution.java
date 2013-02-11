package mc3kit;

public abstract class DoubleDistribution extends Distribution<DoubleVariable> {

  public DoubleDistribution() {
    this(null);
  }
  
  public DoubleDistribution(String name) {
    super(name);
  }

  @Override
  public abstract VariableProposer<DoubleVariable> makeVariableProposer(String varName);
}
