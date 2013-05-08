package mc3kit.partition;

import mc3kit.*;
import mc3kit.model.Distribution;
import mc3kit.model.Model;
import mc3kit.step.univariate.VariableProposer;

@SuppressWarnings("serial")
public abstract class PartitionDistribution extends Distribution {
  
  protected PartitionDistribution() { }
  
  public PartitionDistribution(Model model) {
    this(model, null);
  }
  
  public PartitionDistribution(Model model, String name) {
    super(model, name);
  }
  
  @Override
  public VariableProposer makeVariableProposer(String varName) {
    return new PartitionProposer(varName);
  }
}
