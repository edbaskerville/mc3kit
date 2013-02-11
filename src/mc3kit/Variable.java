package mc3kit;

import mc3kit.graph.Node;

public abstract class Variable<D extends Distribution<?>> extends ModelNode {
  private boolean observed;
  
  public Variable() {
  }
  
  public Variable(String name) {
    super(name);
  }
  
  protected void setObserved(boolean observed) {
    this.observed = observed;
  }
  
  public boolean isObserved() {
    return observed;
  }
}
