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

import static java.lang.String.format;
import mc3kit.MC3KitException;
import mc3kit.step.univariate.VariableProposer;

public abstract class Variable extends ModelNode {
	private ModelEdge distEdge;
	
	private boolean observed;
	
	private double logP;
	
	protected Variable() {
	}
	
	protected Variable(Model model) {
		this(model, null, true);
	}
	
	protected Variable(Model model, Distribution dist) throws MC3KitException {
		this(model, null, true, dist);
	}
	
	protected Variable(Model model, String name, boolean observed) {
		super(name);
		this.observed = observed;
		
		if(model != null) {
			model.addVariable(this);
		}
	}
	
	protected Variable(Model model, String name, boolean observed,
			Distribution dist) throws MC3KitException {
		this(model, name, observed);
		setDistribution(dist);
	}
	
	public boolean isObserved() {
		return observed;
	}
	
	protected void setLogP(double logP) {
		this.logP = logP;
	}
	
	public double getLogP() {
		return logP;
	}
	
	public void sample() throws MC3KitException {
		Distribution dist = getDistribution();
		if(dist == null) {
			throw new MC3KitException(format("Distribution for %s is null",
					getName()));
		}
		
		dist.sample(this);
		setChanged();
		notifyObservers();
	}
	
	public Variable setDistribution(Distribution dist) throws MC3KitException {
		distEdge = updateEdge(distEdge, dist);
		return this;
	}
	
	public Distribution getDistribution() {
		return distEdge == null ? null : (Distribution) distEdge.getHead();
	}
	
	public VariableProposer makeProposer() throws MC3KitException {
		Distribution dist = getDistribution();
		if(dist == null) {
			throw new MC3KitException(format("No distribution for variable %s",
					this));
		}
		return getDistribution().makeVariableProposer(getName());
	}
	
	public Object makeOutputObject() {
		throw new UnsupportedOperationException(format(
				"Variable %s doesn't support output.", this));
	}
	
	public String makeOutputString() throws MC3KitException {
		throw new UnsupportedOperationException(format(
				"Variable %s doesn't support output as string.", this));
	}
	
	public Object toDbValue() throws MC3KitException {
		throw new UnsupportedOperationException(format(
				"Variable %s doesn't support db output.", this));
	}
	
	public void loadFromDbValue(Object value) throws MC3KitException {
		throw new UnsupportedOperationException(format(
				"Variable %s doesn't support db loading.", this));
	}
	
	public boolean canManipulateGraph() {
		return false;
	}
	
	@Override
	public boolean update() throws MC3KitException {
		Distribution dist = getDistribution();
		if(dist == null) {
			if(observed) {
				setLogP(0.0);
			}
			else {
				throw new MC3KitException(format(
						"Variable %s doesn't have a distribution", this));
			}
		}
		else {
			setLogP(dist.getLogP(this));
		}
		
		return false;
	}
}
