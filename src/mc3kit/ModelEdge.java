package mc3kit;

import mc3kit.graph.Edge;

public class ModelEdge extends Edge {
  Model model;
  
  public ModelEdge(Model model, ModelNode tail, ModelNode head) throws ModelException {
    super(tail, head);
    model.addEdge(this);
  }

  public ModelEdge(Model model, String name, ModelNode tail, ModelNode head) throws ModelException {
    super(name, tail, head);
    model.addEdge(this);
  }
  
  public ModelNode getTail() {
    return (ModelNode)super.getTail();
  }
  
  public ModelNode getHead() {
    return (ModelNode)super.getHead();
  }
}
