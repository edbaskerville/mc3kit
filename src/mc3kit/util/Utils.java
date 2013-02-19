package mc3kit.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  
  @SuppressWarnings("unchecked")
  public static Map<String, Object> makeHierarchicalMap(Map<String, Object> flatMap) {
    Map<String, Object> hMap = new LinkedHashMap<String, Object>();
    
    for(String flatKey : flatMap.keySet()) {
      String[] subKeys = flatKey.split("\\.");
      
      Map<String, Object> curMap = hMap;
      int i = 0;
      for(String subKey : subKeys) {
        if(i == subKeys.length - 1) {
          curMap.put(subKey, flatMap.get(flatKey));
        }
        else {
          if(!curMap.containsKey(subKey)) {
            curMap.put(subKey, new LinkedHashMap<String, Object>());
          }
          curMap = (Map<String, Object>)curMap.get(subKey);
        }
        i++;
      }
    }
    return hMap;
  }
}
