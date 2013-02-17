package mc3kit.functions;

import mc3kit.*;

import java.util.*;

public class DoubleSumFunction extends DoubleFunction {
  Map<DoubleValued, Summand> summandMap;
  Map<ModelEdge, Summand> edgeMap;
  
  public DoubleSumFunction() {
    this(null);
  }

  public DoubleSumFunction(String name) {
    super(name);
    summandMap = new LinkedHashMap<DoubleValued, Summand>(2);
  }
  
  public <T extends ModelNode & DoubleValued> DoubleSumFunction add(T summandNode) throws ModelException {
    return add(summandNode, 1.0);
  }
  
  public <T extends ModelNode & DoubleValued> DoubleSumFunction add(T summandNode, double coeff) throws ModelException {
    if(summandMap.containsKey(summandNode)) {
      throw new IllegalArgumentException("Summand already present.");
    }
    
    ModelEdge edge = new ModelEdge(this, summandNode);
    getModel().addEdge(edge);
    Summand summand = new Summand(edge, coeff);
    summandMap.put(summandNode, summand);
    
    return this;
  }
  
  public <T extends ModelNode & DoubleValued> DoubleSumFunction remove(T summandNode) throws ModelException {
    Summand summand = summandMap.remove(summandNode);
    if(summand == null) {
      throw new IllegalArgumentException("Summand not present.");
    }
    getModel().removeEdge(summand.edge);
    return this;
  }
  
  @Override
  public boolean update() {
    double oldVal = getValue();
    double newVal = 0.0;
    for(Summand summand : summandMap.values()) {
      newVal += summand.coeff * getDoubleValue(summand.edge);
    }
    if(newVal != oldVal) {
      setValue(newVal);
      return true;
    }
    return false;
  }
  
  private class Summand {
    double coeff;
    ModelEdge edge;
    
    Summand(ModelEdge edge, double coeff) {
      this.coeff = coeff;
      this.edge = edge;
    }
  }
}
