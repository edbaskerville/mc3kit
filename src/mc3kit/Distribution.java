package mc3kit;


public abstract class Distribution extends ModelNode {

  public Distribution() {
    this(null);
  }
  
  public Distribution(String name) {
    super(name);
  }
  
  public abstract double getLogP(Variable var);
  
  public abstract VariableProposer makeVariableProposer(String varName);
  
  public abstract void sample(Variable var);
}
