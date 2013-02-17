package mc3kit;

import java.util.*;

import cern.jet.random.engine.RandomEngine;
import mc3kit.graph.*;

public abstract class ModelNode extends Node {
  Model model;
  
  public ModelNode() { }

  public ModelNode(String name) {
    super(name);
  }
  
  public boolean update() {
    return true;
  }
  
  public boolean update(Set<ModelEdge> fromEdges) {
    return update();
  }
  
  public boolean updateAfterRejection() {
    return update();
  }
  
  public boolean updateAfterRejection(Set<ModelEdge> fromEdges) {
    return update(fromEdges);
  }
  
  public Model getModel() {
    return model;
  }
  
  protected ModelEdge updateEdge(ModelEdge edge, ModelNode headNode) throws ModelException {
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
  
  public Chain getChain() {
    return model.getChain();
  }
  
  public RandomEngine getRng() {
    return getChain().getRng();
  }
}
