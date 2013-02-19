package mc3kit.output;

import java.io.FileNotFoundException;
import static java.lang.String.*;
import java.util.*;

import mc3kit.*;
import mc3kit.output.DataLoggerFactory;

public class SampleOutputStep implements Step
{
  String filename;
  String format;
  boolean useQuotes;
  long thin;
  int chainId;
	
  public SampleOutputStep(String filename, long thin) {
    this(filename, null, false, thin, 0);
  }
  
	public SampleOutputStep(String filename, String format, boolean useQuotes, long thin, int chainId) {
	  this.filename = filename;
	  this.format = format;
	  this.useQuotes = useQuotes;
	  this.thin = thin;
	  this.chainId = chainId;
	}

	/*** METHODS ***/

	@Override
	public List<Task> makeTasks(int chainCount) throws MC3KitException
	{
	  List<Task> Tasks = new ArrayList<Task>();
		Tasks.add(new SampleOutputTask());
		return Tasks;
	}
	
	/*** Task CLASS ***/
	
	private class SampleOutputTask implements Task
	{
		SampleWriter writer;
		
		private long iterationCount;
		
		@Override
		public int[] getChainIds()
		{
			return new int[] { chainId };
		}
		
		SampleOutputTask() throws MC3KitException
		{
			try {
        writer = DataLoggerFactory.getFactory().createDataLogger(filename, format, useQuotes);
      }
      catch(FileNotFoundException e) {
        throw new MC3KitException("File not found", e);
      }
		}
		
		@Override
		public void step(Chain[] chains) throws MC3KitException
		{
      iterationCount++;
      
      Chain chain = chains[0];
			if(iterationCount % thin == 0)
			{
	      chain.getLogger().info(format("Writing sample %d", iterationCount));
				Model model = chain.getModel();
				
				writer.writeSample(model);
			}
		}
	}
}
