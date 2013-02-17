package mc3kit.proposal;

import static java.lang.Math.*;
import static mc3kit.util.Math.shouldAcceptMetropolisHastings;
import static java.lang.String.format;
import mc3kit.*;
import cern.jet.random.engine.RandomEngine;

public class MHUniformProposer extends VariableProposer<DoubleVariable> {

  double min;
  double max;
  double proposalRadius;
  
  public MHUniformProposer(String name, double min, double max) {
    super(name);
    this.min = min;
    this.max = max;
    proposalRadius = (max - min) * 0.25;
  } 

  @Override
  public void step(Chain chain, Model model, double priorHeatExp, double likeHeatExp,
      RandomEngine rng) throws MC3KitException {
    chain.getLogger().finest("MHUniformProposer stepping");
    
    double oldLogLikelihood = model.getLogLikelihood();
    double oldLogPrior = model.getLogPrior();
    
    chain.getLogger().finest(format("oldLP, oldLL: %f, %f", oldLogPrior, oldLogLikelihood));
    
    DoubleVariable rv = model.getDoubleVariable(getName());
    
    double oldValue = rv.getValue();
    
    double xMinF = max(min, oldValue - proposalRadius);
    double xMaxF = min(max, oldValue + proposalRadius);
    double logForwardProposal = -log(xMaxF - xMinF);
    
    double newValue = xMinF + (xMaxF - xMinF) * rng.nextDouble();
    
    double xMinB = max(min, newValue - proposalRadius);
    double xMaxB = min(max, newValue + proposalRadius);
    double logBackwardProposal = -log(xMaxB - xMinB);
    
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
      logBackwardProposal - logForwardProposal
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
    proposalRadius = min(
      adjustTuningParameter(proposalRadius, getAcceptanceRate(), targetRate),
      max - min
    );
  }
}
