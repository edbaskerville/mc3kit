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

package mc3kit.types.doublearray;

import mc3kit.MC3KitException;
import mc3kit.model.Distribution;
import mc3kit.model.Model;
import mc3kit.model.Variable;

public class DoubleArrayVariable extends Variable implements DoubleArrayValued {
	
	private double[] value;
	
	/**
	 * Constructor for an <i>observed</i> double-valued variable.
	 * 
	 * @param model
	 * @param value
	 */
	public DoubleArrayVariable(Model model, double[] value) {
		super(model, null, true);
		this.value = value;
	}
	
	@Override
	public double[] getValue() {
		return value;
	}
	
	@Override
	public void setValue(double[] value) {
		if(isObserved()) {
			throw new UnsupportedOperationException(
					"Can't set value on an observed variable.");
		}
		
		if(this.value != value) {
			this.value = value;
			setChanged();
			notifyObservers();
		}
	}
	
	@Override
	public DoubleArrayVariable setDistribution(Distribution dist)
			throws MC3KitException {
		throw new MC3KitException("Doesn't support distributions.");
	}
	
	public boolean valueIsValid(double value) throws MC3KitException {
		throw new MC3KitException(
				"Can't ask whether value is valid without distribution.");
	}
	
	@Override
	public Object makeOutputObject() {
		return value;
	}
	
	@Override
	public String makeOutputString() throws MC3KitException {
		throw new MC3KitException("Can't output");
	}
	
	@Override
	public Object toDbValue() throws MC3KitException {
		throw new MC3KitException("Can't output");
	}
	
	@Override
	public void loadFromDbValue(Object value) throws MC3KitException {
		throw new MC3KitException("Can't load from DB");
	}
	
	@Override
	public int getLength() {
		return value.length;
	}
	
	@Override
	public double getValue(int index) {
		return value[index];
	}
	
	@Override
	public void setValue(int index, double value) throws MC3KitException {
		throw new MC3KitException("can't set value");
	}
}
