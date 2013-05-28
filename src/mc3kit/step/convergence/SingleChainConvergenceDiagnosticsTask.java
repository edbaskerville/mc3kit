package mc3kit.step.convergence;

import mc3kit.MC3KitException;
import mc3kit.mcmc.Chain;
import mc3kit.mcmc.Task;

public class SingleChainConvergenceDiagnosticsTask implements Task {
	int chainId;
	
	public SingleChainConvergenceDiagnosticsTask(int chainId) {
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
		
		// 
	}
	
}
