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

package mc3kit.types.doublevector;

import cern.colt.matrix.DoubleMatrix1D;
import mc3kit.MC3KitException;
import mc3kit.model.Model;
import mc3kit.model.Variable;

public class DoubleVectorVariable extends Variable implements DoubleVectorValued {
	
	private DoubleMatrix1D value;
	
	/**
	 * Constructor for an <i>observed</i> double-valued variable.
	 * 
	 * @param model
	 * @param value
	 */
	public DoubleVectorVariable(Model model, DoubleMatrix1D value) {
		super(model, null, true);
		this.value = value;
	}
	
	@Override
	public DoubleMatrix1D getValue() {
		return value;
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
	public double getValue(int index) {
		return value.getQuick(index);
	}

	@Override
	public int size() throws MC3KitException {
		return value.size();
	}
}
