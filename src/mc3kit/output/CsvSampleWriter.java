package mc3kit.output;

import java.io.*;
import java.util.*;

import mc3kit.*;

public class CsvSampleWriter implements SampleWriter
{
	private PrintWriter writer;
	private String delimiter;
	private boolean useQuotes;
	
	private Set<String> keys;
	
	public CsvSampleWriter(String filename, String delimiter, boolean useQuotes) throws FileNotFoundException
	{
		this.writer = new PrintWriter(new FileOutputStream(filename, false));
		this.delimiter = delimiter;
		this.useQuotes = useQuotes;
	}
	
	@Override
	public synchronized void writeSample(Model model) throws MC3KitException
	{
	  Map<String, String> samp = model.makeFlatSample();
	  
		// Establish keys to use and write headers if unestablished
		if(keys == null)
		{
			keys = new LinkedHashSet<String>(samp.keySet());
			
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
			String value = samp.get(key);
			if(value != null)
			{
				String str = formattedString(value);
				writer.write(str);
			}
			if(i < keys.size() - 1)
			{
				writer.write(delimiter);
			}
			i++;
		}
		writer.println();
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
