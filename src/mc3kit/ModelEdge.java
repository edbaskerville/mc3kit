package mc3kit;

import mc3kit.graph.Edge;

public class ModelEdge extends Edge {

  public ModelEdge(ModelNode tail, ModelNode head) {
    super(tail, head);
  }

  public ModelEdge(String name, ModelNode tail, ModelNode head) {
    super(name, tail, head);
  }
}
