package mc3kit.output;

import java.io.*;
import java.util.*;

import mc3kit.MC3KitException;

public class CsvDataLogger implements DataLogger
{
	private String filename;
	private String delimiter;
	private boolean useQuotes;
	
	private Set<String> keys;
	
	public CsvDataLogger(String filename, String delimiter, boolean useQuotes) throws MC3KitException
	{
		this.filename = filename;
		this.delimiter = delimiter;
		this.useQuotes = useQuotes;
	}
	
	@Override
	public synchronized void writeData(Map<String, Object> dataObject) throws MC3KitException
	{
		try
		{
			PrintWriter writer = new PrintWriter(new FileOutputStream(filename, true));
			
			// Establish keys to use and write headers if unestablished
			if(keys == null)
			{
				keys = new LinkedHashSet<String>(dataObject.keySet());
				
				int i = 0;
				for(String key : keys)
				{
					writer.write(formattedString(key));
					if(i < keys.size() - 1)
					{
						writer.write(delimiter);
					}
					i++;
				}
				writer.println();
			}
			
			// Write quoted, quote-escaped data as string
			int i = 0;
			for(String key : keys)
			{
				Object value = dataObject.get(key);
				if(value != null)
				{
					String str = formattedString(value.toString());
					writer.write(str);
				}
				if(i < keys.size() - 1)
				{
					writer.write(delimiter);
				}
				i++;
			}
			
			writer.println();
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			throw new MC3KitException("File not found when setting up file output.", e);
		}
	}
	
	private String formattedString(String str) throws MC3KitException
	{
		if(useQuotes)
			return String.format("\"%s\"", str.replaceAll("\"", "\"\""));
		else
		{
			if(str.contains(delimiter))
				throw new MC3KitException(
					String.format("Delimiter found in string in non-quote mode: %s.", str)
				);
			return str;
		}
	}
}
