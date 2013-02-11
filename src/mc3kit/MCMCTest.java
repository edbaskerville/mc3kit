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
    
    MCMC mcmc = new MCMC();
    
    Model m = new Model();
    m.addDistribution(new NormalDistribution("nd"));
    m.addVariable(new DoubleVariable("nv"));
    m.setDistribution("nv", "nd");
    mcmc.setModel(m);
    
    RandomEngine rng = new MersenneTwister();
    mcmc.setRng(rng);
    
    UnivariateProposalTask proposalTask = new UnivariateProposalTask();
    proposalTask.setTuneEvery(100);
    proposalTask.setTuneFor(burnIn);
    mcmc.addTask(proposalTask);
    
    // Run and collect statistics
    double sum = 0;
    double sumSq = 0;
    for(long i = 0; i < 10000; i++) {
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
    
    assertEquals(0.0, mean, 0.01);
    
    double sd = sqrt(N / (N - 1) * (sumSq/N - mean * mean));
    assertEquals(1.0, sd, 0.01);
  }
}
