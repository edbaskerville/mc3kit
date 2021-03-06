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

package mc3kit.types.doublevalue.functions;

import mc3kit.*;
import mc3kit.model.Model;
import mc3kit.model.ModelEdge;
import mc3kit.model.ModelNode;
import mc3kit.types.doublevalue.DoubleFunction;
import mc3kit.types.doublevalue.DoubleValued;

import java.util.*;

public class DoubleSum extends DoubleFunction {
	Map<DoubleValued, Summand> summandMap;
	
	protected DoubleSum() {
	}
	
	public DoubleSum(Model model) {
		this(model, null);
	}
	
	public DoubleSum(Model model, String name) {
		super(model, name);
		summandMap = new LinkedHashMap<DoubleValued, Summand>(2);
	}
	
	public DoubleSum add(DoubleValued summandNode) throws MC3KitException {
		return add(summandNode, 1.0);
	}
	
	public DoubleSum add(DoubleValued summandNode, double coeff)
			throws MC3KitException {
		if(summandMap.containsKey(summandNode)) {
			throw new IllegalArgumentException("Summand already present.");
		}
		
		ModelEdge edge = getModel().addEdge(this, (ModelNode) summandNode);
		Summand summand = new Summand(edge, coeff);
		summandMap.put(summandNode, summand);
		
		return this;
	}
	
	@Override
	public boolean update() {
		double oldVal = getValue();
		double newVal = 0.0;
		for(Summand summand : summandMap.values()) {
			newVal += summand.coeff * getDoubleValue(summand.edge);
		}
		if(newVal != oldVal) {
			setValue(newVal);
			return true;
		}
		return false;
	}
	
	private class Summand {
		double coeff;
		ModelEdge edge;
		
		Summand(ModelEdge edge, double coeff) {
			this.coeff = coeff;
			this.edge = edge;
		}
	}
}
