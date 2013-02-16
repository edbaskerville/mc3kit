package mc3kit.distributions;

import static org.junit.Assert.*;

import mc3kit.DoubleVariable;
import mc3kit.ModelNodeException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UniformDistributionTest {

  @Test
  public void minMaxEqual() {
    try {
      UniformDistribution ud = new UniformDistribution("ud", 0.0, 0.0);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }

  @Test
  public void minLTMax() {
    try {
      UniformDistribution ud = new UniformDistribution("ud", 1.0, 0.0);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void minNaN() {
    try {
      UniformDistribution ud = new UniformDistribution("ud", Double.NaN, 0.0);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void maxNaN() {
    try {
      UniformDistribution ud = new UniformDistribution("ud", 0.0, Double.NaN);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void minInf() {
    try {
      UniformDistribution ud = new UniformDistribution("ud", Double.NEGATIVE_INFINITY, 0.0);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void maxInf() {
    try {
      UniformDistribution ud = new UniformDistribution("ud", 0.0, Double.POSITIVE_INFINITY);
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }
  
  @Test
  public void standard() {
    UniformDistribution ud = new UniformDistribution();
    DoubleVariable var = new DoubleVariable();
    var.setValue(0.5);
    double logP = ud.getLogP(var);
    assertEquals(0.0, logP, 1e-12);
  }
  
  @Test
  public void standardOutOfRange() {
    UniformDistribution ud = new UniformDistribution();
    assertFalse(ud.valueIsValid(0.0));
    assertFalse(ud.valueIsValid(1.0));
    assertTrue(ud.valueIsValid(1e-12));
    assertTrue(ud.valueIsValid(1.0 - 1e-12));
  }
  
  @Test
  public void severalRandom() {
    double[] mins = { 6.30683091934770, 9.26587566733360, -8.11127731576562 };
    double[] maxs = { 13.9267403981648, 14.6373395924456, -0.1198287634179 };
    double[] xs = { 8.33015870757078, 10.96825034973461, -1.20515368875235 };
    double[] logPs = { -2.03076449020767, -1.68110048312165, -2.07837203900915 };
    
    for(int i = 0; i < mins.length; i++) {
      UniformDistribution ud = new UniformDistribution(mins[i], maxs[i]);
      DoubleVariable var = new DoubleVariable();
      var.setValue(xs[i]);
      double logP = ud.getLogP(var);
      assertEquals(logPs[i], logP, 1e-12);
    }
  }
}
