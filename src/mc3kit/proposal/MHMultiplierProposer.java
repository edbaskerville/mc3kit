package mc3kit.proposal;

import cern.jet.random.engine.RandomEngine;
import mc3kit.*;
import static java.lang.Math.exp;
import static mc3kit.util.Math.*;
import static java.lang.String.format;

public class MHMultiplierProposer extends VariableProposer<DoubleVariable> {

  double lambda;
  
  public MHMultiplierProposer(String name) {
    super(name);
    this.lambda = 0.5;
  } 

  @Override
  public void step(Chain chain, Model model, double priorHeatExp, double likeHeatExp,
      RandomEngine rng) throws MC3KitException {
    
    chain.getLogger().finest("MHMultiplierProposer stepping");
    
    double oldLogLikelihood = model.getLogLikelihood();
    double oldLogPrior = model.getLogPrior();
    
    chain.getLogger().finest(format("oldLP, oldLL: %f, %f", oldLogPrior, oldLogLikelihood));
    
    DoubleVariable rv = model.getDoubleVariable(getName());
    
    double oldValue = rv.getValue();
    double logMultiplier = lambda * (rng.nextDouble() - 0.5);
    double multiplier = exp(logMultiplier);
    
    double newValue = multiplier * oldValue;
    
    chain.getLogger().finest(format("oldVal, newVal = %f, %f", oldValue, newValue));
    
    if(!rv.valueIsValid(newValue))
    {
      recordRejection();
      return;
    }
    
    model.beginProposal();
    rv.setValue(newValue);
    model.endProposal();

    double newLogPrior = model.getLogPrior();
    double newLogLikelihood = model.getLogLikelihood();
    
    chain.getLogger().finest(format("newLP, newLL: %f, %f", newLogPrior, newLogLikelihood));
    
    boolean accepted = shouldAcceptMetropolisHastings(rng, priorHeatExp, likeHeatExp,
      oldLogPrior, oldLogLikelihood,
      newLogPrior, newLogLikelihood,
      logMultiplier
    );
    
    if(accepted)
    {
      model.acceptProposal();
      recordAcceptance();
    }
    else
    {
      model.beginRejection();
      rv.setValue(oldValue);
      model.endRejection();
      
      recordRejection();
    }
  }

  @Override
  public void tune(double targetRate) throws MC3KitException {
    lambda = adjustTuningParameter(lambda, getAcceptanceRate(), targetRate);
  }
}
