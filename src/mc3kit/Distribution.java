package mc3kit;

import cern.jet.random.engine.RandomEngine;

public abstract class Distribution<V extends Variable<?>> extends ModelNode {

  public Distribution() {
    this(null);
  }
  
  public Distribution(String name) {
    super(name);
  }
  
  public abstract double getLogP(V var);
  
  public abstract VariableProposer<V> makeVariableProposer(String varName);
  
  public abstract void sample(V var, RandomEngine rng);
}
