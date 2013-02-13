package mc3kit.proposal;

import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import mc3kit.*;
import static mc3kit.util.Math.*;

public class MHNormalProposer extends VariableProposer<DoubleVariable> {

  double proposalSD;
  
  public MHNormalProposer(String name) {
    super(name);
    proposalSD = 1.0;
  } 

  @Override
  public void step(Model model, double priorHeatExp, double likeHeatExp,
      RandomEngine rng) throws MC3KitException {
    
    System.err.println("MHNormalProposer stepping");
    
    Normal normal = new Normal(0.0, proposalSD, rng);
    
    double oldLogLikelihood = model.getLogLikelihood();
    double oldLogPrior = model.getLogPrior();
    
    System.err.printf("oldLP, oldLL: %f, %f\n", oldLogPrior, oldLogLikelihood);
    
    DoubleVariable rv = model.getDoubleVariable(getName());
    
    double oldValue = rv.getValue();
    double newValue = oldValue + normal.nextDouble();
    
    System.err.printf("oldVal, newVal = %f, %f\n", oldValue, newValue);
    
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
    
    System.err.printf("newLP, newLL: %f, %f\n", newLogPrior, newLogLikelihood);
    
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
