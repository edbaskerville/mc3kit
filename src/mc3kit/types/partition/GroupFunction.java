package mc3kit.types.partition;

import mc3kit.MC3KitException;
import mc3kit.model.Function;
import mc3kit.model.Model;
import mc3kit.util.IterableBitSet;

public class GroupFunction extends Function {
	private PartitionVariable partVar;
	private int groupNum;
	private IterableBitSet group;
	
	public GroupFunction(Model model, PartitionVariable partVar, int groupNum)
			throws MC3KitException {
		super(model);
		
		this.partVar = partVar;
		this.groupNum = groupNum;
		
		model.addEdge(this, partVar);
	}
	
	@Override
	public boolean update() {
		IterableBitSet oldGroup = group;
		group = (IterableBitSet) partVar.getGroup(groupNum).clone();
		if(!group.equals(oldGroup)) {
			return true;
		}
		return false;
	}
	
	public IterableBitSet getGroup() {
		return (IterableBitSet)group.clone();
	}
}
