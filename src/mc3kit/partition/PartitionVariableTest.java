package mc3kit.partition;

import static org.junit.Assert.*;

import mc3kit.Chain;
import mc3kit.DoubleDistribution;
import mc3kit.DoubleVariable;
import mc3kit.MC3KitException;
import mc3kit.MCMC;
import mc3kit.Model;
import mc3kit.ModelFactory;
import mc3kit.UnivariateProposalStep;
import mc3kit.distributions.NormalDistribution;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PartitionVariableTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void normalMixture() throws Throwable {
    long burnIn = 5000;
    long iterCount = 40000;
    
    MCMC mcmc = new MCMC();
    mcmc.setRandomSeed(100L);
    
    mcmc.setModelFactory(new ModelFactory() {
      
      @Override
      public Model createModel(Chain initialChain) throws MC3KitException {
        Model m = new Model(initialChain);
        
        m.beginConstruction();
        
        DoubleDistribution[] d = new DoubleDistribution[2];
        d[0] = new NormalDistribution(-1.0, 1.0);
        m.addDistribution(d[0]);
        d[1] = new NormalDistribution(3.0, 1.0);
        m.addDistribution(d[1]);
        
        DoubleVariable[] v = new DoubleVariable[50];
        for(int i = 0; i < 50; i++) {
          v[i] = new DoubleVariable("v" + i);
          m.addVariable(v[i]);
        }
        
        PartitionVariable pv = new PartitionVariable("part", v.length, d.length);
        m.addVariable(pv);
        pv.associateVariablesWithDistributions(v, d);
        pv.sample();
        
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
      System.err.println("stepping " + i);
      mcmc.step();
      mcmc.getModel().recalculate();
      
      assertEquals(i + 1, mcmc.getIterationCount());
      
      if(i >= burnIn) {
        for(int j = 0; j < 50; j++) {
          double val = mcmc.getModel().getDoubleVariable("v" + j).getValue();
          sum += val;
          sumSq += val * val;
        }
      }
    }
    
    double N = 50 * (iterCount - burnIn);
    
    double mean = sum / N;
    System.err.printf("mean = %f\n", mean);
    assertEquals(1.0, mean, 0.02);
    
    double var = N / (N - 1) * (sumSq/N - mean * mean);
    System.err.printf("var = %f\n", var);
    assertEquals(5.0, var, 0.02);
  }
}
