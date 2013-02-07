package mc3kit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public final class ErrorLogger
{
	Logger log4jLogger;
	
	public ErrorLogger()
	{
		log4jLogger = null;
	}
	
	public ErrorLogger(Logger log4jLogger)
	{
		this.log4jLogger = log4jLogger;
	}
	
	public boolean isEnabledFor(Priority level)
	{
		if(log4jLogger == null)
			return false;
		
		return log4jLogger.isEnabledFor(level);
	}
	
	public void debug(String format, Object... args)
	{
		if(log4jLogger == null)
			return;
		
		if(log4jLogger.isEnabledFor(Level.DEBUG))
			log4jLogger.debug(String.format(format, args));
	}
	
	public void trace(String format, Object... args)
	{
		if(log4jLogger == null)
			return;

		if(log4jLogger.isEnabledFor(Level.TRACE))
			log4jLogger.trace(String.format(format, args));
	}
	
	public void info(String format, Object... args)
	{
		if(log4jLogger == null)
			return;

		if(log4jLogger.isEnabledFor(Level.INFO))
			log4jLogger.info(String.format(format, args));
	}
	
	public void warn(String format, Object... args)
	{
		if(log4jLogger == null)
			return;

		if(log4jLogger.isEnabledFor(Level.WARN))
			log4jLogger.warn(String.format(format, args));
	}
	
	public void error(String format, Object... args)
	{
		if(log4jLogger == null)
			return;

		if(log4jLogger.isEnabledFor(Level.ERROR))
			log4jLogger.error(String.format(format, args));
	}
	
	public void fatal(String format, Object... args)
	{
		if(log4jLogger == null)
			return;
		
		if(log4jLogger.isEnabledFor(Level.FATAL))
			log4jLogger.fatal(String.format(format, args));
	}
	
	public void log(Priority level, String format, Object... args)
	{
		if(log4jLogger == null)
			return;

		if(log4jLogger.isEnabledFor(level))
			log4jLogger.log(level, String.format(format, args));
	}
}
