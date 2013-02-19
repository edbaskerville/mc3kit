package mc3kit.util;

import com.google.gson.*;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class JsonUtils
{
	public static <T> T parseObject(Class<T> cls, String filename) throws FileNotFoundException
	{
		return parseObject(cls, new FileReader(filename));
	}
	
	public static  <T> T parseObject(Class<T> cls, Reader reader)
	{
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonObject raw = parser.parse(reader).getAsJsonObject();
		return gson.fromJson(raw, cls);
	}
	
	public static <T> List<T> parseList(Class<T> cls, String filename) throws FileNotFoundException
	{
		return parseList(cls, new FileReader(filename));
	}
	
	public static <T> List<T> parseList(Class<T> cls, Reader reader)
	{
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		
		JsonObject raw = parser.parse(reader).getAsJsonObject();
		
		List<T> list = new ArrayList<T>();
		for(Entry<String, JsonElement> entry : raw.entrySet())
		{
			T obj = gson.fromJson(entry.getValue(), cls);
			list.add(obj);
		}
		
		return list;
	}
	
	public static <T> Map<String, T> parseMap(Class<T> cls, String filename) throws FileNotFoundException
	{
		return parseMap(cls, new FileReader(filename));
	}
	
	public static <T> Map<String, T> parseMap(Class<T> cls, Reader reader)
	{
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		
		JsonObject raw = parser.parse(reader).getAsJsonObject();
		
		Map<String, T> map = new LinkedHashMap<String, T>();
		for(Entry<String, JsonElement> entry : raw.entrySet())
		{
			T obj = gson.fromJson(entry.getValue(), cls);
			map.put(entry.getKey(), obj);
		}
		
		return map;
	}
}
