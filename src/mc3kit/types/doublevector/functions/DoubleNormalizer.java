package mc3kit.types.doublevector.functions;

import cern.colt.matrix.DoubleMatrix1D;
import mc3kit.*;
import mc3kit.model.*;
import mc3kit.types.doublevalue.*;
import mc3kit.types.doublevector.*;

public class DoubleNormalizer extends DoubleVectorFunction {
	DoubleMatrix1D value;
	DoubleValued[] values;
	
	public DoubleNormalizer(Model model, DoubleValued... values)
			throws MC3KitException {
		super(model);
		for(DoubleValued value : values) {
			model.addEdge(this, (ModelNode) value);
		}
		this.values = values.clone();
	}
	
	@Override
	public boolean update() {
		double sum = 0.0;
		for(DoubleValued value : values) {
			sum += value.getValue();
		}
		for(int i = 0; i < values.length; i++) {
			value.setQuick(i, values[i].getValue() / sum);
		}
		return true;
	}

	@Override
	public DoubleMatrix1D getValue() throws MC3KitException {
		return value;
	}

	@Override
	public double getValue(int index) throws MC3KitException {
		return value.getQuick(index);
	}

	@Override
	public int size() throws MC3KitException {
		return value.size();
	}
}
