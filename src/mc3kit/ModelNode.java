package mc3kit;

import mc3kit.graph.Node;

public abstract class ModelNode extends Node {
  Model model;
  
  public ModelNode() { }

  public ModelNode(String name) {
    super(name);
  }
  
  public void recalculate() { }
  
  public Model getModel() {
    return model;
  }
  
  protected ModelEdge updateEdge(ModelEdge edge, ModelNode headNode) throws ModelEdgeException {
    if(edge != null) {
      if(edge.getHead() == headNode) {
        return edge;
      }
      getModel().removeEdge(edge);
    }
    
    if(headNode == null) {
      return null;
    }
    
    edge = new ModelEdge(this, headNode);
    getModel().addEdge(edge);
    return edge;
  }
  
  protected double getDoubleValue(ModelEdge edge) {
    return ((DoubleValued)edge.getHead()).getValue();
  }
}
