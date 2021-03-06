/***
  This file is part of mc3kit.
  
  Copyright (C) 2013 Edward B. Baskerville

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ***/

package mc3kit.step.verification;

import java.util.*;

import mc3kit.MC3KitException;
import mc3kit.mcmc.Chain;
import mc3kit.mcmc.Step;
import mc3kit.mcmc.Task;

public class VerificationStep implements Step {
	long runEvery;
	double tol;
	
	protected VerificationStep() {
	}
	
	public VerificationStep(long runEvery, double tolerance) {
		this.runEvery = runEvery;
		this.tol = tolerance;
	}
	
	@Override
	public List<Task> makeTasks(int chainCount) throws MC3KitException {
		List<Task> tasks = new ArrayList<Task>();
		for(int i = 0; i < chainCount; i++) {
			tasks.add(new VerificationTask(i));
		}
		return tasks;
	}
	
	/*** TASK INTERFACE IMPLEMENTATION ***/
	
	class VerificationTask implements Task {
		int chainId;
		long iterationCount;
		
		/*** CONSTRUCTOR ***/
		
		public VerificationTask(int chainId) {
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
			iterationCount++;
			if(iterationCount % runEvery == 0) {
				chains[0].getModel().recalculate(tol);
			}
		}
	}
}
