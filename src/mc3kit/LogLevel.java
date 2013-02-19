package mc3kit;

import java.util.logging.Level;

public enum LogLevel {
  ALL(Level.ALL),
  OFF(Level.OFF),
  FINEST(Level.FINEST),
  FINER(Level.FINER),
  FINE(Level.FINE),
  INFO(Level.INFO),
  SEVERE(Level.SEVERE);

  Level level;

  LogLevel(Level level) {
    this.level = level;
  }

  public Level getLevel() {
    return level;
  }
}
