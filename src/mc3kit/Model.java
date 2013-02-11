package mc3kit;

import java.util.*;
import java.io.*;

import mc3kit.graph.*;

/**
 * Represents a directed probabilistic graphical model consisting of random
 * variables, distributions, and (deterministic) functions in a directed
 * acyclic graph.
 * @author Ed Baskerville
 *
 */
@SuppressWarnings("serial")
public class Model implements Serializable {
  
  Graph graph;
  
  List<Variable<?>> unobservedVariables;
  Map<String, DistributionEdge<?,?>> varDistEdgeMap;
  
  public Model() {
    graph = new Graph();
    unobservedVariables = new ArrayList<Variable<?>>();
    varDistEdgeMap = new HashMap<String, DistributionEdge<?,?>>();
  }
  
  public String[] getUnobservedVariableNames() {
    String[] varNames = new String[unobservedVariables.size()];
    for(int i = 0; i < varNames.length; i++) {
      varNames[i] = unobservedVariables.get(i).getName();
    }
    return varNames;
  }
  
  public <V extends Variable<?>> V addVariable(V var) throws ModelNodeException {
    if(var.model != null) {
      throw new ModelNodeException("Variable already in model", this, var);
    }
    
    try {
      graph.addNode(var);
    }
    catch(NodeException e) {
      throw new ModelNodeException("Exception thrown from underlying graph", this, var, e);
    }
    var.model = this;
    
    if(!var.isObserved()) {
      if(var.getName() == null) {
        throw new ModelNodeException("Unobserved random variables must have a name.", this, var);
      }
      unobservedVariables.add(var);
    }
    
    return var;
  }
  
  public <D extends Distribution<?>> D addDistribution(D dist) throws ModelNodeException {
    if(dist.model != null) {
      throw new ModelNodeException("Distribution already in model.", this, dist);
    }
    
    try {
      graph.addNode(dist);
    }
    catch(NodeException e) {
      throw new ModelNodeException("Exception thrown from underlying graph", this, dist, e);
    }
    dist.model = this;
    
    return dist;
  }
  
  @SuppressWarnings("unchecked")
  public <V extends Variable<D>, D extends Distribution<V>> void setDistribution(V var, D dist) throws ModelException {
    String vName = var.getName();
    
    // Check for existing edge: if unchanged, return; if not, remove the edge
    if(varDistEdgeMap.containsKey(vName)) {
      DistributionEdge<V, D> distEdge = (DistributionEdge<V, D>)varDistEdgeMap.get(vName);
      assert(distEdge.getVariable() == var);
      if(distEdge.getDistribution() == dist) {
        return;
      }
      try {
        graph.removeEdge(distEdge);
      }
      catch(EdgeException e) {
        throw new ModelEdgeException("Exception thrown from underlying graph ", this, distEdge, e);
      }
      varDistEdgeMap.remove(vName);
    }
    
    // Create a new edge
    DistributionEdge<V, D> distEdge = new DistributionEdge<V, D>(var, dist);
    try {
      graph.addEdge(distEdge);
    }
    catch(EdgeException e) {
      throw new ModelEdgeException("Exception thrown from underlying graph", this, distEdge, e);
    }
    varDistEdgeMap.put(vName, distEdge);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setDistribution(String vName, String dName) throws ModelException { 
    Variable var = getVariable(vName);
    Distribution dist = getDistribution(dName);
    setDistribution(var, dist);
  }
  
  public Variable<?> getVariable(String name) {
    return (Variable<?>)graph.getNode(name);
  }
  
  public DoubleVariable getDoubleVariable(String name) {
    return (DoubleVariable)graph.getNode(name);
  }
  
  public Distribution<?> getDistribution(String name) {
    return (Distribution<?>)graph.getNode(name);
  }
  
  public Distribution<?> getDistributionForVariable(String varName) {
    DistributionEdge<?,?> distEdge = varDistEdgeMap.get(varName);
    if(distEdge == null) {
      return null;
    }
    return distEdge.getDistribution();
  }
}
