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

//import static java.lang.Math.sqrt;
//
//import static org.junit.Assert.*;
//
//import mc3kit.*;
//import mc3kit.mcmc.Chain;
//import mc3kit.mcmc.ChainParity;
//import mc3kit.mcmc.MCMC;
//import mc3kit.model.Model;
//import mc3kit.model.ModelFactory;
//import mc3kit.step.swap.SwapStep;
//import mc3kit.step.univariate.UnivariateProposalStep;
//import mc3kit.types.doublevalue.DoubleVariable;
//import mc3kit.types.doublevalue.distributions.NormalDistribution;

import org.junit.After;
import org.junit.Before;

//import org.junit.Test;

public class DEMCProposalStepTest {
	
	@Before
	public void setUp() throws Exception {
	}
	
	@After
	public void tearDown() throws Exception {
	}
	/*
	 * @Test public void testTwoStandardNormalsParallel() throws Throwable {
	 * long burnIn = 5000; long iterCount = 40000;
	 * 
	 * MCMC mcmc = new MCMC(); mcmc.setRandomSeed(100L);
	 * 
	 * ModelFactory mf = new ModelFactory() {
	 * 
	 * @Override public Model createModel(Chain initialChain) throws
	 * MC3KitException { Model m = new Model(initialChain);
	 * 
	 * m.beginConstruction(); NormalDistribution d = new NormalDistribution(m,
	 * "d"); new DoubleVariable(m, "v0", d); new DoubleVariable(m, "v1", d);
	 * m.endConstruction();
	 * 
	 * return m; } };
	 * 
	 * mcmc.setModelFactory(mf);
	 * 
	 * UnivariateProposalStep uniStep = new UnivariateProposalStep(0.25, 100,
	 * burnIn); mcmc.addStep(uniStep);
	 * 
	 * DEMCProposalStep demcStep = new DEMCProposalStep(0.25, 100, burnIn, 10,
	 * 100, 0, 2, 2, true, false, false); demcStep.setUseParallel(true);
	 * demcStep.setUseLarge(false); demcStep.setUseSnooker(false);
	 * mcmc.addStep(demcStep);
	 * 
	 * // Run, collect statistics, and check moments against expected
	 * distribution double[] sum = new double[2]; double[] sumSq = new
	 * double[2]; for(long i = 0; i < iterCount; i++) { mcmc.step();
	 * mcmc.getModel().recalculate(1e-8);
	 * 
	 * assertEquals(i + 1, mcmc.getIterationCount());
	 * 
	 * if(i >= burnIn) { for(int j = 0; j < 2; j++) { double val =
	 * mcmc.getModel().getDoubleVariable("v" + j).getValue(); sum[j] += val;
	 * sumSq[j] += val * val; } } }
	 * 
	 * double N = iterCount - burnIn;
	 * 
	 * for(int i = 0; i < 2; i++) { double mean = sum[i] / N;
	 * System.err.printf("mean %d = %f\n", i, mean); assertEquals(0.0, mean,
	 * 0.02);
	 * 
	 * double sd = sqrt(N / (N - 1) * (sumSq[i]/N - mean * mean));
	 * System.err.printf("sd %d = %f\n", i, sd); assertEquals(1.0, sd, 0.02); }
	 * }
	 * 
	 * @Test public void testTwoStandardNormalsLarge() throws Throwable { long
	 * burnIn = 5000; long iterCount = 40000;
	 * 
	 * MCMC mcmc = new MCMC(); mcmc.setRandomSeed(100L);
	 * 
	 * ModelFactory mf = new ModelFactory() {
	 * 
	 * @Override public Model createModel(Chain initialChain) throws
	 * MC3KitException { Model m = new Model(initialChain);
	 * 
	 * m.beginConstruction(); NormalDistribution d = new NormalDistribution(m,
	 * "d"); new DoubleVariable(m, "v0", d); new DoubleVariable(m, "v1", d);
	 * m.endConstruction();
	 * 
	 * return m; } };
	 * 
	 * mcmc.setModelFactory(mf);
	 * 
	 * UnivariateProposalStep uniStep = new UnivariateProposalStep(0.25, 100,
	 * burnIn); mcmc.addStep(uniStep);
	 * 
	 * DEMCProposalStep demcStep = new DEMCProposalStep(0.25, 100, burnIn, 10,
	 * 100, 0, 2, 2, true, true, false); mcmc.addStep(demcStep);
	 * 
	 * // Run, collect statistics, and check moments against expected
	 * distribution double[] sum = new double[2]; double[] sumSq = new
	 * double[2]; for(long i = 0; i < iterCount; i++) { mcmc.step();
	 * mcmc.getModel().recalculate(1e-8);
	 * 
	 * assertEquals(i + 1, mcmc.getIterationCount());
	 * 
	 * if(i >= burnIn) { for(int j = 0; j < 2; j++) { double val =
	 * mcmc.getModel().getDoubleVariable("v" + j).getValue(); sum[j] += val;
	 * sumSq[j] += val * val; } } }
	 * 
	 * double N = iterCount - burnIn;
	 * 
	 * for(int i = 0; i < 2; i++) { double mean = sum[i] / N;
	 * System.err.printf("mean %d = %f\n", i, mean); assertEquals(0.0, mean,
	 * 0.02);
	 * 
	 * double sd = sqrt(N / (N - 1) * (sumSq[i]/N - mean * mean));
	 * System.err.printf("sd %d = %f\n", i, sd); assertEquals(1.0, sd, 0.02); }
	 * }
	 * 
	 * @Test public void testTwoStandardNormalsSnooker() throws Throwable { long
	 * burnIn = 5000; long iterCount = 40000;
	 * 
	 * MCMC mcmc = new MCMC(); mcmc.setRandomSeed(100L);
	 * 
	 * ModelFactory mf = new ModelFactory() {
	 * 
	 * @Override public Model createModel(Chain initialChain) throws
	 * MC3KitException { Model m = new Model(initialChain);
	 * 
	 * m.beginConstruction(); NormalDistribution d = new NormalDistribution(m,
	 * "d"); new DoubleVariable(m, "v0", d); new DoubleVariable(m, "v1", d);
	 * m.endConstruction();
	 * 
	 * return m; } };
	 * 
	 * mcmc.setModelFactory(mf);
	 * 
	 * UnivariateProposalStep uniStep = new UnivariateProposalStep(0.25, 100,
	 * burnIn); mcmc.addStep(uniStep);
	 * 
	 * DEMCProposalStep demcStep = new DEMCProposalStep(0.25, 100, burnIn, 10,
	 * 100, 0, 2, 2, false, false, true); mcmc.addStep(demcStep);
	 * 
	 * // Run, collect statistics, and check moments against expected
	 * distribution double[] sum = new double[2]; double[] sumSq = new
	 * double[2]; for(long i = 0; i < iterCount; i++) { mcmc.step();
	 * mcmc.getModel().recalculate(1e-8);
	 * 
	 * assertEquals(i + 1, mcmc.getIterationCount());
	 * 
	 * if(i >= burnIn) { for(int j = 0; j < 2; j++) { double val =
	 * mcmc.getModel().getDoubleVariable("v" + j).getValue(); sum[j] += val;
	 * sumSq[j] += val * val; } } }
	 * 
	 * double N = iterCount - burnIn;
	 * 
	 * for(int i = 0; i < 2; i++) { double mean = sum[i] / N;
	 * System.err.printf("mean %d = %f\n", i, mean); assertEquals(0.0, mean,
	 * 0.02);
	 * 
	 * double sd = sqrt(N / (N - 1) * (sumSq[i]/N - mean * mean));
	 * System.err.printf("sd %d = %f\n", i, sd); assertEquals(1.0, sd, 0.02); }
	 * }
	 * 
	 * @Test public void testTwoStandardNormalsManyChains() throws Throwable {
	 * long burnIn = 5000; long iterCount = 80000;
	 * 
	 * int chainCount = 16;
	 * 
	 * MCMC mcmc = new MCMC(); mcmc.setRandomSeed(100L);
	 * mcmc.setChainCount(chainCount); mcmc.setHeatFunction(1.0, 0.0, 3.0, 0.1);
	 * 
	 * ModelFactory mf = new ModelFactory() {
	 * 
	 * @Override public Model createModel(Chain initialChain) throws
	 * MC3KitException { Model m = new Model(initialChain);
	 * 
	 * m.beginConstruction(); NormalDistribution d = new NormalDistribution(m,
	 * "d"); new DoubleVariable(m, "v0", d); new DoubleVariable(m, "v1", d);
	 * m.endConstruction();
	 * 
	 * return m; } };
	 * 
	 * mcmc.setModelFactory(mf);
	 * 
	 * UnivariateProposalStep uniStep = new UnivariateProposalStep(0.25, 100,
	 * burnIn); DEMCProposalStep demcStep = new DEMCProposalStep(0.25, 100,
	 * burnIn, 10, 100, 0, 2, 2, false, false, true); SwapStep evenStep = new
	 * SwapStep(ChainParity.EVEN, 100 * chainCount); SwapStep oddStep = new
	 * SwapStep(ChainParity.ODD, 100 * chainCount);
	 * 
	 * mcmc.addStep(uniStep); for(int i = 0; i < chainCount; i++) {
	 * mcmc.addStep(evenStep); mcmc.addStep(oddStep); } mcmc.addStep(demcStep);
	 * 
	 * // Run, collect statistics, and check moments against expected
	 * distribution double[] sum = new double[2]; double[] sumSq = new
	 * double[2]; for(long i = 0; i < iterCount; i++) { mcmc.runFor(1);
	 * 
	 * assertEquals(i + 1, mcmc.getIterationCount());
	 * 
	 * if(i >= burnIn) { for(int j = 0; j < 2; j++) { double val =
	 * mcmc.getModel().getDoubleVariable("v" + j).getValue(); sum[j] += val;
	 * sumSq[j] += val * val; } } }
	 * 
	 * double N = iterCount - burnIn;
	 * 
	 * for(int i = 0; i < 2; i++) { double mean = sum[i] / N;
	 * System.err.printf("mean %d = %f\n", i, mean); assertEquals(0.0, mean,
	 * 0.02);
	 * 
	 * double sd = sqrt(N / (N - 1) * (sumSq[i]/N - mean * mean));
	 * System.err.printf("sd %d = %f\n", i, sd); assertEquals(1.0, sd, 0.02); }
	 * }
	 * 
	 * @Test public void testTwoStandardNormalsManyChainsSnooker() throws
	 * Throwable { long burnIn = 5000; long iterCount = 80000;
	 * 
	 * int chainCount = 16;
	 * 
	 * MCMC mcmc = new MCMC(); mcmc.setRandomSeed(100L);
	 * mcmc.setChainCount(chainCount); mcmc.setHeatFunction(1.0, 0.0, 3.0, 0.1);
	 * 
	 * ModelFactory mf = new ModelFactory() {
	 * 
	 * @Override public Model createModel(Chain initialChain) throws
	 * MC3KitException { Model m = new Model(initialChain);
	 * 
	 * m.beginConstruction(); NormalDistribution d = new NormalDistribution(m,
	 * "d"); new DoubleVariable(m, "v0", d); new DoubleVariable(m, "v1", d);
	 * m.endConstruction();
	 * 
	 * return m; } };
	 * 
	 * mcmc.setModelFactory(mf);
	 * 
	 * UnivariateProposalStep uniStep = new UnivariateProposalStep(0.25, 100,
	 * burnIn); DEMCProposalStep demcStep = new DEMCProposalStep(0.25, 100,
	 * burnIn, 10, 100, 0, 2, 2, false, false, true); SwapStep evenStep = new
	 * SwapStep(ChainParity.EVEN, 100 * chainCount); SwapStep oddStep = new
	 * SwapStep(ChainParity.ODD, 100 * chainCount);
	 * 
	 * mcmc.addStep(uniStep); for(int i = 0; i < chainCount; i++) {
	 * mcmc.addStep(evenStep); mcmc.addStep(oddStep); } mcmc.addStep(demcStep);
	 * 
	 * // Run, collect statistics, and check moments against expected
	 * distribution double[] sum = new double[2]; double[] sumSq = new
	 * double[2]; for(long i = 0; i < iterCount; i++) { mcmc.runFor(1);
	 * 
	 * assertEquals(i + 1, mcmc.getIterationCount());
	 * 
	 * if(i >= burnIn) { for(int j = 0; j < 2; j++) { double val =
	 * mcmc.getModel().getDoubleVariable("v" + j).getValue(); sum[j] += val;
	 * sumSq[j] += val * val; } } }
	 * 
	 * double N = iterCount - burnIn;
	 * 
	 * for(int i = 0; i < 2; i++) { double mean = sum[i] / N;
	 * System.err.printf("mean %d = %f\n", i, mean); assertEquals(0.0, mean,
	 * 0.02);
	 * 
	 * double sd = sqrt(N / (N - 1) * (sumSq[i]/N - mean * mean));
	 * System.err.printf("sd %d = %f\n", i, sd); assertEquals(1.0, sd, 0.02); }
	 * }
	 * 
	 * @Test public void testManyStandardNormals() throws Throwable { long
	 * burnIn = 5000; long iterCount = 80000;
	 * 
	 * final int varCount = 64;
	 * 
	 * MCMC mcmc = new MCMC(); mcmc.setRandomSeed(100L); mcmc.setChainCount(1);
	 * mcmc.setHeatFunction(3.0, 0.0);
	 * 
	 * ModelFactory mf = new ModelFactory() {
	 * 
	 * @Override public Model createModel(Chain initialChain) throws
	 * MC3KitException { Model m = new Model(initialChain);
	 * 
	 * m.beginConstruction(); NormalDistribution d = new NormalDistribution(m,
	 * "d");
	 * 
	 * for(int i = 0; i < varCount; i++) { new DoubleVariable(m, "v" + i, d); }
	 * m.endConstruction();
	 * 
	 * return m; } };
	 * 
	 * mcmc.setModelFactory(mf);
	 * 
	 * UnivariateProposalStep uniStep = new UnivariateProposalStep(0.25, 100,
	 * burnIn); mcmc.addStep(uniStep);
	 * 
	 * DEMCProposalStep demcStep = new DEMCProposalStep(0.25, 100, burnIn, 10,
	 * 100, 0, 2, varCount, true, false, false); mcmc.addStep(demcStep);
	 * 
	 * // Run, collect statistics, and check moments against expected
	 * distribution double[] sum = new double[64]; double[] sumSq = new
	 * double[64]; for(long i = 0; i < iterCount; i++) { mcmc.runFor(1);
	 * 
	 * assertEquals(i + 1, mcmc.getIterationCount());
	 * 
	 * if(i >= burnIn) { for(int j = 0; j < varCount; j++) { double val =
	 * mcmc.getModel().getDoubleVariable("v" + j).getValue(); sum[j] += val;
	 * sumSq[j] += val * val; } } }
	 * 
	 * double N = iterCount - burnIn;
	 * 
	 * for(int i = 0; i < varCount; i++) { double mean = sum[i] / N;
	 * System.err.printf("mean %d = %f\n", i, mean); assertEquals(0.0, mean,
	 * 0.02);
	 * 
	 * double sd = sqrt(N / (N - 1) * (sumSq[i]/N - mean * mean));
	 * System.err.printf("sd %d = %f\n", i, sd); assertEquals(1.0, sd, 0.02); }
	 * }
	 * 
	 * @Test public void testManyStandardNormalsSnooker() throws Throwable {
	 * long burnIn = 20000; long iterCount = 80000;
	 * 
	 * final int varCount = 64;
	 * 
	 * MCMC mcmc = new MCMC(); mcmc.setRandomSeed(100L); mcmc.setChainCount(1);
	 * mcmc.setHeatFunction(3.0, 0.0);
	 * 
	 * ModelFactory mf = new ModelFactory() {
	 * 
	 * @Override public Model createModel(Chain initialChain) throws
	 * MC3KitException { Model m = new Model(initialChain);
	 * 
	 * m.beginConstruction(); NormalDistribution d = new NormalDistribution(m,
	 * "d");
	 * 
	 * for(int i = 0; i < varCount; i++) { new DoubleVariable(m, "v" + i, d); }
	 * m.endConstruction();
	 * 
	 * return m; } };
	 * 
	 * mcmc.setModelFactory(mf);
	 * 
	 * UnivariateProposalStep uniStep = new UnivariateProposalStep(0.25, burnIn,
	 * 100); mcmc.addStep(uniStep);
	 * 
	 * DEMCProposalStep demcStep = new DEMCProposalStep(0.25, burnIn, 200, 10,
	 * 100, 0, 2, varCount, false, false, true); mcmc.addStep(demcStep);
	 * 
	 * // Run, collect statistics, and check moments against expected
	 * distribution double[] sum = new double[varCount]; double[] sumSq = new
	 * double[varCount]; for(long i = 0; i < iterCount; i++) { mcmc.runFor(1);
	 * 
	 * assertEquals(i + 1, mcmc.getIterationCount());
	 * 
	 * if(i >= burnIn) { for(int j = 0; j < varCount; j++) { double val =
	 * mcmc.getModel().getDoubleVariable("v" + j).getValue(); sum[j] += val;
	 * sumSq[j] += val * val; } } }
	 * 
	 * double N = iterCount - burnIn;
	 * 
	 * double[] mean = new double[varCount]; double[] sd = new double[varCount];
	 * for(int i = 0; i < varCount; i++) { mean[i] = sum[i] / N;
	 * System.err.printf("mean %d = %f\n", i, mean[i]);
	 * 
	 * sd[i] = sqrt(N / (N - 1) * (sumSq[i]/N - mean[i] * mean[i]));
	 * System.err.printf("sd %d = %f\n", i, sd[i]); }
	 * 
	 * for(int i = 0; i < varCount; i++) { assertEquals(0.0, mean[i], 0.02);
	 * assertEquals(1.0, sd[i], 0.02); } }
	 * 
	 * @Test public void testManyStandardNormalsManyChains() throws Throwable {
	 * long burnIn = 5000; long iterCount = 80000;
	 * 
	 * final int varCount = 64;
	 * 
	 * int chainCount = 16;
	 * 
	 * MCMC mcmc = new MCMC(); mcmc.setRandomSeed(100L);
	 * mcmc.setChainCount(chainCount); mcmc.setHeatFunction(1.0, 0.0, 3.0, 0.1);
	 * 
	 * ModelFactory mf = new ModelFactory() {
	 * 
	 * @Override public Model createModel(Chain initialChain) throws
	 * MC3KitException { Model m = new Model(initialChain);
	 * 
	 * m.beginConstruction(); NormalDistribution d = new NormalDistribution(m,
	 * "d");
	 * 
	 * for(int i = 0; i < varCount; i++) { new DoubleVariable(m, "v" + i, d); }
	 * m.endConstruction();
	 * 
	 * return m; } };
	 * 
	 * mcmc.setModelFactory(mf);
	 * 
	 * UnivariateProposalStep uniStep = new UnivariateProposalStep(0.25, 100,
	 * burnIn); DEMCProposalStep demcStep = new DEMCProposalStep(0.25, 100,
	 * burnIn, 10, 100, 0, varCount/4, varCount, true, false, false); SwapStep
	 * evenStep = new SwapStep(ChainParity.EVEN, 100 * chainCount); SwapStep
	 * oddStep = new SwapStep(ChainParity.ODD, 100 * chainCount);
	 * 
	 * mcmc.addStep(uniStep); for(int i = 0; i < chainCount; i++) {
	 * mcmc.addStep(evenStep); mcmc.addStep(oddStep); } mcmc.addStep(demcStep);
	 * 
	 * // Run, collect statistics, and check moments against expected
	 * distribution double[] sum = new double[varCount]; double[] sumSq = new
	 * double[varCount]; for(long i = 0; i < iterCount; i++) { mcmc.runFor(1);
	 * 
	 * assertEquals(i + 1, mcmc.getIterationCount());
	 * 
	 * if(i >= burnIn) { for(int j = 0; j < varCount; j++) { double val =
	 * mcmc.getModel().getDoubleVariable("v" + j).getValue(); sum[j] += val;
	 * sumSq[j] += val * val; } } }
	 * 
	 * double N = iterCount - burnIn;
	 * 
	 * double[] mean = new double[varCount]; double[] sd = new double[varCount];
	 * for(int i = 0; i < varCount; i++) { mean[i] = sum[i] / N;
	 * System.err.printf("mean %d = %f\n", i, mean[i]);
	 * 
	 * sd[i] = sqrt(N / (N - 1) * (sumSq[i]/N - mean[i] * mean[i]));
	 * System.err.printf("sd %d = %f\n", i, sd[i]); }
	 * 
	 * for(int i = 0; i < varCount; i++) { assertEquals(0.0, mean[i], 0.02);
	 * assertEquals(1.0, sd[i], 0.02); } }
	 * 
	 * @Test public void testManyStandardNormalsManyChainsSnooker() throws
	 * Throwable { long burnIn = 5000; long iterCount = 80000;
	 * 
	 * final int varCount = 64;
	 * 
	 * int chainCount = 16;
	 * 
	 * MCMC mcmc = new MCMC(); mcmc.setRandomSeed(99L);
	 * mcmc.setChainCount(chainCount); mcmc.setHeatFunction(1.0, 0.0, 3.0, 0.1);
	 * 
	 * ModelFactory mf = new ModelFactory() {
	 * 
	 * @Override public Model createModel(Chain initialChain) throws
	 * MC3KitException { Model m = new Model(initialChain);
	 * 
	 * m.beginConstruction(); NormalDistribution d = new NormalDistribution(m,
	 * "d");
	 * 
	 * for(int i = 0; i < varCount; i++) { new DoubleVariable(m, "v" + i, d); }
	 * m.endConstruction();
	 * 
	 * return m; } };
	 * 
	 * mcmc.setModelFactory(mf);
	 * 
	 * UnivariateProposalStep uniStep = new UnivariateProposalStep(0.25, 100,
	 * burnIn); DEMCProposalStep demcStep = new DEMCProposalStep(0.25, 100,
	 * burnIn, 10, 100, 0, varCount/4, varCount, false, false, true); SwapStep
	 * evenStep = new SwapStep(ChainParity.EVEN, 100 * chainCount); SwapStep
	 * oddStep = new SwapStep(ChainParity.ODD, 100 * chainCount);
	 * 
	 * mcmc.addStep(uniStep); for(int i = 0; i < chainCount; i++) {
	 * mcmc.addStep(evenStep); mcmc.addStep(oddStep); } mcmc.addStep(demcStep);
	 * 
	 * // Run, collect statistics, and check moments against expected
	 * distribution double[] sum = new double[varCount]; double[] sumSq = new
	 * double[varCount]; for(long i = 0; i < iterCount; i++) { mcmc.runFor(1);
	 * 
	 * assertEquals(i + 1, mcmc.getIterationCount());
	 * 
	 * if(i >= burnIn) { for(int j = 0; j < varCount; j++) { double val =
	 * mcmc.getModel().getDoubleVariable("v" + j).getValue(); sum[j] += val;
	 * sumSq[j] += val * val; } } }
	 * 
	 * double N = iterCount - burnIn;
	 * 
	 * double[] mean = new double[varCount]; double[] sd = new double[varCount];
	 * for(int i = 0; i < varCount; i++) { mean[i] = sum[i] / N;
	 * System.err.printf("mean %d = %f\n", i, mean[i]);
	 * 
	 * sd[i] = sqrt(N / (N - 1) * (sumSq[i]/N - mean[i] * mean[i]));
	 * System.err.printf("sd %d = %f\n", i, sd[i]); }
	 * 
	 * for(int i = 0; i < varCount; i++) { assertEquals(0.0, mean[i], 0.02);
	 * assertEquals(1.0, sd[i], 0.02); } }
	 */
}
