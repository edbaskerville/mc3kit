package mc3kit.types.doublevector.functions;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import mc3kit.*;
import mc3kit.model.*;
import mc3kit.types.doublevalue.*;
import mc3kit.types.doublevector.*;

public class DoubleSorter extends DoubleVectorFunction {
	DoubleValued[] values;
	DoubleMatrix1D unsorted;
	DoubleMatrix1D sorted;
	
	public DoubleSorter(Model model, DoubleValued... values)
			throws MC3KitException {
		super(model);
		for(DoubleValued value : values) {
			model.addEdge(this, (ModelNode) value);
		}
		this.values = values.clone();
		unsorted = new DenseDoubleMatrix1D(values.length);
		sorted = unsorted.viewSorted();
	}
	
	@Override
	public boolean update() {
		for(int i = 0; i < values.length; i++) {
			unsorted.setQuick(i, values[i].getValue());
		} 
		return true;
	}

	@Override
	public DoubleMatrix1D getValue() throws MC3KitException {
		return sorted;
	}

	@Override
	public double getValue(int index) throws MC3KitException {
		return sorted.getQuick(index);
	}

	@Override
	public int size() throws MC3KitException {
		return sorted.size();
	}
}
