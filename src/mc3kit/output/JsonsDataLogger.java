package mc3kit.output;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;

import mc3kit.MC3KitException;

import com.google.gson.*;

public class JsonsDataLogger implements DataLogger
{
	private String filename;
	private Gson gson;
	
	public JsonsDataLogger(String filename)
	{
		this.filename = filename;
		this.gson = new GsonBuilder()
			.serializeSpecialFloatingPointValues()
			.setPrettyPrinting()
			.create();
	}
	
	@Override
	public synchronized void writeData(Map<String, Object> dataObject) throws MC3KitException
	{
		try
		{
			PrintWriter writer = new PrintWriter(new FileOutputStream(filename, true));
			writer.println("---");
			
			gson.toJson(dataObject, writer);
			
			writer.println();
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			throw new MC3KitException("File not found when setting up file output.", e);
		}
	}
}
