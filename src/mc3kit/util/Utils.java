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

package mc3kit.util;

import java.nio.*;
import java.util.*;

public final class Utils {
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> makeMap(Object... keysAndValues) {
		if(keysAndValues.length % 2 != 0)
			throw new IllegalArgumentException(
					"Odd number of arguments to makeMap.");
		
		Map<K, V> map = new LinkedHashMap<K, V>();
		for(int i = 0; i < keysAndValues.length / 2; i++) {
			K key = (K) keysAndValues[2 * i];
			V value = (V) keysAndValues[2 * i + 1];
			map.put(key, value);
		}
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> makeHierarchicalMap(
			Map<String, Object> flatMap) {
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
					curMap = (Map<String, Object>) curMap.get(subKey);
				}
				i++;
			}
		}
		return hMap;
	}
	
	public static byte[] toBytes(double[] x) {
		ByteBuffer buf = ByteBuffer.allocateDirect(x.length * 8);
		buf.position(0);
		buf.asDoubleBuffer().put(x);
		byte[] bytes = new byte[x.length * 8];
		buf.get(bytes);
		return bytes;
	}
	
	public static double[] fromBytes(byte[] x) {
		ByteBuffer buf = ByteBuffer.wrap(x);
		double[] dbls = new double[x.length / 8];
		buf.asDoubleBuffer().get(dbls);
		return dbls;
	}
}
