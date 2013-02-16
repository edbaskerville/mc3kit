package mc3kit;

import org.junit.*;

import static org.junit.Assert.*;

import mc3kit.distributions.*;
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
    System.err.printf("mean = %f\n", mean);
    assertEquals(0.0, mean, 0.02);
    
    double sd = sqrt(N / (N - 1) * (sumSq/N - mean * mean));
    System.err.printf("sd = %f\n", sd);
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
    System.err.printf("mean = %f\n", mean);
    assertEquals(1.0, mean, 0.1);
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
    System.err.printf("var = %f\n", var);
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
    System.err.printf("mean = %f\n", mean);
    assertEquals(0.5, mean, 0.1);
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
    System.err.printf("var = %f\n", var);
    assertEquals(1/12.0, var, 0.01);
  }
}
