/***
  This file is part of mc3kit.
  
  Copyright (C) 2013 Edward B. Baskerville

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
***/

package mc3kit.types.doublevalue.proposers;

import com.google.gson.JsonObject;

import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import mc3kit.*;
import mc3kit.mcmc.Chain;
import mc3kit.model.Model;
import mc3kit.step.univariate.VariableProposer;
import mc3kit.types.doublevalue.DoubleVariable;
import static mc3kit.util.Math.*;

public class MHNormalProposer extends VariableProposer {

  double proposalSD;
  
  public MHNormalProposer(String name) {
    super(name);
    proposalSD = 1.0;
  }
  
  @Override
  public JsonObject toJsonObject() {
  	JsonObject jsonObj = super.toJsonObject();
  	jsonObj.addProperty("sd", proposalSD);
  	return jsonObj;
  }
  
  @Override
  public void fromJsonObject(JsonObject jsonObj) {
  	super.fromJsonObject(jsonObj);
  	proposalSD = jsonObj.getAsJsonPrimitive("sd").getAsDouble();
  }

  @Override
  public void step(Model model) throws MC3KitException {
    Chain chain = model.getChain();
    RandomEngine rng = chain.getRng();
    
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

    double newLogPrior = model.getLogPrior();
    double newLogLikelihood = model.getLogLikelihood();
    
    boolean accepted = shouldAcceptMetropolisHastings(rng,
       chain.getPriorHeatExponent(), chain.getLikelihoodHeatExponent(),
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

  @Override
  public void tune(double targetRate) throws MC3KitException {
    proposalSD = adjustTuningParameter(proposalSD, getAcceptanceRate(), targetRate);
  }
}
