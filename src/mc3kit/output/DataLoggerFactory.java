package mc3kit.output;

import mc3kit.MC3KitException;

public class DataLoggerFactory
{
	private static DataLoggerFactory factory;
	
	public static synchronized DataLoggerFactory getFactory()
	{
		if(factory == null)
		{
			factory = new DataLoggerFactory();
		}
		return factory;
	}
	
	private DataLoggerFactory()
	{
	}
	
	public DataLogger createDataLogger(String filename) throws MC3KitException
	{
		return createDataLogger(filename, null, false);
	}
	
	public DataLogger createDataLogger(String filename, String format) throws MC3KitException
	{
		return createDataLogger(filename, format, false);
	}
	
	public DataLogger createDataLogger(String filename, String format, boolean useQuotes) throws MC3KitException
	{
		if(filename == null)
			return new NullDataLogger();
		
		if(format == null)
		{
			String[] filenamePieces = filename.split("\\.");
			if(filenamePieces.length > 1)
				format = filenamePieces[filenamePieces.length - 1];
		}
		
		if(format.equalsIgnoreCase("jsons"))
		{
			return new JsonsDataLogger(filename);
		}
		else if(format.equalsIgnoreCase("csv"))
		{
			return new CsvDataLogger(filename, ",", useQuotes);
		}
		else if(format.equalsIgnoreCase("txt"))
		{
			return new CsvDataLogger(filename, "\t", useQuotes);
		}
		else
		{
			throw new MC3KitException(
				String.format("Unknown format %s.", format)
			);
		}
	}
}
