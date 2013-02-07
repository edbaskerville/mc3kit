package mc3kit.output;

import mc3kit.*;
import java.util.Map;

import mc3kit.output.DataLogger;

public class NullDataLogger implements DataLogger
{
	@Override
	public void writeData(Map<String, Object> dataObject)
			throws MC3KitException
	{
	}

}
