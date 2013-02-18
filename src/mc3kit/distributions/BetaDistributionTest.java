package mc3kit.distributions;

import static org.junit.Assert.*;

import mc3kit.*;
import org.junit.Test;

public class BetaDistributionTest {
  
  @Test
  public void alphaZero() {
    try {
      new BetaDistribution(null, 0.0, 1.0);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void betaZero() {
    try {
      new BetaDistribution(null, 1.0, 0.0);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void alphaNaN() {
    try {
      new BetaDistribution(null, Double.NaN, 1.0);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void betaNaN() {
    try {
      new BetaDistribution(null, 1.0, Double.NaN);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void alphaInf() {
    try {
      new BetaDistribution(null, Double.NEGATIVE_INFINITY, 0.0);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void betaInf() {
    try {
      new BetaDistribution(null, 0.0, Double.POSITIVE_INFINITY);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void standard() {
    BetaDistribution d = new BetaDistribution(null);
    DoubleVariable var = new DoubleVariable(null, 0.5);
    double logP = d.getLogP(var);
    assertEquals(0.0, logP, 1e-12);
  }
  
  @Test
  public void standardOutOfRange() {
    BetaDistribution d = new BetaDistribution(null);
    assertFalse(d.valueIsValid(0.0));
    assertFalse(d.valueIsValid(1.0));
    assertTrue(d.valueIsValid(1e-12));
    assertTrue(d.valueIsValid(1.0 - 1e-12));
  }
  
  @Test
  public void severalRandom() {
    double[] alphas = { 1.776662818162918, 0.747474253139548, 2.695768631910706 };
    double[] betas = { 0.657581642270088, 2.504673201506758, 0.849449489778260 };
    double[] xs = { 0.274468221468851, 0.966567777097225, 0.771841087378561 };
    double[] logPs = { -0.892500640169931, -4.660665612263455, 0.495979055011964 };
    
    for(int i = 0; i < alphas.length; i++) {
      BetaDistribution d = new BetaDistribution(null, alphas[i], betas[i]);
      DoubleVariable var = new DoubleVariable(null, xs[i]);
      double logP = d.getLogP(var);
      assertEquals(logPs[i], logP, 1e-12);
    }
  }
}
