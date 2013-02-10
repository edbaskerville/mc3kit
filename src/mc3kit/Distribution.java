package mc3kit;

import mc3kit.graph.*;

public class Distribution<V extends Variable<?>> extends Node {

  public Distribution() {
    this(null);
  }
  
  public Distribution(String name) {
    super(name);
  }
}
