package mc3kit.types.intarray.distributions;

import static java.lang.Math.*;
import static mc3kit.util.Math.*;

import mc3kit.*;
import mc3kit.model.Model;
import mc3kit.model.ModelEdge;
import mc3kit.model.ModelNode;
import mc3kit.model.Variable;
import mc3kit.step.univariate.VariableProposer;
import mc3kit.types.doublearray.DoubleArrayValued;
import mc3kit.types.intarray.IntArrayDistribution;
import mc3kit.types.intarray.IntArrayVariable;

public class MultinomialDistribution extends IntArrayDistribution {
  private ModelEdge pEdge;
  
  protected MultinomialDistribution() { }
  
  public MultinomialDistribution(Model model) {
    this(model, null);
  }
  
  public MultinomialDistribution(Model model, String name) {
    super(model, name);
  }
  
  public MultinomialDistribution setP(DoubleArrayValued p) throws MC3KitException {
    pEdge = updateEdge(pEdge, (ModelNode)p);
    return this;
  }
  
  @Override
  public VariableProposer makeVariableProposer(String varName) {
   // Only supporting observed variables for now; no proposing
    return null;
  }
  
  @Override
  public double getLogP(Variable var) throws MC3KitException {
    DoubleArrayValued pNode = (DoubleArrayValued)pEdge.getHead();
    double[] p = pNode.getValue();
    int[] x = ((IntArrayVariable)var).getValue();
    
    assert p.length == x.length;
    
    double logP = 0.0;
    
    int n = 0;
    for(int i = 0; i < p.length; i++) {
      n += x[i];
      logP += x[i] * log(p[i]) - logFactorial(x[i]);
    }
    logP += logFactorial(n);
    
    return logP;
  }
  
  @Override
  public void sample(Variable var) {
    // Only supporting observed variables for now; no sampling
    assert false;
  }
}
