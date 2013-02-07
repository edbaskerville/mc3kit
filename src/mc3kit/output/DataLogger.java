package mc3kit.output;

import java.util.Map;

import mc3kit.MC3KitException;

public interface DataLogger
{
	public void writeData(Map<String, Object> dataObject) throws MC3KitException;
}
