package mc3kit;

import java.util.logging.Level;

import org.junit.*;

import static org.junit.Assert.*;

import mc3kit.distributions.*;
import mc3kit.functions.DoubleSumFunction;
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
    
    MCMC.setLogLevel(Level.INFO);
    
    ModelFactory mf = new ModelFactory() {
      @Override
      public Model createModel(Chain initialChain) throws MC3KitException {
        Model m = new Model(initialChain);
        
        m.beginConstruction();
        m.addDistribution(new NormalDistribution("nd"));
        m.addVariable(new DoubleVariable("nv"));
        m.setDistribution("nv", "nd");
        m.endConstruction();
        
        return m;
      }
    };
    
    mcmc.setModelFactory(mf);
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep();
    proposalStep.setTuneEvery(100);
    proposalStep.setTuneFor(burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      mcmc.getModel().recalculate();
      
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
    long burnIn = 10000;
    long iterCount = 50000;
    
    MCMC mcmc = new MCMC();
    mcmc.setRandomSeed(1453L);
    
    mcmc.setModelFactory(new ModelFactory() {
      @Override
      public Model createModel(Chain initialChain) throws MC3KitException {
        Model m = new Model(initialChain);
        
        m.beginConstruction();
        m.addDistribution(new ExponentialDistribution("ed"));
        m.addVariable(new DoubleVariable("ev"));
        m.setDistribution("ev", "ed");
        m.endConstruction();
        
        return m;
      }
    });
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep();
    proposalStep.setTuneEvery(100);
    proposalStep.setTuneFor(burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      mcmc.getModel().recalculate();
      
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
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
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
        m.addDistribution(new UniformDistribution("ed"));
        m.addVariable(new DoubleVariable("ev"));
        m.setDistribution("ev", "ed");
        m.endConstruction();
        
        return m;
      }
    });
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep();
    proposalStep.setTuneEvery(100);
    proposalStep.setTuneFor(burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      mcmc.getModel().recalculate();
      
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
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
    System.err.printf("var = %f\n", var);
    assertEquals(1/12.0, var, 0.01);
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
        m.addDistribution(new BetaDistribution("d", 2.0, 3.0));
        m.addVariable(new DoubleVariable("v"));
        m.setDistribution("v", "d");
        m.endConstruction();
        
        return m;
      }
    });
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep();
    proposalStep.setTuneEvery(100);
    proposalStep.setTuneFor(burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      mcmc.getModel().recalculate();
      
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
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
    System.err.printf("var = %f\n", var);
    assertEquals(5.0/(5*5*6.0), var, 0.01);
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
        DoubleDistribution d = m.addDistribution(new NormalDistribution());
        DoubleVariable v1 = m.addVariable(new DoubleVariable("v1"))
          .setDistribution(d);
        DoubleVariable v2 = m.addVariable(new DoubleVariable("v2"))
          .setDistribution(d);
        m.addFunction(new DoubleSumFunction("v12"))
          .add(v1).add(v2);
        m.endConstruction();
        
        return m;
      }
    });
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep();
    proposalStep.setTuneEvery(100);
    proposalStep.setTuneFor(burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      mcmc.getModel().recalculate();
      
      assertEquals(i + 1, mcmc.getIterationCount());
      
      if(i >= burnIn) {
        double val = mcmc.getModel().getDoubleFunction("v12").getValue();
        sum += val;
        sumSq += val * val;
      }
    }
    
    double N = iterCount - burnIn;
    
    double mean = sum / N;
    System.err.printf("mean = %f\n", mean);
    assertEquals(0.0, mean, 0.02);
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
    System.err.printf("var = %f\n", var);
    assertEquals(2.0, var, 0.02);
  }
}
