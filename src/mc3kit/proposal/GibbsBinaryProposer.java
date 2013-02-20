package mc3kit.proposal;

import static java.lang.String.format;
import cern.jet.random.engine.RandomEngine;
import mc3kit.*;
import static java.lang.Math.*;

public class GibbsBinaryProposer extends VariableProposer {

  public GibbsBinaryProposer(String name) {
    super(name);
  }

  @Override
  public void step(Model model) throws MC3KitException {
    Chain chain = model.getChain();
    RandomEngine rng = chain.getRng();
    
    chain.getLogger().finest("GibbsBinaryProposer stepping");

    double oldLogPrior = model.getLogPrior();
    double oldLogLike = model.getLogLikelihood();
    
    chain.getLogger().finest(format("oldLP, oldLL: %f, %f", oldLogPrior, oldLogLike));
    
    BinaryVariable v = model.getBinaryVariable(getName());
    boolean oldValue = v.getValue();
    
    // Propose 1 for 0, 0 for 1
    model.beginProposal();
    v.setValue(!oldValue);
    model.endProposal();

    double newLogPrior = model.getLogPrior();
    double newLogLike = model.getLogLikelihood();
    
    chain.getLogger().finest(format("newLP, newLL: %f, %f", newLogPrior, newLogLike));
    
    // For this to be a Gibbs step, the ratio of the probability of acceptance (change the value)
    // to rejection (keep the value what it was) needs to be the ratio of the conditional densities
    // of those two states:
    // p / (1 - p) = f(!oldValue) / f(oldValue) = r
    // i.e.
    // p = r / (1 + r)
    // where f = prior^priorHeatExponent x likelihood^heatExponent
    double priorHeatExp = chain.getPriorHeatExponent();
    double likeHeatExp = chain.getLikelihoodHeatExponent();
    double logR = priorHeatExp * (newLogPrior - oldLogPrior) + likeHeatExp * (newLogLike - oldLogLike);
    double logP = logR - log1p(exp(logR));
    
    boolean accepted = log(rng.nextDouble()) < logP;
    if(accepted) {
      model.acceptProposal();
      recordAcceptance();
    }
    else {
      model.beginRejection();
      v.setValue(oldValue);
      model.endRejection();
      recordRejection();
    }
  }

}
