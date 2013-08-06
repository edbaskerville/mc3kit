package mc3kit.model;

import mc3kit.MC3KitException;

public class IndexedEdge extends ModelEdge {
	private int index;
	
	public IndexedEdge(Model model, int index, ModelNode tail, ModelNode head)
			throws MC3KitException {
		super(model, tail, head);
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
}
