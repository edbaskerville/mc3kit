package mc3kit;

public abstract class DoubleDistribution extends Distribution {

  public DoubleDistribution(Model model) {
    this(model, null);
  }
  
  public DoubleDistribution(Model model, String name) {
    super(model, name);
  }

  @Override
  public abstract VariableProposer makeVariableProposer(String varName);
  
  public abstract boolean valueIsValid(double value);
}
