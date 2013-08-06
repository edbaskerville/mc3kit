package mc3kit.util;

import java.math.BigInteger;
import java.util.*;
import static mc3kit.util.Random.*;

import cern.jet.random.engine.RandomEngine;

public class SetPartition
{
	static
	{
		cacheStirling = Collections.synchronizedMap(
			new HashMap<Pair<Integer>, BigInteger>());
		cacheBell = Collections.synchronizedMap(
			new HashMap<Integer, BigInteger>());
	}
	
	// Cache of Stirling numbers of the second kind S(n,k)
	private static Map<Pair<Integer>, BigInteger> cacheStirling;
	
	// Cache of Bell numbers B(n) = sum(S(n,k))
	private static Map<Integer, BigInteger> cacheBell;
	
	public static BigInteger getPartitionCount(int n)
	{
		if(n <= 0) return BigInteger.ZERO;
		
		BigInteger bellN = cacheBell.get(n);
		if(bellN == null)
		{
			bellN = BigInteger.ZERO;
			for(int k = 1; k <= n; k++)
			{
				bellN = bellN.add(getPartitionCount(n, k));
			}
			cacheBell.put(n, bellN);
		}
		
		return bellN; 
	}
	
	public static BigInteger getPartitionCount(int n, int k)
	{
		if(n < 0 || k < 0) return BigInteger.ZERO;
		if(n < k) return BigInteger.ZERO;
		
		if(n == k) return BigInteger.ONE;
		if(k == 1) return BigInteger.ONE;
		
		Pair<Integer> pair = new Pair<Integer>(n, k);
		BigInteger value = cacheStirling.get(pair);
		if(value == null)
		{
			value = getPartitionCount(n - 1, k - 1).add(
					getPartitionCount(n - 1, k).multiply(new BigInteger(String.format("%d", k))));
			cacheStirling.put(pair, value);
		}
		return value;
	}
	
	public static List<List<Integer>> getPartition(int n, int k, BigInteger partitionNumber)
	{
		if(partitionNumber.compareTo(BigInteger.ZERO) < 0) return null;
		if(partitionNumber.compareTo(getPartitionCount(n, k)) >= 0) return null;
		
		List<List<Integer>> partition = new ArrayList<List<Integer>>();
		
		if(n == 1 && k == 1)
		{
			partition.add(new ArrayList<Integer>());
			partition.get(0).add(0);
			return partition;
		}
		
		BigInteger partitionCount_n1k1 = getPartitionCount(n-1, k-1);
		if(partitionNumber.compareTo(partitionCount_n1k1) < 0)
		{
			partition.add(new ArrayList<Integer>());
			partition.get(0).add(0);
			
			List<List<Integer>> subPartition = getPartition(n - 1, k - 1, partitionNumber);
			incrementLists(subPartition);
			partition.addAll(subPartition);
		}
		else
		{
			BigInteger kBI = new BigInteger(String.format("%d", k));
			
			BigInteger[] divRem = partitionNumber.subtract(partitionCount_n1k1).divideAndRemainder(kBI);
			
			List<List<Integer>> subPartition = getPartition(n - 1, k, divRem[0]);
			incrementLists(subPartition);
			partition.addAll(subPartition);
			
			int groupIndex = divRem[1].intValue();
			List<Integer> group = partition.get(groupIndex);
			group.add(0, 0);
			partition.remove(groupIndex);
			partition.add(0, group);
		}
		
		return partition;
	}
	
	private static void incrementLists(List<List<Integer>> lists)
	{
		for(List<Integer> list : lists)
		{
			incrementList(list);
		}
	}
	
	private static void incrementList(List<Integer> list)
	{
		int size = list.size();
		for(int i = 0; i < size; i++)
		{
			list.set(i, list.get(i) + 1);
		}
	}
	
	public static List<List<Integer>> generateRandomPartition(RandomEngine rng, int n, int k)
	{
		BigInteger partitionCount = getPartitionCount(n, k);
		if(partitionCount.equals(BigInteger.ZERO))
			return null;
		
		BigInteger partitionNumber = nextBigIntegerFromTo(rng, BigInteger.ZERO,
				partitionCount.subtract(BigInteger.ONE));
		
		return getPartition(n, k, partitionNumber);
	}
}
