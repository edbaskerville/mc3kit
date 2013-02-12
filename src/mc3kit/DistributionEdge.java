package mc3kit;

public class DistributionEdge extends ModelEdge {

  public DistributionEdge(Variable<?> var, Distribution<?> dist) {
    super(var, dist);
  }
  
  public Variable<?> getVariable() {
    return (Variable<?>)getTail();
  }
  
  public Distribution<?> getDistribution() {
    return (Distribution<?>)getHead();
  }
}
