package mc3kit.types.doublevector;

import mc3kit.MC3KitException;
import mc3kit.model.Model;
import mc3kit.model.ModelNode;
import mc3kit.types.doublevalue.DoubleFunction;

public class DoubleVectorEntry extends DoubleFunction {
	private DoubleVectorValued array;
	private int index;
	
	protected DoubleVectorEntry() {
	}
	
	public DoubleVectorEntry(Model model, DoubleVectorValued array, int index)
			throws MC3KitException {
		super(model);
		model.addEdge(this, (ModelNode) array);
		this.array = array;
		this.index = index;
	}
	
	@Override
	public boolean update() throws MC3KitException {
		double oldValue = getValue();
		double value = array.getValue(index);
		
		if(value != oldValue) {
			setValue(value);
			return true;
		}
		else {
			return false;
		}
	}
}
