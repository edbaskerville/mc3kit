package mc3kit.util;

import java.io.Serializable;

public class Pair<T extends Comparable<T>> implements Comparable<Pair<T>>, Serializable
{
	private static final long serialVersionUID = 1L;
	
	public T first;
	public T second;
	private final int hash;
	
	public Pair(T first, T second)
	{
		this.first = first;
		this.second = second;
		hash = (first == null ? 0 : first.hashCode() * 31) + (second == null ? 0 : second.hashCode());
	}
	
	@Override
	public int hashCode()
	{
		return hash;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null || !getClass().isInstance(obj)) return false;
		
		Pair<T> pair = getClass().cast(obj);
		
		return (first == null ? pair.first == null : first.equals(pair.first))
		&& (second == null ? pair.second == null : second.equals(pair.second));
	}
	
	@Override
	public String toString()
	{
		return String.format("(%s, %s)", first, second);
	}

	public int compareTo(Pair<T> o)
	{
		int firstComp = first.compareTo(o.first);
		if(firstComp == 0)
			return second.compareTo(o.second);
		else return firstComp;
	}
}
