package mc3kit.types.doublearray.functions;

import java.util.Arrays;

import mc3kit.*;
import mc3kit.model.*;
import mc3kit.types.doublearray.*;
import mc3kit.types.doublevalue.*;

public class DoubleSorter extends DoubleArrayFunction {
	
	DoubleValued[] values;
	
	protected DoubleSorter() {
	}
	
	public DoubleSorter(Model model, DoubleValued... values)
			throws MC3KitException {
		super(model, values.length);
		for(DoubleValued value : values) {
			model.addEdge(this, (ModelNode) value);
		}
		this.values = values.clone();
	}
	
	@Override
	public int getLength() {
		return 0;
	}
	
	@Override
	public boolean update() {
		for(int i = 0; i < values.length; i++) {
			setValue(i, values[i].getValue());
		}
		Arrays.sort(getValueArray());
		return true;
	}
}
