package mc3kit.mcmc;

import java.util.*;

import mc3kit.MC3KitException;

class FirstStep implements Step {
  public FirstStep() {
  }
  
  @Override
  public List<Task> makeTasks(int chainCount) throws MC3KitException {
    List<Task> tasks = new ArrayList<Task>();
    
    for(int i = 0; i < chainCount; i++) {
      tasks.add(new ChainFirstTask(i));
    }
    
    return tasks;
  }
  
  class ChainFirstTask implements Task {
    int chainId;
    
    ChainFirstTask(int chainId) {
      this.chainId = chainId;
    }
    
    @Override
    public int[] getChainIds() {
      return new int[] { chainId };
    }
    
    @Override
    public void step(Chain[] chains) throws MC3KitException {
      assert chains.length == 1;
      
      Chain chain = chains[0];
      
      if(!chain.initialized) {
        chain.initialize();
        chain.beginIteration();
      }
      else {
        if((chain.getIteration() % chain.getMCMC().getThin()) == 0) {
          chain.writeToDb();
        }
      }
      chain.endIteration();
      
      // Next iteration begins at the start of this step
      // so each chain's progress is atomic
      chain.beginIteration();
    }
  }
}
