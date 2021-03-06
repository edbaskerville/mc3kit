package mc3kit.types.partition.distributions;

import java.util.List;

import mc3kit.*;
import mc3kit.model.*;
import mc3kit.types.partition.PartitionDistribution;
import mc3kit.types.partition.PartitionVariable;
import mc3kit.util.SetPartition;

public class UniformPartitionDistribution extends PartitionDistribution {
	public UniformPartitionDistribution(Model model) {
		this(model, null);
	}
	
	public UniformPartitionDistribution(Model model, String name) {
		super(model, name);
	}
	
	@Override
	public double getLogP(Variable var) throws MC3KitException {
		return 0.0;
	}
	
	@Override
	public void sample(Variable var) throws MC3KitException {
		PartitionVariable partVar = (PartitionVariable) var;
		
		int n = partVar.getElementCount();
		int k = partVar.getGroupCount();
		
		List<List<Integer>> groups;
		if(partVar.allowsEmptyGroups()) {
			groups = SetPartition.generateRandomPartitionUpTo(getRng(), n, k);
			assert groups.size() <= k;
		}
		else {
			groups = SetPartition.generateRandomPartition(getRng(), n, k);
			assert groups.size() == k;
		}
		System.err.printf("initial groups: %s\n", groups);
		int[] gs = new int[n];
		for(int g = 0; g < groups.size(); g++) {
			for(int i : groups.get(g)) {
				gs[i] = g;
			}
		}
		partVar.setGroups(gs);
	}
}
