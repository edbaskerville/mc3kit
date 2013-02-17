package mc3kit;


public abstract class Variable extends ModelNode {
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
  
  public void sample() {
    getDistribution().sample(this);
  }
  
  public Variable setDistribution(Distribution dist) {
    getModel().setDistribution(this, dist);
    return this;
  }
  
  public Distribution getDistribution() {
    return getModel().getDistributionForVariable(this);
  }
}
