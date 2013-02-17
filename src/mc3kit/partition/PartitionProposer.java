package mc3kit.partition;

import java.util.*;

import static java.lang.Math.*;
import static mc3kit.util.Random.*;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import mc3kit.*;
import static mc3kit.util.Math.*;

public class PartitionProposer extends VariableProposer
{
	protected PartitionProposer(String name) {
    super(name);
  }

  @Override
	public void step(Model model)
			throws MC3KitException
	{
	  Chain chain = model.getChain();
	  RandomEngine rng = chain.getRng();
	  PartitionVariable var = (PartitionVariable)model.get(getName());
	  
		if(var.getGroupCount() == 1)
			return;
		
		Uniform unif = new Uniform(rng);
		
		int n = var.getElementCount();
		int k = var.getGroupCount();
		
		for(int a = 0; a < n; a++)
		{
			// Get a random item i in a group of size >= 2
			BitSet movable = getMovableItems(var, n, k);
			int i = getRandomSetBit(unif, movable);
			int iGroup = var.getGroupId(i);
			int iGroupSize = var.getGroupSize(iGroup);
			assert(iGroupSize >= 2);
			
			// Get a random item j whose group will be destination for i
			int j;
			int jGroup;
			do
			{
				j = nextIntFromToExcept(unif, 0, n - 1, i);
				jGroup = var.getGroupId(j);
			} while(jGroup == iGroup);
			int jGroupSize = var.getGroupSize(jGroup);
			
			// Log-probability of moving i to j's group
			double logForwardProposal = -log(movable.cardinality())
				+ log(jGroupSize) - log(n - iGroupSize);
			
			double oldLogPrior = model.getLogPrior();
			double oldLogLikelihood = model.getLogLikelihood();
			
			model.beginProposal();
			var.setGroup(i, jGroup);
			model.endProposal();
			
			double newLogPrior = model.getLogPrior();
			double newLogLikelihood = model.getLogLikelihood();
			
			movable = getMovableItems(var, n, k);
			double logReverseProposal = -log(movable.cardinality())
				+ log(jGroupSize + 1) - log(n - (iGroupSize - 1));
			
			boolean accepted = shouldAcceptMetropolisHastings(rng,
			  chain.getPriorHeatExponent(), chain.getLikelihoodHeatExponent(),
				oldLogPrior, oldLogLikelihood, newLogPrior, newLogLikelihood,
				logReverseProposal - logForwardProposal);
			
			if(accepted)
			{
			  model.acceptProposal();
				recordAcceptance();
			}
			else
			{
				model.beginRejection();
				var.setGroup(i, iGroup);
				model.endRejection();
				recordRejection();
			}
		}
	}
	
  
  
	@Override
  public void tune(double targetRate) throws MC3KitException {
    super.tune(targetRate);
  }

  BitSet getMovableItems(PartitionVariable var, int n, int k)
	{
		BitSet movable = new BitSet(n);
		for(int i = 0; i < k; i++)
		{
			BitSet groupSet = var.getGroup(i);
			assert(groupSet.cardinality() > 0);
			if(groupSet.cardinality() > 1)
				movable.or(groupSet);
		}
		return movable;
	}
	
	int getRandomSetBit(Uniform unif, BitSet movable)
	{
		int whichSetBit = unif.nextIntFromTo(0, movable.cardinality() - 1);
		
		int whichBit = -1;
		for(int i = 0; i <= whichSetBit; i++)
		{
			whichBit = movable.nextSetBit(whichBit + 1);
		}
		assert(whichBit != -1);
		
		return whichBit;
	}
}
