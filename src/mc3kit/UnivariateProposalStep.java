package mc3kit;

import static mc3kit.util.Math.getRandomPermutation;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;

public class UnivariateProposalStep implements Step {
  boolean initialized;
  
  double targetAcceptanceRate = 0.25;
  long tuneFor = 1000;
  long tuneEvery = 100;

  public UnivariateProposalStep(double targetAcceptanceRate, long tuneFor, long tuneEvery) {
    this.targetAcceptanceRate = targetAcceptanceRate;
    this.tuneFor = tuneFor;
    this.tuneEvery = tuneEvery;
  }

  @Override
  public List<Task> makeTasks(int chainCount) throws MC3KitException {
    List<Task> tasks = new ArrayList<Task>();
    for(int i = 0; i < chainCount; i++) {
      tasks.add(new UnivariateProposalTask(i));
    }
    return tasks;
  }
  
  /*** TASK INTERFACE IMPLEMENTATION ***/
  
  class UnivariateProposalTask implements Task {
    int chainId;
    
    private long iterationCount;
    
    VariableProposer[] proposers;
    
    /*** CONSTRUCTOR ***/
    
    public UnivariateProposalTask(int chainId) {
      this.chainId = chainId;
    }
    
    
    /*** TASK INTERFACE IMPLEMENTATION ***/
    
    @Override
    public int[] getChainIds() {
      return new int[] { chainId };
    }

    @Override
    public void step(Chain[] chains) throws MC3KitException {
      assert (chains.length == 1);

      Chain chain = chains[0];
      
      chain.getLogger().fine("UnivariateProposalStep stepping");
      
      Model model = chain.getModel();
      initialize(model);
      
      RandomEngine rng = chain.getRng();

      chain.getLogger().fine(format("univariate stepping %d", chainId));

      Uniform unif = new Uniform(rng);

      // Run all proposers in random order
      for(int i : getRandomPermutation(proposers.length, unif)) {
        proposers[i].step(model);
      }

      iterationCount++;

      // If we're still in the tuning period, tune
      if((iterationCount <= tuneFor) && iterationCount % tuneEvery == 0) {
        chain.getLogger().info("VARIABLE ACCEPTANCE RATES:");
        for(VariableProposer proposer : proposers) {
          chain.getLogger().info(format("%s: %f", proposer.getName(), proposer.getAcceptanceRate()));
          proposer.tune(targetAcceptanceRate);
          proposer.resetTuningPeriod();
        }
      }
    }
    
    /*** PRIVATE METHODS ***/
    
    private void initialize(Model model) throws MC3KitException {
      if(initialized) return;
      initialized = true;

      String[] varNames = model.getUnobservedVariableNames();
      proposers = new VariableProposer[varNames.length];
      for(int i = 0; i < varNames.length; i++) {
        proposers[i] = makeVariableProposer(model, varNames[i]);
      }
    }
    
    private VariableProposer makeVariableProposer(Model model, String varName) throws MC3KitException {
      Variable var = model.getVariable(varName);
      
      if(var == null) {
        throw new MC3KitException(format("No variable named %s", varName));
      }
      
      return var.makeProposer();
    }
  }
}
