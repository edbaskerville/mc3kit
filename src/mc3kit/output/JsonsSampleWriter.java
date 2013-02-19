package mc3kit.output;

import java.io.*;

import mc3kit.*;

import com.google.gson.*;

public class JsonsSampleWriter implements SampleWriter
{
	private PrintWriter writer;
	private Gson gson;
	
	public JsonsSampleWriter(String filename) throws FileNotFoundException
	{
		this.gson = new GsonBuilder()
			.serializeSpecialFloatingPointValues()
			.setPrettyPrinting()
			.create();
		this.writer = new PrintWriter(new FileOutputStream(filename, false));
	}
	
	@Override
	public synchronized void writeSample(Model model) throws MC3KitException
	{
	  writer.println("---");
		gson.toJson(model.makeHierarchicalSample(), writer);
		writer.println();
	}
}
