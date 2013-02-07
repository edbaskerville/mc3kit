package mc3kit;

import java.util.*;

/**
 * Class that accumulates values that may arrive out of order
 * from multiple threads.
 * 
 * @param <V> The value type. 
 */
public class Collector<V>
{
	protected int count;
	protected int counter;
	protected Map<Long, IterationData> iterationMap;
	
	public Collector(int count)
	{
		this.count = count;
		iterationMap = new HashMap<Long, IterationData>();
	}
	
	public synchronized List<V> takeValue(long iteration, int index, V value)
	{
		counter++;
		
		IterationData iterData = iterationMap.get(iteration);
		if(iterData == null)
		{
			iterData = new IterationData();
			iterationMap.put(iteration, iterData);
		}
		iterData.set(index, value);
		
		List<V> returnValues = null;
		if(iterData.isDone())
		{
			returnValues = iterData.getValues();
			iterationMap.remove(iteration);
		}
		return returnValues;
	}
	
	private class IterationData
	{
		int counter;
		ArrayList<V> values;
		
		IterationData()
		{
			counter = 0;
			values = new ArrayList<V>(count);
			for(int i = 0; i < count; i++)
				values.add(null);
		}
		
		void set(int index, V value)
		{
			assert(values.get(index) == null);
			values.set(index,  value);
			counter++;
		}
		
		ArrayList<V> getValues()
		{
			return values;
		}
		
		boolean isDone()
		{
			return counter == count;
		}
	}
}
