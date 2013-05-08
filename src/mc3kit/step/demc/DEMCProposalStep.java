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

package mc3kit.step.demc;

import mc3kit.*;
import mc3kit.mcmc.Step;
import mc3kit.mcmc.Task;

import java.util.*;

/**
 * Differential evolution MCMC (DEMC) step.
 * 
 * Cajo J.F. ter Braak and Jasper A. Vrugt Differential Evolution Markov Chain
 * with snooker updater and fewer chains Stat Comput (2008) 18: 435--446 DOI
 * 10.1007/s11222-008-9104-9
 * 
 * The paper uses multiple chains; this implementation exists on a single chain
 * and uses historical samples exclusively to generate proposals.
 * 
 * The step will do the following: (1) If useParallel is true, then do standard
 * DEMC proposals (difference of historical samples). (2) If useParallel AND
 * useLarge is true, then also do double-size proposals. (3) If useSnooker is
 * on, also perform snooker proposals (project differences into a sensible
 * direction).
 * 
 * For each of these, proposals will be done at multiple block sizes:
 * minBlockSize, 2 * minBlockSize, 4 * minBlockSize, 8 * minBlockSize, ..., N
 * where N = minBlockSize * 2^n and N <= maxBlockSize.
 * 
 * If all three proposal types are on, then a single step will make 3m proposals
 * to each variable, where m = # of different block sizes.
 * 
 * targetAcceptanceRate What fraction of proposals should be accepted? tuneFor
 * How many iterations the tuning period lasts. tuneEvery How often to tune
 * within the tuning period. historyThin How many samples go by for every
 * historical sample recorded for the DEMC. initialHistoryCount The number of
 * historical samples to accumulate before doing DEMC. minBlockSize The smallest
 * number of variables to propose at a time. maxBlockSize The useParallel
 * useLarge useSnooker
 */
public class DEMCProposalStep implements Step {
	
	double targetAcceptanceRate = 0.25;
	long tuneFor = 1000;
	long tuneEvery = 100;
	long recordHistoryAfter = 5;
	long iterateAfter = 10;
	int minBlockSize = 16;
	int maxBlockSize = 128;
	boolean useParallel = true;
	boolean useLarge = true;
	boolean useSnooker = true;
	
	public DEMCProposalStep(long tuneFor, long tuneEvery,
			long recordHistoryAfter, long iterateAfter) {
	}
	
	public void setMinBlockSize(int minBlockSize) {
		this.minBlockSize = minBlockSize;
	}
	
	public void setMaxBlockSize(int maxBlockSize) {
		this.maxBlockSize = maxBlockSize;
	}
	
	public void setUseParallel(boolean useParallel) {
		this.useParallel = useParallel;
	}
	
	public void setUseLarge(boolean useLarge) {
		this.useLarge = useLarge;
	}
	
	public void setUseSnooker(boolean useSnooker) {
		this.useSnooker = useSnooker;
	}
	
	@Override
	public List<Task> makeTasks(int chainCount) throws MC3KitException {
		List<Task> tasks = new ArrayList<Task>();
		for(int i = 0; i < chainCount; i++) {
			tasks.add(new DEMCProposalTask(this, i));
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
