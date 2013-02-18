package mc3kit.distributions;

import static cern.jet.stat.Gamma.logGamma;
import static java.lang.Math.log;
import cern.jet.random.Exponential;
import mc3kit.*;
import mc3kit.proposal.*;

public class GammaDistribution extends DoubleDistribution {
  
  ModelEdge shapeEdge;
  ModelEdge rateEdge;
  ModelEdge scaleEdge;
  
  
  public GammaDistribution(Model model) {
    this(model, null);
  }
  
  public GammaDistribution(Model model, String name) {
    super(model, name);
  }

  @Override
  public VariableProposer makeVariableProposer(String varName) {
    return new MHMultiplierProposer(varName);
  }
  
  public <T extends ModelNode & DoubleValued> GammaDistribution setShape(T shapeNode) throws ModelException {
    shapeEdge = updateEdge(shapeEdge, shapeNode);
    return this;
  }
  
  public <T extends ModelNode & DoubleValued> GammaDistribution setRate(T rateNode) throws ModelException {
    scaleEdge = updateEdge(scaleEdge, null);
    rateEdge = updateEdge(rateEdge, rateNode);
    return this;
  }
  
  public <T extends ModelNode & DoubleValued> GammaDistribution setScale(T scaleNode) throws ModelException {
    rateEdge = updateEdge(rateEdge, null);
    scaleEdge = updateEdge(scaleEdge, scaleNode);
    return this;
  }

  @Override
  public double getLogP(Variable v) {
    double x = ((DoubleVariable)v).getValue();
    
    double shape = 1.0;
    if(shapeEdge != null) {
      shape = getDoubleValue(shapeEdge);
    }
    
    if(rateEdge != null) {
      assert scaleEdge == null;
      return getLogPRate(x, shape, getDoubleValue(rateEdge));
    }
    else if(scaleEdge != null) {
      return getLogPScale(x, shape, getDoubleValue(scaleEdge));
    }
    return -x; // for rate == scale == null => rate = 1
  }
  
  public static double getLogPRate(double x, double shape, double rate) {
    assert x > 0;
    assert shape > 0;
    assert rate > 0;
    
    return shape * log(rate) - logGamma(shape) + (shape - 1.0) * log(x) - rate * x;
  }
  
  public static double getLogPScale(double x, double shape, double scale) {
    assert x > 0;
    assert shape > 0;
    assert scale > 0;
    
    return - shape * log(scale) - logGamma(shape) + (shape - 1.0) * log(x) - x / scale;
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
