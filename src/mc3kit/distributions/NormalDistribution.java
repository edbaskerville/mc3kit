package mc3kit.distributions;

import cern.jet.random.Normal;
import mc3kit.*;

import static java.lang.Math.*;
import static mc3kit.util.Math.*;
import mc3kit.DoubleDistribution;
import mc3kit.DoubleVariable;
import mc3kit.ModelException;
import mc3kit.VariableProposer;

import mc3kit.proposal.*;

public class NormalDistribution extends DoubleDistribution {

  double mean;
  double stdDev;
  
  ModelEdge meanEdge;
  ModelEdge stdDevEdge;
  ModelEdge varEdge;
  ModelEdge precEdge;
  
  public NormalDistribution(Model model) {
    this(model, null);
  }
  
  public NormalDistribution(Model model, String name) {
    this(model, name, 0.0, 1.0);
  }

  public NormalDistribution(Model model, double mean, double stdDev) {
    this(model, null, mean, stdDev);
  }
  
  public NormalDistribution(Model model, String name, double mean, double stdDev) {
    super(model, name);
    this.mean = mean;
    this.stdDev = stdDev;
  }
  
  @Override
  public VariableProposer makeVariableProposer(String varName) {
    return new MHNormalProposer(varName);
  }
  
  public <T extends ModelNode & DoubleValued> NormalDistribution setMean(T meanNode) throws ModelException {
    meanEdge = updateEdge(meanEdge, meanNode);
    return this;
  }
  
  public <T extends ModelNode & DoubleValued> NormalDistribution setVariance(T varNode) throws ModelException {
    precEdge = updateEdge(precEdge, null);
    stdDevEdge = updateEdge(stdDevEdge, null);
    varEdge = updateEdge(varEdge, varNode);
    return this;
  }
  
  public <T extends ModelNode & DoubleValued> NormalDistribution setStdDev(T stdDevNode) throws ModelException {
    precEdge = updateEdge(precEdge, null);
    varEdge = updateEdge(varEdge, null);
    stdDevEdge = updateEdge(stdDevEdge, stdDevNode);
    return this;
  }
  
  public <T extends ModelNode & DoubleValued> NormalDistribution setPrecision(T precNode) throws ModelException {
    varEdge = updateEdge(varEdge, null);
    stdDevEdge = updateEdge(stdDevEdge, null);
    precEdge = updateEdge(precEdge, precNode);
    return this;
  }

  @Override
  public double getLogP(Variable v) {
    double x = ((DoubleVariable)v).getValue();
    double mean = meanEdge == null ? this.mean : getDoubleValue(meanEdge);
    
    if(precEdge != null) {
      assert varEdge == null;
      assert stdDevEdge == null;
      return getLogPPrecision(mean, getDoubleValue(precEdge), x);
    }
    else if(varEdge != null) {
      assert precEdge == null;
      assert stdDevEdge == null;
      return getLogPVar(mean, getDoubleValue(varEdge), x);
    }
    else  {
      assert precEdge == null;
      assert varEdge == null;
      return getLogPStdDev(mean, stdDevEdge == null ? this.stdDev : getDoubleValue(stdDevEdge), x);
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
  public void sample(Variable var) {
    double mean = this.mean;
    if(meanEdge != null) {
      mean = getDoubleValue(meanEdge);
    }
    
    double sd = this.stdDev;
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
    ((DoubleVariable)var).setValue(new Normal(mean, sd, getRng()).nextDouble());
  }

  @Override
  public boolean valueIsValid(double val) {
    if(Double.isInfinite(val) || Double.isNaN(val)) {
      return false;
    }
    return true;
  }
}
