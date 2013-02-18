package mc3kit;


public abstract class Distribution extends ModelNode {

  public Distribution(Model model) {
    this(model, null);
  }
  
  public Distribution(Model model, String name) {
    super(name);
    
    if(model != null) {
      model.addDistribution(this);
    }
  }
  
  public abstract double getLogP(Variable var);
  
  public abstract VariableProposer makeVariableProposer(String varName);
  
  public abstract void sample(Variable var);
}
