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

package mc3kit.mcmc;

import java.util.Map;
import java.util.logging.Level;

import org.junit.*;

import static org.junit.Assert.*;

import mc3kit.MC3KitException;
import mc3kit.distributions.*;
import mc3kit.functions.DoubleSumFunction;
import mc3kit.model.Model;
import mc3kit.model.ModelFactory;
import mc3kit.step.univariate.UnivariateProposalStep;
import mc3kit.types.doublevalue.DoubleDistribution;
import mc3kit.types.doublevalue.DoubleVariable;
import mc3kit.types.doublevalue.distributions.BetaDistribution;
import mc3kit.types.doublevalue.distributions.ExponentialDistribution;
import mc3kit.types.doublevalue.distributions.NormalDistribution;
import mc3kit.types.doublevalue.distributions.UniformDistribution;
import static java.lang.Math.*;

public class MCMCTest {
	
	@Before
	public void setUp() throws Exception {
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testStandardNormal() throws Throwable {
		long burnIn = 5000;
		long iterCount = 10000;
		
		MCMC mcmc = new MCMC();
		mcmc.setRandomSeed(100L);
		
		mcmc.setLogLevel(Level.INFO);
		
		ModelFactory mf = new ModelFactory() {
			@Override
			public Model createModel(Chain initialChain) throws MC3KitException {
				Model m = new Model(initialChain);
				
				m.beginConstruction();
				new DoubleVariable(m, "nv", new NormalDistribution(m, "nd"));
				m.endConstruction();
				
				return m;
			}
			
			@Override
			public Model createModel(Chain initialChain,
					Map<String, Object> sample) throws MC3KitException {
				return null;
			}
		};
		
		mcmc.setModelFactory(mf);
		
		UnivariateProposalStep proposalStep = new UnivariateProposalStep(0.25,
				100, burnIn);
		mcmc.addStep(proposalStep);
		
		// Run, collect statistics, and check moments against expected
		// distribution
		double sum = 0;
		double sumSq = 0;
		for(long i = 0; i < iterCount; i++) {
			mcmc.step();
			mcmc.getModel().recalculate(1e-8);
			
			assertEquals(i + 1, mcmc.getIterationCount());
			
			if(i >= burnIn) {
				double val = mcmc.getModel().getDoubleVariable("nv").getValue();
				sum += val;
				sumSq += val * val;
			}
		}
		
		double N = iterCount - burnIn;
		
		double mean = sum / N;
		System.err.printf("mean = %f\n", mean);
		assertEquals(0.0, mean, 0.02);
		
		double sd = sqrt(N / (N - 1) * (sumSq / N - mean * mean));
		System.err.printf("sd = %f\n", sd);
		assertEquals(1.0, sd, 0.02);
	}
	
	@Test
	public void testStandardExponential() throws Throwable {
		long burnIn = 10000;
		long iterCount = 50000;
		
		MCMC mcmc = new MCMC();
		mcmc.setRandomSeed(1453L);
		
		mcmc.setModelFactory(new ModelFactory() {
			@Override
			public Model createModel(Chain initialChain) throws MC3KitException {
				Model m = new Model(initialChain);
				
				m.beginConstruction();
				new DoubleVariable(m, "ev",
						new ExponentialDistribution(m, "ed"));
				m.endConstruction();
				
				return m;
			}

			@Override
			public Model createModel(Chain initialChain,
					Map<String, Object> sample) throws MC3KitException {
				return null;
			}
		});
		
		UnivariateProposalStep proposalStep = new UnivariateProposalStep(0.25,
				100, burnIn);
		mcmc.addStep(proposalStep);
		
		// Run, collect statistics, and check moments against expected
		// distribution
		double sum = 0;
		double sumSq = 0;
		for(long i = 0; i < iterCount; i++) {
			mcmc.step();
			mcmc.getModel().recalculate(1e-8);
			
			assertEquals(i + 1, mcmc.getIterationCount());
			
			if(i >= burnIn) {
				double val = mcmc.getModel().getDoubleVariable("ev").getValue();
				sum += val;
				sumSq += val * val;
			}
		}
		
		double N = iterCount - burnIn;
		
		double mean = sum / N;
		System.err.printf("mean = %f\n", mean);
		assertEquals(1.0, mean, 0.02);
		
		double var = N / (N - 1) * (sumSq / N - mean * mean);
		System.err.printf("var = %f\n", var);
		assertEquals(1.0, var, 0.02);
	}
	
	@Test
	public void testStandardUniform() throws Throwable {
		long burnIn = 5000;
		long iterCount = 10000;
		
		MCMC mcmc = new MCMC();
		mcmc.setRandomSeed(100L);
		
		mcmc.setModelFactory(new ModelFactory() {
			@Override
			public Model createModel(Chain initialChain) throws MC3KitException {
				Model m = new Model(initialChain);
				
				m.beginConstruction();
				new DoubleVariable(m, "ev", new UniformDistribution(m, "ed"));
				m.endConstruction();
				
				return m;
			}

			@Override
			public Model createModel(Chain initialChain,
					Map<String, Object> sample) throws MC3KitException {
				return null;
			}
		});
		
		UnivariateProposalStep proposalStep = new UnivariateProposalStep(0.25,
				100, burnIn);
		mcmc.addStep(proposalStep);
		
		// Run, collect statistics, and check moments against expected
		// distribution
		double sum = 0;
		double sumSq = 0;
		for(long i = 0; i < iterCount; i++) {
			mcmc.step();
			mcmc.getModel().recalculate(1e-8);
			
			assertEquals(i + 1, mcmc.getIterationCount());
			
			if(i >= burnIn) {
				double val = mcmc.getModel().getDoubleVariable("ev").getValue();
				assertTrue(val > 0.0);
				assertTrue(val < 1.0);
				sum += val;
				sumSq += val * val;
			}
		}
		
		double N = iterCount - burnIn;
		
		double mean = sum / N;
		System.err.printf("mean = %f\n", mean);
		assertEquals(0.5, mean, 0.02);
		
		double var = N / (N - 1) * (sumSq / N - mean * mean);
		System.err.printf("var = %f\n", var);
		assertEquals(1 / 12.0, var, 0.01);
	}
	
	@Test
	public void testBeta() throws Throwable {
		long burnIn = 5000;
		long iterCount = 10000;
		
		MCMC mcmc = new MCMC();
		mcmc.setRandomSeed(100L);
		
		mcmc.setModelFactory(new ModelFactory() {
			
			@Override
			public Model createModel(Chain initialChain) throws MC3KitException {
				Model m = new Model(initialChain);
				
				m.beginConstruction();
				new DoubleVariable(m, "v", new BetaDistribution(m, "d", 2.0,
						3.0));
				m.endConstruction();
				
				return m;
			}

			@Override
			public Model createModel(Chain initialChain,
					Map<String, Object> sample) throws MC3KitException {
				return null;
			}
		});
		
		UnivariateProposalStep proposalStep = new UnivariateProposalStep(0.25,
				100, burnIn);
		mcmc.addStep(proposalStep);
		
		// Run, collect statistics, and check moments against expected
		// distribution
		double sum = 0;
		double sumSq = 0;
		for(long i = 0; i < iterCount; i++) {
			mcmc.step();
			mcmc.getModel().recalculate(1e-8);
			
			assertEquals(i + 1, mcmc.getIterationCount());
			
			if(i >= burnIn) {
				double val = mcmc.getModel().getDoubleVariable("v").getValue();
				assertTrue(val > 0.0);
				assertTrue(val < 1.0);
				sum += val;
				sumSq += val * val;
			}
		}
		
		double N = iterCount - burnIn;
		
		double mean = sum / N;
		System.err.printf("mean = %f\n", mean);
		assertEquals(0.4, mean, 0.02);
		
		double var = N / (N - 1) * (sumSq / N - mean * mean);
		System.err.printf("var = %f\n", var);
		assertEquals(5.0 / (5 * 5 * 6.0), var, 0.01);
	}
	
	@Test
	public void testSumNormals() throws Throwable {
		long burnIn = 10000;
		long iterCount = 100000;
		
		MCMC mcmc = new MCMC();
		mcmc.setRandomSeed(100L);
		
		mcmc.setModelFactory(new ModelFactory() {
			
			@Override
			public Model createModel(Chain initialChain) throws MC3KitException {
				Model m = new Model(initialChain);
				
				m.beginConstruction();
				DoubleDistribution d = new NormalDistribution(m);
				DoubleVariable v1 = new DoubleVariable(m, "v1", d);
				DoubleVariable v2 = new DoubleVariable(m, "v2", d);
				new DoubleSumFunction(m, "v12").add(v1).add(v2);
				m.endConstruction();
				
				return m;
			}

			@Override
			public Model createModel(Chain initialChain,
					Map<String, Object> sample) throws MC3KitException {
				return null;
			}
		});
		
		UnivariateProposalStep proposalStep = new UnivariateProposalStep(0.25,
				100, burnIn);
		mcmc.addStep(proposalStep);
		
		// Run, collect statistics, and check moments against expected
		// distribution
		double sum = 0;
		double sumSq = 0;
		for(long i = 0; i < iterCount; i++) {
			mcmc.step();
			mcmc.getModel().recalculate(1e-8);
			
			assertEquals(i + 1, mcmc.getIterationCount());
			
			if(i >= burnIn) {
				double val = mcmc.getModel().getDoubleFunction("v12")
						.getValue();
				sum += val;
				sumSq += val * val;
			}
		}
		
		double N = iterCount - burnIn;
		
		double mean = sum / N;
		System.err.printf("mean = %f\n", mean);
		assertEquals(0.0, mean, 0.02);
		
		double var = N / (N - 1) * (sumSq / N - mean * mean);
		System.err.printf("var = %f\n", var);
		assertEquals(2.0, var, 0.02);
	}
}
