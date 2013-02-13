package mc3kit.distributions;

import static java.lang.Math.log;
import mc3kit.*;
import mc3kit.proposal.*;

public class ExponentialDistribution extends DoubleDistribution {
  
  ModelEdge rateEdge;
  ModelEdge scaleEdge;
  
  
  public ExponentialDistribution() {
    this(null);
  }
  
  public ExponentialDistribution(String name) {
    super(name);
  }

  @Override
  public VariableProposer<DoubleVariable> makeVariableProposer(String varName) {
    return new MHMultiplierProposal(varName);
  }
  
  public <T extends ModelNode & DoubleValued> void setRateNode(T rateNode) throws ModelEdgeException {
    scaleEdge = updateEdge(scaleEdge, null);
    rateEdge = updateEdge(rateEdge, rateNode);
  }
  
  public <T extends ModelNode & DoubleValued> void setScaleNode(T scaleNode) throws ModelEdgeException {
    rateEdge = updateEdge(rateEdge, null);
    scaleEdge = updateEdge(scaleEdge, scaleNode);
  }

  @Override
  public double getLogP(DoubleVariable v) {
    double x = v.getValue();
    
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
}
