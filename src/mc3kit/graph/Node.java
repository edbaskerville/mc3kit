package mc3kit.graph;

import java.util.*;

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
  
  public int getOrder() {
    return graph.getOrder(this);
  }
  
  public Set<Edge> getHeadEdges() {
    return graph.getHeadEdges(this);
  }
  
  public Set<Edge> getTailEdges() {
    return graph.getTailEdges(this);
  }
  
  @Override
  public String toString() {
    return name == null ? super.toString() : name;
  }
}
