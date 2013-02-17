package mc3kit;

import cern.jet.random.engine.RandomEngine;

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
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void sample(RandomEngine rng) {
    ((Distribution)getDistribution()).sample(this, rng);
  }
  
  public Variable<D> setDistribution(D dist) {
    getModel().setDistribution(this, dist);
    return this;
  }
  
  @SuppressWarnings("unchecked")
  public D getDistribution() {
    return (D)getModel().getDistributionForVariable(this);
  }
}
