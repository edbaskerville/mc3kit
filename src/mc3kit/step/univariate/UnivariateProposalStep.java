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

package mc3kit.step.univariate;

import java.util.*;
import mc3kit.*;
import mc3kit.mcmc.Step;
import mc3kit.mcmc.Task;

public class UnivariateProposalStep implements Step {
	
	double targetAcceptanceRate;
	long tuneFor;
	long tuneEvery;
	
	public UnivariateProposalStep(double targetAcceptanceRate, long tuneFor,
			long tuneEvery) {
		this.targetAcceptanceRate = targetAcceptanceRate;
		this.tuneFor = tuneFor;
		this.tuneEvery = tuneEvery;
	}
	
	@Override
	public List<Task> makeTasks(int chainCount) throws MC3KitException {
		List<Task> tasks = new ArrayList<Task>();
		for(int i = 0; i < chainCount; i++) {
			tasks.add(new UnivariateProposalTask(this, i));
		}
		return tasks;
	}
	
	String getTableName() {
		return getClass().getSimpleName();
	}
	
	String getIndexName() {
		return getTableName() + "_iterationIndex";
	}
}
