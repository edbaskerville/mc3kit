package mc3kit;

public class DistributionEdge extends ModelEdge {

  public DistributionEdge(Model model, Variable var, Distribution dist) throws ModelException {
    super(model, var, dist);
  }
  
  public Variable getVariable() {
    return (Variable)getTail();
  }
  
  public Distribution getDistribution() {
    return (Distribution)getHead();
  }
}
