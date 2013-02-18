package mc3kit.distributions;

import static java.lang.Math.log;
import cern.jet.random.Exponential;
import mc3kit.*;
import mc3kit.proposal.*;

public class ExponentialDistribution extends DoubleDistribution {
  
  ModelEdge rateEdge;
  ModelEdge scaleEdge;
  
  
  public ExponentialDistribution(Model model) {
    this(model, null);
  }
  
  public ExponentialDistribution(Model model, String name) {
    super(model, name);
  }

  @Override
  public VariableProposer makeVariableProposer(String varName) {
    return new MHMultiplierProposer(varName);
  }
  
  public <T extends ModelNode & DoubleValued> void setRate(T rateNode) throws ModelException {
    scaleEdge = updateEdge(scaleEdge, null);
    rateEdge = updateEdge(rateEdge, rateNode);
  }
  
  public <T extends ModelNode & DoubleValued> void setScale(T scaleNode) throws ModelException {
    rateEdge = updateEdge(rateEdge, null);
    scaleEdge = updateEdge(scaleEdge, scaleNode);
  }

  @Override
  public double getLogP(Variable v) {
    double x = ((DoubleVariable)v).getValue();
    
    if(rateEdge != null) {
      assert scaleEdge == null;
      return getLogPRate(x, getDoubleValue(rateEdge));
    }
    else if(scaleEdge != null) {
      return getLogPScale(x, getDoubleValue(scaleEdge));
    }
    return -x; // for rate == scale == null => rate = 1
  }
  
  public static double getLogPRate(double x, double rate) {
    assert x > 0;
    assert rate > 0;
    
    return log(rate) - rate * x;
  }
  
  public static double getLogPScale(double x, double scale) {
    assert x > 0;
    assert scale > 0;
    
    return -log(scale) - x / scale;
  }

  @Override
  public boolean valueIsValid(double val) {
    if(Double.isInfinite(val) || Double.isNaN(val) || val <= 0.0) {
      return false;
    }
    return true;
  }

  @Override
  public void sample(Variable var) {
    double rate;
    if(rateEdge != null) {
      rate = getDoubleValue(rateEdge);
    }
    else if(scaleEdge != null) {
      rate = 1.0 / getDoubleValue(scaleEdge);
    }
    else {
      rate = 1.0;
    }
    ((DoubleVariable)var).setValue(new Exponential(rate, getRng()).nextDouble());
  }
}
