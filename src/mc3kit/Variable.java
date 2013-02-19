package mc3kit;

import static java.lang.String.format;

public abstract class Variable extends ModelNode {
  private boolean observed;
  
  private double logP;
  
  protected Variable(Model model) {
    this(model, null, true);
  }
  
  protected Variable(Model model, Distribution dist) throws ModelException {
    this(model, null, true, dist);
  }
  
  protected Variable(Model model, String name, boolean observed) {
    super(name);
    this.observed = observed;
    
    if(model != null) {
      model.addVariable(this);
    }
  }
  
  protected Variable(Model model, String name, boolean observed, Distribution dist) throws ModelException {
    this(model, name, observed);
    setDistribution(dist);
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
  
  public Object makeOutputObject() {
    throw new UnsupportedOperationException("This variable doesn't support output.");
  }
  
  public String makeOutputString() {
    throw new UnsupportedOperationException("This variable doesn't support output as string.");
  }
}
