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

package mc3kit.types.binary.distributions;

import mc3kit.*;
import mc3kit.model.Model;
import mc3kit.model.ModelEdge;
import mc3kit.model.ModelNode;
import mc3kit.model.Variable;
import mc3kit.step.univariate.VariableProposer;
import mc3kit.types.binary.BinaryDistribution;
import mc3kit.types.binary.BinaryVariable;
import mc3kit.types.binary.proposers.GibbsBinaryProposer;
import mc3kit.types.doublevalue.DoubleValued;
import static java.lang.Math.*;

public class BernoulliDistribution extends BinaryDistribution {
	double pVal;
	ModelEdge pEdge;
	
	protected BernoulliDistribution() {
	}
	
	public BernoulliDistribution(Model model) {
		this(model, null, 0.5);
	}
	
	public BernoulliDistribution(Model model, String name) {
		this(model, name, 0.5);
	}
	
	public BernoulliDistribution(Model model, DoubleValued pNode)
			throws MC3KitException {
		this(model, null, 0.5);
		setP(pNode);
	}
	
	public BernoulliDistribution(Model model, double pVal) {
		this(model, null, pVal);
	}
	
	public BernoulliDistribution(Model model, String name, double pVal) {
		super(model, name);
		this.pVal = pVal;
	}
	
	public <T extends ModelNode & DoubleValued> BernoulliDistribution setP(
			T node) throws MC3KitException {
		pEdge = updateEdge(pEdge, node);
		return this;
	}
	
	public void setP(DoubleValued pNode) throws MC3KitException {
		pEdge = updateEdge(pEdge, (ModelNode) pNode);
	}
	
	@Override
	public double getLogP(Variable var) {
		boolean value = ((BinaryVariable) var).getValue();
		double p = pEdge == null ? this.pVal : getDoubleValue(pEdge);
		
		return value ? log(p) : log1p(-p);
	}
	
	@Override
	public VariableProposer makeVariableProposer(String varName) {
		return new GibbsBinaryProposer(varName);
	}
	
	@Override
	public void sample(Variable var) {
		double p = pEdge == null ? this.pVal : getDoubleValue(pEdge);
		((BinaryVariable) var).setValue(getRng().nextDouble() < p);
	}
}
