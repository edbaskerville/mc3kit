package mc3kit;

public abstract class Variable<D extends Distribution<?>> extends ModelNode {
  private boolean observed;
  
  private double logP;
  
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
  
  protected void setLogP(double logP) {
    this.logP = logP;
  }
  
  public double getLogP() {
    return logP;
  }
  
  @SuppressWarnings("unchecked")
  public D getDistribution() {
    return (D)getModel().getDistributionForVariable(this);
  }
}
