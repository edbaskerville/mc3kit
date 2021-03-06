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

package mc3kit.model;

import mc3kit.MC3KitException;
import mc3kit.step.univariate.VariableProposer;

public abstract class Distribution extends ModelNode {
	
	protected Distribution() {
	}
	
	public Distribution(Model model) {
		this(model, null);
	}
	
	public Distribution(Model model, String name) {
		super(name);
		
		if(model != null) {
			model.addDistribution(this);
		}
	}
	
	public abstract double getLogP(Variable var) throws MC3KitException;
	
	public abstract VariableProposer makeVariableProposer(String varName)
			throws MC3KitException;
	
	public abstract void sample(Variable var) throws MC3KitException;
}
