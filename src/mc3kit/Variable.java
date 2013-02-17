package mc3kit;

import static java.lang.String.format;

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
  
  public void sample() throws ModelException {
    getDistribution().sample(this);
    setChanged();
    notifyObservers();
  }
  
  public Variable setDistribution(Distribution dist) throws ModelException {
    getModel().setDistribution(this, dist);
    return this;
  }
  
  public Distribution getDistribution() {
    return getModel().getDistributionForVariable(this);
  }
  
  public VariableProposer makeProposer() throws MC3KitException {
    Distribution dist = getDistribution();
    if(dist == null) {
      throw new MC3KitException(format("No distribution for variable %s", this));
    }
    return getDistribution().makeVariableProposer(getName());
  }
}
