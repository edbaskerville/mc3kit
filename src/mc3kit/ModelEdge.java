package mc3kit;

import mc3kit.graph.Edge;
import mc3kit.graph.Node;

public class ModelEdge extends Edge {

  public ModelEdge(Node tail, Node head) {
    super(tail, head);
  }

  public ModelEdge(String name, Node tail, Node head) {
    super(name, tail, head);
  }
}
