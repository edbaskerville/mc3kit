package mc3kit.distributions;

import mc3kit.*;
import mc3kit.proposal.GibbsBinaryProposer;
import static java.lang.Math.*;

public class BernoulliDistribution extends BinaryDistribution {

  double p;
  ModelEdge pEdge;
  
  public BernoulliDistribution(Model model) {
    this(model, null, 0.5);
  }

  public BernoulliDistribution(Model model, String name) {
    this(model, name, 0.5);
  }
  
  public BernoulliDistribution(Model model, double p) {
    this(model, null, p);
  }
  
  public BernoulliDistribution(Model model, String name, double p) {
    super(model, name);
    this.p = p;
  }
  
  
  public <T extends ModelNode & DoubleValued> BernoulliDistribution setP(T node) throws ModelException {
    pEdge = updateEdge(pEdge, node);
    return this;
  }
  
  @Override
  public double getLogP(Variable var) {
    boolean value = ((BinaryVariable)var).getValue();
    double p = pEdge == null ? this.p : getDoubleValue(pEdge);
    
    return value ? log(p) : log1p(-p);
  }

  @Override
  public VariableProposer makeVariableProposer(String varName) {
    return new GibbsBinaryProposer(varName);
  }

  @Override
  public void sample(Variable var) {
    double p = pEdge == null ? this.p : getDoubleValue(pEdge);
    ((BinaryVariable)var).setValue(getRng().nextDouble() < p);
  }
}
