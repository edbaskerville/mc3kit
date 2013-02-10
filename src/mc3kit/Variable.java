package mc3kit;

import mc3kit.graph.Node;

public class Variable<D extends Distribution<?>> extends Node {
  public Variable() {
  }
  
  public Variable(String name) {
    super(name);
  }
}
