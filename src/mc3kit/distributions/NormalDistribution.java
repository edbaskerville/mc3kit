package mc3kit.distributions;

import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import mc3kit.*;

import static java.lang.Math.*;
import static mc3kit.util.Math.*;
import mc3kit.DoubleDistribution;
import mc3kit.DoubleVariable;
import mc3kit.ModelEdgeException;
import mc3kit.VariableProposer;

import mc3kit.proposal.*;

public class NormalDistribution extends DoubleDistribution {
  
  ModelEdge meanEdge;
  ModelEdge precEdge;
  ModelEdge varEdge;
  ModelEdge stdDevEdge;
  
  public NormalDistribution() {
    this(null);
  }
  
  public NormalDistribution(String name) {
    super(name);
  }

  @Override
  public VariableProposer<DoubleVariable> makeVariableProposer(String varName) {
    return new MHNormalProposer(varName);
  }
  
  public <T extends ModelNode & DoubleValued> void setMean(T meanNode) throws ModelEdgeException {
    meanEdge = updateEdge(meanEdge, meanNode);
  }
  
  public <T extends ModelNode & DoubleValued> void setVariance(T varNode) throws ModelEdgeException {
    precEdge = updateEdge(precEdge, null);
    stdDevEdge = updateEdge(stdDevEdge, null);
    varEdge = updateEdge(varEdge, varNode);
  }
  
  public <T extends ModelNode & DoubleValued> void setStdDev(T stdDevNode) throws ModelEdgeException {
    precEdge = updateEdge(precEdge, null);
    varEdge = updateEdge(varEdge, null);
    stdDevEdge = updateEdge(stdDevEdge, stdDevNode);
  }
  
  public <T extends ModelNode & DoubleValued> void setPrecision(T precNode) throws ModelEdgeException {
    varEdge = updateEdge(varEdge, null);
    stdDevEdge = updateEdge(stdDevEdge, null);
    precEdge = updateEdge(precEdge, precNode);
  }

  @Override
  public double getLogP(DoubleVariable v) {
    double x = v.getValue();
    double mean = meanEdge == null ? 0.0 : getDoubleValue(meanEdge);
    
    if(precEdge != null) {
      assert varEdge == null;
      assert stdDevEdge == null;
      return getLogPPrecision(mean, getDoubleValue(precEdge), x);
    }
    else if(stdDevEdge != null) {
      assert precEdge == null;
      assert varEdge == null;
      return getLogPStdDev(mean, getDoubleValue(stdDevEdge), x);
    }
    else {
      assert precEdge == null;
      assert stdDevEdge == null;
      return getLogPVar(mean, varEdge == null ? 1.0 : getDoubleValue(varEdge), x);
    }
  }
  
  public static double getLogPPrecision(double mean, double prec, double x) {
    assert prec > 0.0;
    double d = x - mean;
    return 0.5 * (log(prec) - LOG_TWO_PI) - d * d * prec / 2.0;
  }
  
  public static double getLogPStdDev(double mean, double stdDev, double x) {
    assert stdDev > 0.0;
    double d = x - mean;
    return -(log(stdDev) + 0.5 * LOG_TWO_PI) - d * d / (2.0 * stdDev * stdDev);
  }
  
  public static double getLogPVar(double mean, double var, double x) {
    assert var > 0.0;
    double d = x - mean;
    return -0.5 * (log(var) + LOG_TWO_PI) - d * d / (2.0 * var);
  }

  @Override
  public void sample(DoubleVariable var, RandomEngine rng) {
    double mean = 0.0;
    if(meanEdge != null) {
      mean = getDoubleValue(meanEdge);
    }
    
    double sd = 1.0;
    if(stdDevEdge != null) {
      assert precEdge == null;
      assert varEdge == null;
      sd = getDoubleValue(stdDevEdge);
    }
    else if(varEdge != null) {
      assert precEdge == null;
      assert stdDevEdge == null;
      sd = sqrt(getDoubleValue(varEdge));
    }
    else if(precEdge != null) {
      assert stdDevEdge == null;
      assert varEdge == null;
      sd = sqrt(1.0/getDoubleValue(precEdge));
    }
    var.setValue(new Normal(mean, sd, rng).nextDouble());
  }

  @Override
  public boolean valueIsValid(double val) {
    if(Double.isInfinite(val) || Double.isNaN(val)) {
      return false;
    }
    return true;
  }
}
