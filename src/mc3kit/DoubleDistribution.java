package mc3kit;

public abstract class DoubleDistribution extends Distribution {

  public DoubleDistribution() {
    this(null);
  }
  
  public DoubleDistribution(String name) {
    super(name);
  }

  @Override
  public abstract VariableProposer makeVariableProposer(String varName);
  
  public abstract boolean valueIsValid(double value);
}
