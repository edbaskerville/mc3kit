package mc3kit.distributions;

import mc3kit.*;
import mc3kit.proposal.MHUniformProposer;
import static java.lang.Math.*;

public class UniformDistribution extends DoubleDistribution {
  double min;
  double max;
  double logP;
  
  public UniformDistribution() {
    this(null);
  }

  public UniformDistribution(String name) {
    this(name, 0.0, 1.0);
  }
  
  public UniformDistribution(double min, double max) throws IllegalArgumentException {
    this(null, min, max);
  }
  
  public UniformDistribution(String name, double min, double max) throws IllegalArgumentException {
    super(name);
    
    if(Double.isNaN(min) || Double.isNaN(max)) {
      throw new IllegalArgumentException("min or max is NaN");
    }
    
    if(min >= max) {
      throw new IllegalArgumentException("min must be less than max.");
    }
    
    if(Double.isInfinite(min) || Double.isInfinite(max)) {
      throw new IllegalArgumentException("min or max is infinite");
    }
    
    this.min = min;
    this.max = max;
    logP = getLogP(min, max);
  }

  @Override
  public VariableProposer makeVariableProposer(String varName) {
    return new MHUniformProposer(varName, min, max);
  }

  @Override
  public boolean valueIsValid(double value) {
    return value > min && value < max;
  }

  @Override
  public double getLogP(Variable var) {
    return logP;
  }

  @Override
  public void sample(Variable var) {
    ((DoubleVariable)var).setValue(min + (max - min) * getRng().nextDouble());
  }

  @Override
  public boolean update() {
    return false;
  }
  
  public static double getLogP(double min, double max) {
    return -log(max - min);
  }
}
