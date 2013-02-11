package mc3kit;

public abstract class Distribution<V extends Variable<?>> extends ModelNode {

  public Distribution() {
    this(null);
  }
  
  public Distribution(String name) {
    super(name);
  }
  
  public abstract VariableProposer<V> makeVariableProposer(String varName);
}
