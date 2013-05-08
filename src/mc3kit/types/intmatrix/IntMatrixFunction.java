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

package mc3kit.types.intmatrix;

import mc3kit.model.Function;
import mc3kit.model.Model;

public abstract class IntMatrixFunction extends Function implements IntMatrixValued {
	int rowCount;
	int columnCount;
	protected int[][] value;
	
	protected IntMatrixFunction(Model model, int rowCount, int columnCount) {
		super(model);
		this.rowCount = rowCount;
		this.columnCount = columnCount;
		this.value = new int[rowCount][columnCount];
	}
	
	@Override
	public int getValue(int r, int c) {
		return value[r][c];
	}
	
	public int[][] getValue() {
		return value;
	}
	
	public int rowCount() {
		return rowCount;
	}
	
	public int columnCount() {
		return columnCount;
	}
	
	protected void setValue(int row, int col, int val) {
		value[row][col] = val;
	}
}
