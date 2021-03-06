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

package mc3kit.types.partition;

import mc3kit.*;
import mc3kit.model.Distribution;
import mc3kit.model.Model;
import mc3kit.model.ModelNode;
import mc3kit.model.Variable;
import mc3kit.step.univariate.VariableProposer;
import mc3kit.util.*;
import java.util.*;

import com.google.gson.*;

import cern.jet.random.Uniform;

public class PartitionVariable extends Variable {
	int n;
	int k;
	
	private boolean allowsEmptyGroups;
	boolean useGibbs;
	
	int[] assignment;
	IterableBitSet[] groups;
	
	List<IndexAssociator> indexAssociators;
	List<Association> associations;
	
	protected PartitionVariable() {
	}
	
	public PartitionVariable(Model model, String name, int n, int k)
			throws MC3KitException {
		this(model, name, n, k, false);
	}
	
	public PartitionVariable(Model model, String name, int n, int k,
			boolean allowsEmptyGroups) throws MC3KitException {
		this(model, name, n, k, allowsEmptyGroups, false);
	}
	
	public PartitionVariable(Model model, String name, int n, int k,
			boolean allowsEmptyGroups, boolean useGibbs) throws MC3KitException {
		super(model, name, false);
		
		this.n = n;
		this.k = k;
		this.setAllowsEmptyGroups(allowsEmptyGroups);
		this.useGibbs = useGibbs;
		
		assignment = new int[n];
		groups = new IterableBitSet[k];
		for(int i = 0; i < k; i++)
			groups[i] = new IterableBitSet(n);
		
		indexAssociators = new ArrayList<IndexAssociator>();
		associations = new ArrayList<Association>();
	}
	
	public boolean allowsEmptyGroups() {
		return allowsEmptyGroups;
	}
	
	public void setAllowsEmptyGroups(boolean allowsEmptyGroups) {
		this.allowsEmptyGroups = allowsEmptyGroups;
	}
	
	public void associate(IndexAssociator associator) {
		indexAssociators.add(associator);
	}
	
	public void associate(ModelNode[] tails, ModelNode[] heads,
			Associator associator) {
		if(tails.length != n)
			throw new IllegalArgumentException("Wrong number of tails");
		if(heads.length != k)
			throw new IllegalArgumentException("Wrong number of heads");
		
		Association association = new Association(tails, heads, associator);
		associations.add(association);
	}
	
	public void associateVariablesWithDistributions(ModelNode[] vars,
			ModelNode[] dists) {
		associate(vars, dists, new DistributionAssociator());
	}
	
	public IterableBitSet getGroup(int g) {
		return groups[g];
	}
	
	public int getGroupId(int i) {
		return assignment[i];
	}
	
	public void setGroups(int[] gs) throws MC3KitException {
		assert gs.length == n;
		
		// Reset group bitsets
		for(int i = 0; i < k; i++) {
			groups[i].clear();
		}
		
		for(int i = 0; i < gs.length; i++) {
			int gi = gs[i];
			assert gi < k;
			assignment[i] = gi;
			groups[gi].set(i);
		}
		
		// Call index associators
		for(IndexAssociator asr : indexAssociators) {
			for(int i = 0; i < n; i++) {
				asr.associate(i, assignment[i]);
			}
		}
		
		// Associate vars and priors
		for(Association asn : associations) {
			for(int i = 0; i < n; i++) {
				asn.setGroup(i, assignment[i]);
			}
		}
		setChanged();
		notifyObservers();
	}
	
	public void setGroup(int i, int g) throws MC3KitException {
		groups[assignment[i]].clear(i);
		groups[g].set(i);
		assignment[i] = g;
		
		for(IndexAssociator asr : indexAssociators) {
			asr.associate(i, g);
		}
		
		for(Association asn : associations) {
			asn.setGroup(i, g);
		}
		
		setChanged();
		notifyObservers();
	}
	
	@Override
	public void sample() throws MC3KitException {
		Distribution dist = getDistribution();
		if(dist == null) {
			Uniform unif = new Uniform(getRng());
			
			// Choose group numbers uniformly randomly
			// conditioned on all groups having at least one member
			boolean done;
			int[] gs = new int[n];
			do {
				int[] groupCounts = new int[k];
				
				// Just choose group number uniformly randomly
				for(int i = 0; i < n; i++) {
					gs[i] = unif.nextIntFromTo(0, k - 1);
					groupCounts[gs[i]]++;
				}
				done = true;
				
				for(int g = 0; g < k; g++) {
					if(groupCounts[g] == 0) {
						done = false;
						break;
					}
				}
			} while(!done);
			
			setGroups(gs);
		}
		else {
			getDistribution().sample(this);
		}
	}
	
	@Override
	public boolean canManipulateGraph() {
		return associations.size() > 0 || indexAssociators.size() > 0;
	}
	
	public int getElementCount() {
		return n;
	}
	
	public int getGroupCount() {
		return k;
	}
	
	public int getNonemptyGroupCount() {
		if(allowsEmptyGroups) {
			int count = 0;
			for(IterableBitSet group : groups) {
				if(group.cardinality() > 0) {
					count++;
				}
			}
			return count;
		}
		return k;
	}
	
	public int getGroupSize(int g) {
		return groups[g].cardinality();
	}
	
	public int getGroupSizeForItem(int i) {
		return groups[assignment[i]].cardinality();
	}
	
	private class Association {
		ModelNode[] tails;
		ModelNode[] heads;
		Associator associator;
		
		public Association(ModelNode[] tails, ModelNode[] heads,
				Associator associator) {
			this.tails = tails;
			this.heads = heads;
			this.associator = associator;
		}
		
		public void setGroup(int i, int g) throws MC3KitException {
			associator.associate(tails[i], heads[g]);
		}
	}
	
	private class DistributionAssociator implements Associator {
		@Override
		public void associate(ModelNode tail, ModelNode head)
				throws MC3KitException {
			((Variable) tail).setDistribution((Distribution) head);
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
	
	@Override
	public String makeOutputString() {
		return new Gson().toJson(assignment);
	}
	
	@Override
	public Object toDbValue() {
		return getGson().toJson(assignment);
	}
	
	@Override
	public void loadFromDbValue(Object value) throws MC3KitException {
		setGroups(getGson().fromJson((String) value, int[].class));
	}
}
