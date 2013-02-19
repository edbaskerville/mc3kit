package mc3kit.output;

import java.io.FileNotFoundException;

import mc3kit.MC3KitException;

public class DataLoggerFactory {
  private static DataLoggerFactory factory;

  public static synchronized DataLoggerFactory getFactory() {
    if(factory == null) {
      factory = new DataLoggerFactory();
    }
    return factory;
  }

  private DataLoggerFactory() {
  }

  public SampleWriter createDataLogger(String filename) throws MC3KitException, FileNotFoundException {
    return createDataLogger(filename, null, false);
  }

  public SampleWriter createDataLogger(String filename, String format)
      throws MC3KitException, FileNotFoundException {
    return createDataLogger(filename, format, false);
  }

  public SampleWriter createDataLogger(String filename, String format,
      boolean useQuotes) throws FileNotFoundException, MC3KitException {
    if(format == null) {
      String[] filenamePieces = filename.split("\\.");
      if(filenamePieces.length > 1)
        format = filenamePieces[filenamePieces.length - 1];
    }

    if(format.equalsIgnoreCase("jsons")) {
      return new JsonsSampleWriter(filename);
    }
    else if(format.equalsIgnoreCase("csv")) {
      return new CsvSampleWriter(filename, ",", useQuotes);
    }
    else if(format.equalsIgnoreCase("txt")) {
      return new CsvSampleWriter(filename, "\t", useQuotes);
    }
    else {
      throw new MC3KitException(String.format("Unknown format %s.", format));
    }
  }
}
