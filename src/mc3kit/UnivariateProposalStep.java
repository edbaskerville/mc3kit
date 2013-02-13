package mc3kit;

import static mc3kit.util.Math.getRandomPermutation;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;

public class UnivariateProposalStep implements Step {
  boolean initialized;
  
  double targetAcceptanceRate = 0.25;
  long tuneFor = 1000;
  long tuneEvery = 100;

  public UnivariateProposalStep() {
  }

  @Override
  public List<Task> makeTasks(int chainCount) throws MC3KitException {
    List<Task> tasks = new ArrayList<Task>();
    for(int i = 0; i < chainCount; i++) {
      tasks.add(new UnivariateProposalTask(i));
    }
    return tasks;
  }
  
  /*** GETTERS & SETTERS ***/
  
  public double getTargetAcceptanceRate() {
    return targetAcceptanceRate;
  }

  public void setTargetAcceptanceRate(double targetAcceptanceRate)
      throws MC3KitException {
    throwIfInitialized();
    this.targetAcceptanceRate = targetAcceptanceRate;
  }

  public long getTuneFor() {
    return tuneFor;
  }

  public void setTuneFor(long tuneFor) throws MC3KitException {
    throwIfInitialized();
    this.tuneFor = tuneFor;
  }

  public long getTuneEvery() {
    return tuneEvery;
  }

  public void setTuneEvery(long tuneEvery) throws MC3KitException {
    throwIfInitialized();
    this.tuneEvery = tuneEvery;
  }

  private void throwIfInitialized() throws MC3KitException {
    if(initialized)
      throw new MC3KitException("Already initialized.");
  }
  
  /*** TASK INTERFACE IMPLEMENTATION ***/
  
  class UnivariateProposalTask implements Task {
    int chainId;
    
    boolean initialized;
    private long iterationCount;
    
    @SuppressWarnings("rawtypes")
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

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void step(Chain[] chains) throws MC3KitException {
      assert (chains.length == 1);
      
      System.err.println("UnivariateProposalStep stepping");

      Chain chain = chains[0];
      Model model = chain.getModel();
      initialize(model);

      double priorHeatExp = chain.getPriorHeatExponent();
      double likeHeatExp = chain.getLikelihoodHeatExponent();
      RandomEngine rng = chain.getRng();

      chain.getLogger().trace("univariate stepping %d\n", chainId);

      Uniform unif = new Uniform(rng);

      // Run all proposers in random order
      for(int i : getRandomPermutation(proposers.length, unif)) {
        proposers[i].step(model, priorHeatExp, likeHeatExp, rng);
      }

      iterationCount++;

      // If we're still in the tuning period, tune
      if((iterationCount <= tuneFor) && iterationCount % tuneEvery == 0) {
        for(VariableProposer proposer : proposers) {
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
    
    private VariableProposer<?> makeVariableProposer(Model model, String varName) {
      Distribution<?> dist = model.getDistributionForVariable(varName);
      if(dist == null) {
        return null;
      }
      return dist.makeVariableProposer(varName);
    }
  }
}
