package mc3kit.util;

import java.util.*;

public class MultiCounter<T extends Enum<?>>
{
	long count;
	Map<T, Long> counts;
	
	public MultiCounter()
	{
		count = 0;
		counts = new HashMap<T, Long>();
	}
	
	public long getCount()
	{
		return count;
	}
	
	public long getCount(T type)
	{
		return counts.containsKey(type) ? counts.get(type) : 0;
	}
	
	public double getRate(T type)
	{
		return count == 0 ? 0.0 : getCount(type) / (double)count;
	}
	
	public void reset()
	{
		count = 0;
		counts.clear();
	}
	
	public void record(T... types)
	{
		count++;
		for(T type : types)
		{
			increment(type);
		}
	}
	
	private void increment(T type)
	{
		counts.put(type, getCount(type) + 1);
	}
}
