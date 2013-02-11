package mc3kit.graph;

import java.util.Observable;

public class Node extends Observable {
  String name;
  Graph graph;
  
  public Node() {
  }
  
  public Node(String name) {
    this();
    this.name = name;
  }
  
  public Graph getGraph() {
    return graph;
  }
  
  public String getName() {
    return name;
  }
}
