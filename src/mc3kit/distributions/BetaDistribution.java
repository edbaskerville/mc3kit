package mc3kit.distributions;

import mc3kit.*;
import mc3kit.proposal.MHUniformProposer;
import cern.jet.random.Beta;
import static mc3kit.util.Math.*;
import static java.lang.Math.*;

public class BetaDistribution extends DoubleDistribution {
  ModelEdge alphaEdge;
  double alpha;
  
  ModelEdge betaEdge;
  double beta;
  
  double logBetaAB;
  
  public BetaDistribution() {
    this(null);
  }

  public BetaDistribution(String name) {
    this(name, 1.0, 1.0);
  }
  
  public BetaDistribution(double alpha, double beta) throws IllegalArgumentException {
    this(null, alpha, beta);
  }
  
  public BetaDistribution(String name, double alpha, double beta) throws IllegalArgumentException {
    super(name);
    
    if(Double.isNaN(alpha) || Double.isNaN(beta)) {
      throw new IllegalArgumentException("alpha or beta is NaN");
    }
    
    if(Double.isInfinite(alpha) || Double.isInfinite(beta)) {
      throw new IllegalArgumentException("alpha or beta is infinite");
    }
    
    if(alpha <= 0.0 || beta <= 0.0) {
      throw new IllegalArgumentException("alpha and beta must be larger than zero");
    }
    
    this.alpha = alpha;
    this.beta = beta;
    updateConstant();
  }
  
  private void updateConstant() {
    logBetaAB = logBeta(alpha, beta);
  }

  @Override
  public VariableProposer makeVariableProposer(String varName) {
    return new MHUniformProposer(varName, 0.0, 1.0);
  }

  @Override
  public boolean valueIsValid(double value) {
    return value > 0.0 && value < 1.0;
  }

  @Override
  public double getLogP(Variable var) {
    double x = ((DoubleVariable)var).getValue();
    double logP = (alpha - 1.0) * log(x) + (beta - 1.0) * log1p(-x) - logBetaAB;
    return logP;
  }

  @Override
  public void sample(Variable var) {
    Beta betaGen = new Beta(alpha, beta, getRng());
    ((DoubleVariable)var).setValue(betaGen.nextDouble());
  }

  @Override
  public boolean update() {
    boolean changed = false;
    if(alphaEdge != null) {
      double oldAlpha = alpha;
      alpha = getDoubleValue(alphaEdge);
      if(alpha != oldAlpha) {
        changed = true;
      }
    }
    if(betaEdge != null) {
      double oldBeta = beta;
      beta = getDoubleValue(betaEdge);
      if(beta != oldBeta) {
        changed = true;
      }
    }
    if(changed) {
      updateConstant();
    }
    return changed;
  }
}
