package mc3kit.distributions;

import static org.junit.Assert.*;

import java.util.logging.Level;

import mc3kit.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BernoulliDistributionTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() throws Throwable {
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
        new BinaryVariable(m, "v", new BernoulliDistribution(m, 0.3));
        m.endConstruction();
        
        return m;
      }
    };
    
    mcmc.setModelFactory(mf);
    
    UnivariateProposalStep proposalStep = new UnivariateProposalStep(0.25, 100, burnIn);
    mcmc.addStep(proposalStep);
    
    // Run, collect statistics, and check moments against expected distribution
    int sum = 0;
    for(long i = 0; i < iterCount; i++) {
      mcmc.step();
      mcmc.getModel().recalculate();
      
      assertEquals(i + 1, mcmc.getIterationCount());
      
      if(i >= burnIn) {
        boolean val = mcmc.getModel().getBinaryVariable("v").getValue();
        sum += val ? 1 : 0;
      }
    }
    
    double N = iterCount - burnIn;
    
    double mean = sum / (double)N;
    System.err.printf("mean = %f\n", mean);
    assertEquals(0.3, mean, 0.02);
  }
}
