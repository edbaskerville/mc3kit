package mc3kit.proposal;

import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import mc3kit.*;
import static mc3kit.util.Math.*;

public class MHNormalProposer extends VariableProposer<DoubleVariable> {

  double proposalSD;
  
  public MHNormalProposer(String name) {
    super(name);
  } 

  @Override
  public void step(Model model, double priorHeatExp, double likeHeatExp,
      RandomEngine rng) throws MC3KitException {
    
    Normal normal = new Normal(0.0, proposalSD, rng);
    
    double oldLogLikelihood = model.getLogLikelihood();
    double oldLogPrior = model.getLogPrior();
    
    DoubleVariable rv = model.getDoubleVariable(getName());
    
    double oldValue = rv.getValue();
    double newValue = oldValue + normal.nextDouble();
    
    if(!rv.valueIsValid(newValue))
    {
      recordRejection();
      return;
    }
    
    model.beginProposal();
    rv.setValue(newValue);
    model.endProposal();
    
    double newLogLikelihood = model.getLogLikelihood();
    double newLogPrior = model.getLogPrior();
    
    boolean accepted = shouldAcceptMetropolisHastings(rng, priorHeatExp, likeHeatExp,
      oldLogPrior, oldLogLikelihood,
      newLogPrior, newLogLikelihood,
      0.0
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
}
