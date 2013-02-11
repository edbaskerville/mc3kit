package mc3kit.distributions;

import mc3kit.DoubleDistribution;
import mc3kit.DoubleVariable;
import mc3kit.VariableProposer;

import mc3kit.proposal.*;

public class NormalDistribution extends DoubleDistribution {

  public NormalDistribution() {
    this(null);
  }
  
  public NormalDistribution(String name) {
    super(name);
  }

  @Override
  public VariableProposer<DoubleVariable> makeVariableProposer(String varName) {
    return new MHNormalProposer(varName);
  }
}
