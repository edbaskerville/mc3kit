package mc3kit.util;

import java.util.*;

import mc3kit.MC3KitException;

public final class Utils {
  @SuppressWarnings("unchecked")
  public static <K,V> Map<K,V> makeMap(Object... keysAndValues) throws MC3KitException
  {
    if(keysAndValues.length % 2 != 0)
      throw new MC3KitException("Odd number of arguments to makeMap.");
    
    Map<K, V> map = new LinkedHashMap<K, V>();
    for(int i = 0; i < keysAndValues.length / 2; i++)
    {
      K key = (K)keysAndValues[2*i];
      V value = (V)keysAndValues[2*i+1];
      map.put(key, value);
    }
    
    return map;
  }
}
