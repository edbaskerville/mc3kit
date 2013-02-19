package mc3kit.partition;

import mc3kit.*;
import mc3kit.util.*;

import java.util.*;

import cern.jet.random.Uniform;

public class PartitionVariable extends Variable
{
	int n;
	int k;
	int[] assignment;
	IterableBitSet[] groups;
	
	List<Association> associations;
	
	public PartitionVariable(Model model, String name, int n, int k) throws MC3KitException
	{
		super(model, name, false);
		
		this.n = n;
		this.k = k;
		
		assignment = new int[n];
		groups = new IterableBitSet[k];
		for(int i = 0; i < k; i++)
			groups[i] = new IterableBitSet(n);
		
		associations = new ArrayList<Association>();
	}
	
	public void associate(ModelNode[] tails, ModelNode[] heads, Associator associator) {
    if(tails.length != n)
      throw new IllegalArgumentException("Wrong number of tails");
    if(heads.length != k)
      throw new IllegalArgumentException("Wrong number of heads");
    
	  Association association = new Association(tails, heads, associator);
	  associations.add(association);
	}
	
	public void associateVariablesWithDistributions(ModelNode[] vars, ModelNode[] dists) {
	  associate(vars, dists, new Associator() {
      @Override
      public void associate(ModelNode tail, ModelNode head) throws ModelException {
        ((Variable)tail).setDistribution((Distribution)head);
      }
	  });
	}
	
	public IterableBitSet getGroup(int g)
	{
		return groups[g];
	}
	
	public int getGroupId(int i)
	{
		return assignment[i];
	}
	
	public void setGroup(int i, int g) throws ModelException
	{
		groups[assignment[i]].clear(i);
		groups[g].set(i);
		assignment[i] = g;
		
		for(Association asn : associations)
		{
			asn.setGroup(i, g);
		}
		
		setChanged();
		notifyObservers();
	}

	@Override
	public void sample() throws ModelException
	{
		Distribution dist = getDistribution();
		if(dist == null)
		{
			Uniform unif = new Uniform(getRng());
			
			// Choose group numbers uniformly randomly
			// conditioned on all groups having at least one member
			boolean done;
			do
			{
				// Reset group bitsets
				for(int i = 0; i < k; i++)
				{
					groups[i].clear();
				}
				
				// Just choose group number uniformly randomly
				for(int i = 0; i < n; i++)
				{
					int g = unif.nextIntFromTo(0, k - 1);
					assignment[i] = g;
					groups[g].set(i);
				}
				
				done = true;
				
				for(int i = 0; i < k; i++)
				{
					if(groups[i].cardinality() == 0)
					{
						done = false;
						break;
					}
				}
			} while(!done);
		}
		else
		{
			getDistribution().sample(this);
		}
		
		// Associate vars and priors
		for(Association asn : associations)
		{
			for(int i = 0; i < n; i++)
			{
				asn.setGroup(i, assignment[i]);
			}
		}
    setChanged();
    notifyObservers();
	}
	
	public int getElementCount()
	{
		return n;
	}
	
	public int getGroupCount()
	{
		return k;
	}
	
	public int getGroupSize(int g)
	{
		return groups[g].cardinality();
	}
	
	public int getGroupSizeForItem(int i)
	{
		return groups[assignment[i]].cardinality();
	}
	
	private class Association
	{
		ModelNode[] tails;
		ModelNode[] heads;
		Associator associator;
		
		public Association(ModelNode[] tails, ModelNode[] heads, Associator associator)
		{
			this.tails = tails;
			this.heads = heads;
			this.associator = associator;
		}
		
		public void setGroup(int i, int g) throws ModelException
		{
		  associator.associate(tails[i], heads[g]);
		}
	}

  @Override
  public VariableProposer makeProposer() {
    return new PartitionProposer(getName());
  }

  @Override
  public Object makeOutputObject() {
    if(k == 1) {
      return null;
    }
    return assignment.clone();
  }
}
