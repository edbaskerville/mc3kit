package mc3kit.proposal;

import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import mc3kit.*;

public class MHNormalProposer extends VariableProposer<DoubleVariable> {

  double proposalSD;
  
  public MHNormalProposer(String name) {
    super(name);
  } 

  @Override
  public void step(Model model, double priorHeatExp, double likeHeatExp,
      RandomEngine rng) throws MC3KitException {
    
    /*Normal normal = new Normal(0.0, proposalSD, rng);
    
    double oldLogLikelihood = model.getLogLikelihood();
    double oldLogPrior = model.getLogPrior();
    
    double proposalSDAdjusted = proposalSD;
    
    double oldValue = rv.getValue();
    double newValue = oldValue + normal.nextDouble(0.0, proposalSDAdjusted);
    
    if(!rv.valueIsValid(newValue))
    {
      recordRejection();
      return;
    }
    
    model.beginUpdate(false);
    rv.setValue(newValue);
    model.endUpdate();
    
    double newLogLikelihood = model.getLogLikelihood();
    double newLogPrior = model.getLogPrior();
    
    boolean accepted = shouldAcceptMetropolisHastings(rng, priorHeatExp, likeHeatExp,
      oldLogPrior, oldLogLikelihood,
      newLogPrior, newLogLikelihood,
      0.0
    );
    
    if(accepted)
    {
      recordAcceptance();
    }
    else
    {
      model.beginUpdate(true);
      rv.setValue(oldValue);
      model.endUpdate();
      
      recordRejection();
    }*/
  }
}
