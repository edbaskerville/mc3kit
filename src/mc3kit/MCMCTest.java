package mc3kit;

import java.util.logging.Level;

import org.junit.*;

import static org.junit.Assert.*;

import mc3kit.distributions.*;
import mc3kit.functions.DoubleSumFunction;
import cern.jet.random.engine.*;
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
    
    RandomEngine rng = new MersenneTwister(100);
    
    MCMC mcmc = new MCMC();
    mcmc.setRng(rng);
    
    MCMC.setLogLevel(Level.INFO);
    
    Model m = new Model();
    
    m.beginConstruction();
    m.addDistribution(new NormalDistribution("nd"));
    m.addVariable(new DoubleVariable("nv"));
    m.setDistribution("nv", "nd");
    m.endConstruction(rng);
    
    mcmc.setModel(m);
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep();
    proposalStep.setTuneEvery(100);
    proposalStep.setTuneFor(burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      
      assertEquals(i + 1, mcmc.getIterationCount());
      
      if(i >= burnIn) {
        double val = mcmc.getModel().getDoubleVariable("nv").getValue();
        sum += val;
        sumSq += val * val;
      }
    }
    
    double N = iterCount - burnIn;
    
    double mean = sum / N;
    System.err.printf("mean = %f", mean);
    assertEquals(0.0, mean, 0.02);
    
    double sd = sqrt(N / (N - 1) * (sumSq/N - mean * mean));
    System.err.printf("sd = %f", sd);
    assertEquals(1.0, sd, 0.02);
  }

  @Test
  public void testStandardExponential() throws Throwable {
    long burnIn = 5000;
    long iterCount = 10000;
    
    MCMC mcmc = new MCMC();
    RandomEngine rng = new MersenneTwister(1453);
    mcmc.setRng(rng);
    
    Model m = new Model();
    
    m.beginConstruction();
    m.addDistribution(new ExponentialDistribution("ed"));
    m.addVariable(new DoubleVariable("ev"));
    m.setDistribution("ev", "ed");
    m.endConstruction(rng);
    
    mcmc.setModel(m);
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep();
    proposalStep.setTuneEvery(100);
    proposalStep.setTuneFor(burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      
      assertEquals(i + 1, mcmc.getIterationCount());
      
      if(i >= burnIn) {
        double val = mcmc.getModel().getDoubleVariable("ev").getValue();
        sum += val;
        sumSq += val * val;
      }
    }
    
    double N = iterCount - burnIn;
    
    double mean = sum / N;
    System.err.printf("mean = %f", mean);
    assertEquals(1.0, mean, 0.1);
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
    System.err.printf("var = %f", var);
    assertEquals(1.0, var, 0.1);
  }

  @Test
  public void testStandardUniform() throws Throwable {
    long burnIn = 5000;
    long iterCount = 10000;
    
    MCMC mcmc = new MCMC();
    RandomEngine rng = new MersenneTwister(1453);
    mcmc.setRng(rng);
    
    Model m = new Model();
    
    m.beginConstruction();
    m.addDistribution(new UniformDistribution("ed"));
    m.addVariable(new DoubleVariable("ev"));
    m.setDistribution("ev", "ed");
    m.endConstruction(rng);
    
    mcmc.setModel(m);
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep();
    proposalStep.setTuneEvery(100);
    proposalStep.setTuneFor(burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      
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
    System.err.printf("mean = %f", mean);
    assertEquals(0.5, mean, 0.1);
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
    System.err.printf("var = %f", var);
    assertEquals(1/12.0, var, 0.01);
  }

  @Test
  public void testBeta() throws Throwable {
    long burnIn = 5000;
    long iterCount = 10000;
    
    MCMC mcmc = new MCMC();
    RandomEngine rng = new MersenneTwister(1453);
    mcmc.setRng(rng);
    
    Model m = new Model();
    
    m.beginConstruction();
    m.addDistribution(new BetaDistribution("d", 2.0, 3.0));
    m.addVariable(new DoubleVariable("v"));
    m.setDistribution("v", "d");
    m.endConstruction(rng);
    
    mcmc.setModel(m);
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep();
    proposalStep.setTuneEvery(100);
    proposalStep.setTuneFor(burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      
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
    System.err.printf("mean = %f", mean);
    assertEquals(0.4, mean, 0.01);
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
    System.err.printf("var = %f", var);
    assertEquals(5.0/(5*5*6.0), var, 0.01);
  }

  @Test
  public void testSumNormals() throws Throwable {
    long burnIn = 5000;
    long iterCount = 10000;
    
    RandomEngine rng = new MersenneTwister(101);
    
    MCMC mcmc = new MCMC();
    mcmc.setRng(rng);
    
    Model m = new Model();
    
    m.beginConstruction();
    DoubleDistribution d = m.addDistribution(new NormalDistribution());
    DoubleVariable v1 = m.addVariable(new DoubleVariable("v1"))
      .setDistribution(d);
    DoubleVariable v2 = m.addVariable(new DoubleVariable("v2"))
      .setDistribution(d);
    m.addFunction(new DoubleSumFunction("v12"))
      .add(v1).add(v2);
    m.endConstruction(rng);
    
    mcmc.setModel(m);
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep();
    proposalStep.setTuneEvery(100);
    proposalStep.setTuneFor(burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      
      assertEquals(i + 1, mcmc.getIterationCount());
      
      if(i >= burnIn) {
        double val = mcmc.getModel().getDoubleFunction("v12").getValue();
        sum += val;
        sumSq += val * val;
      }
    }
    
    double N = iterCount - burnIn;
    
    double mean = sum / N;
    System.err.printf("mean = %f", mean);
    assertEquals(0.0, mean, 0.15);
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
    System.err.printf("var = %f", var);
    assertEquals(2.0, var, 0.4);
  }
}
