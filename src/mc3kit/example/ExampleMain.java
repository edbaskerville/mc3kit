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

package mc3kit.example;

import java.io.*;
import java.util.Date;

import com.google.gson.*;

import cern.jet.random.*;
import cern.jet.random.engine.*;
import mc3kit.mcmc.*;
import mc3kit.output.*;
import mc3kit.step.demc.*;
import mc3kit.step.statistics.*;
import mc3kit.step.swap.*;
import mc3kit.step.univariate.*;
import mc3kit.step.verification.*;

public class ExampleMain {
	/*
	 * For real code, you'll want to read these in from a configuration file
	 * (e.g., use Google Gson to read a JSON file).
	 */
	static final int chainCount = 8;
	static final long iterationCount = 10000;
	static final String sampleFilename = "samples.jsons";
	static final String plFilename = "prior_likelihood.txt";
	static final String mlFilename = "marginal_likelihood.txt";
	
	static final int dataSize = 100;
	static final double weight = 0.2;
	static final double[] means = { -0.5, 0.5 };
	static final double[] stdDevs = { 2.0, 1.0 };
	static final String dataFilename = "data.json";
	
	static final long thin = 100;
	static final long burnIn = 20000;
	static final long tuneEvery = 200;
	static final long recordHistoryAfter = 5000;
	static final long iterateAfter = 10000;
	
	static final double targetAcceptanceRate = 0.25;
	
	static final double heatPower = 3.0;
	
	static final long initialHistoryCount = 100;
	
	public static void main(String[] args) throws Throwable {
		// Use the first command-line argument as the number of iterations to
		// perform
		long iterationCount = Long.parseLong(args[0]);
		
		try {
			MCMC mcmc;
			
			// Make up some fake data from a mixture of two normals
			// and write it to a file (normally you'd just read real data)
			RandomEngine rng = new MersenneTwister(new Date());
			Normal normal = new Normal(0.0, 1.0, rng);
			double[] data = new double[dataSize];
			for(int i = 0; i < dataSize; i++) {
				int j = rng.nextDouble() < weight ? 0 : 1;
				data[i] = normal.nextDouble(means[j], stdDevs[j]);
			}
			PrintWriter writer = new PrintWriter(dataFilename);
			new GsonBuilder().setPrettyPrinting().create().toJson(data, writer);
			writer.println();
			writer.close();
			
			mcmc = new MCMC();
			
			// Object that will be asked to create model objects for each chain
			mcmc.setModelFactory(new ExampleModelFactory(data));
			
			// Number of chains
			mcmc.setChainCount(chainCount);
			
			// Chains will explore Prior * [Likelihood^tau]
			// where tau == x^heatPower
			// x == 0.0 for the hottest chain (chainId: chainCount - 1);
			// x == 1.0 for the coldest chain (chainId: 0)
			mcmc.setHeatFunction(heatPower);
			
			// Simple default Metropolis-Hastings step for each variable;
			// these will tune to try to reach the target acceptance rate during
			// the burn-in
			// period.
			//
			// Proposals are minimally intelligent:
			// - normal proposals for distributions with real support (e.g.,
			// normal)
			// - multiplier proposals for distributions with positive real
			// support (e.g., gamma)
			// - uniform proposals restricted to min, max for distributions with
			// finite support
			// (e.g., uniform, beta)
			// - Gibbs sample for binary-valued variables
			// A natural extension would be to automatically choose Gibbs
			// samplers
			// intelligently, but that's not in the current version.
			Step univarStep = new UnivariateProposalStep(targetAcceptanceRate,
					burnIn, tuneEvery);
			
			// "Differential evolution MCMC" step, which proposes changes to
			// multiple
			// variables simultaneously, taking into account their correlations
			// in a clever way.
			// Proposes at multiple scales: 8, 16, 32, ... variables at a time.
			// (Since this model only has 5 parameters, it'll just do all 5.)
			// For details of method and parameters, see source for
			// DEMCProposalStep and methods paper
			Step demcStep = new DEMCProposalStep(burnIn, tuneEvery,
					recordHistoryAfter, iterateAfter);
			
			// Swap steps: even (0/1, 2/3, 4/5) and odd (1/2, 3/4, 5/6);
			// alternating these sets of pairs of chains ensures up to
			// chainCount/2
			// parallelization while swapping, where
			// No tuning, but tuneEvery used to print swap statistics to log
			// file
			// every so often
			Step evenSwapStep = new SwapStep(ChainParity.EVEN, tuneEvery);
			Step oddSwapStep = new SwapStep(ChainParity.ODD, tuneEvery);
			
			// Verification step: just asks all models to recalculate
			// log prior, likelihood from scratch and compares to existing
			// value;
			// throws an exception if too much error has accumulated.
			Step verificationStep = new VerificationStep(thin, 1e-8);
			
			// Sample output step
			Step sampOutStep = new SampleOutputStep(sampleFilename, thin);
			
			// Prior-likelihood output step for after-run analysis
			Step plOutStep = new PriorLikelihoodOutputStep(plFilename, thin);
			
			// Marginal-likelihood calculation during run
			Step mlOutStep = new MarginalLikelihoodStep(mlFilename, burnIn,
					thin);
			
			// Assemble all steps into a sequence; repeat swaps chainCount times
			// since they're so cheap and beneficial for mixing.
			// Each iteration thus includes many little steps:
			// - Proposes changes to all individual parameters
			// - Proposes changes using parallel DEMC, double-size parallel
			// DEMC,
			// and snooker DEMC at multiple scales
			// - Proposes chainCount * 2 swaps
			// - Every thin iterations, writes samples to a file
			mcmc.addStep(univarStep);
			mcmc.addStep(demcStep);
			for(int i = 0; i < chainCount; i++) {
				mcmc.addStep(evenSwapStep);
				mcmc.addStep(oddSwapStep);
			}
			mcmc.addStep(verificationStep);
			mcmc.addStep(sampOutStep);
			mcmc.addStep(mlOutStep);
			mcmc.addStep(plOutStep);
			mcmc.runFor(iterationCount);
			
			// Tells the MCMC to stop the thread pool so this program will exit
			mcmc.shutdown();
		}
		catch(Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
