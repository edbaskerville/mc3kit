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
  Map<Variable<?>, DistributionEdge> varDistEdgeMap;
  
  double logPrior;
  double logLikelihood;
  
  State state;
  
  public Model() {
    graph = new Graph();
    unobservedVariables = new ArrayList<Variable<?>>();
    varDistEdgeMap = new HashMap<Variable<?>, DistributionEdge>();
    state = State.READY;
  }
  
  public String[] getUnobservedVariableNames() {
    String[] varNames = new String[unobservedVariables.size()];
    for(int i = 0; i < varNames.length; i++) {
      varNames[i] = unobservedVariables.get(i).getName();
    }
    return varNames;
  }
  
  /*** CALCULATIONS ***/
  
  public void recalculate() throws ModelException {
    logPrior = 0.0;
    logLikelihood = 0.0;
    
    for(Node node : graph.orderedNodesHeadToTail()) {
      ((ModelNode)node).recalculate();
      
      if(node instanceof Variable) {
        Variable<?> var = (Variable<?>)node;
        if(var.isObserved()) {
          logLikelihood += var.getLogP();
        }
        else {
          logPrior += var.getLogP();
        }
      }
    }
  }
  
  public void beginProposal() throws ModelException {
    if(state != State.READY) {
      throw new ModelException("beginProposal called with wrong state", this);
    }
    
    state = State.IN_PROPOSAL;
  }
  
  public void endProposal() throws ModelException {
    if(state != State.IN_PROPOSAL) {
      throw new ModelException("endProposal called with wrong state", this);
    }
    
    state = State.PROPOSAL_COMPLETE;
  }
  
  public void acceptProposal() throws ModelException {
    if(state != State.PROPOSAL_COMPLETE) {
      throw new ModelException("acceptProposal called with wrong state", this);
    }
    state = State.READY;
  }
  
  public void beginRejection() throws ModelException {
    if(state != State.PROPOSAL_COMPLETE) {
      throw new ModelException("beginRejection called with wrong state", this);
    }
    
    state = State.IN_REJECTION;
  }
  
  public void endRejection() throws ModelException {
    if(state != State.IN_REJECTION) {
      throw new ModelException("endRejection called with wrong state", this);
    }
    
    state = State.READY;
  }
  
  /*** GRAPH CONSTRUCTION/MANIPULATION ***/
  
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
  
  public void addEdge(ModelEdge edge) throws ModelEdgeException {
    try {
      graph.addEdge(edge);
    }
    catch(EdgeException e) {
      throw new ModelEdgeException("Exception thrown from underlying graph", this, edge, e);
    }
  }
  
  public void removeEdge(ModelEdge edge) throws ModelEdgeException {
    try {
      graph.removeEdge(edge);
    }
    catch(EdgeException e) {
      throw new ModelEdgeException("Exception thrown from underlying graph", this, edge, e);
    }
  }
  
  public <V extends Variable<D>, D extends Distribution<V>> void setDistribution(V var, D dist) throws ModelException {
    // Check for existing edge: if unchanged, return; if not, remove the edge
    if(varDistEdgeMap.containsKey(var)) {
      DistributionEdge distEdge = varDistEdgeMap.get(var);
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
      varDistEdgeMap.remove(var);
    }
    
    // Create a new edge
    DistributionEdge distEdge = new DistributionEdge(var, dist);
    try {
      graph.addEdge(distEdge);
    }
    catch(EdgeException e) {
      throw new ModelEdgeException("Exception thrown from underlying graph", this, distEdge, e);
    }
    varDistEdgeMap.put(var, distEdge);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setDistribution(String vName, String dName) throws ModelException { 
    Variable var = getVariable(vName);
    Distribution dist = getDistribution(dName);
    setDistribution(var, dist);
  }
  
  /*** GETTERS ***/
  
  public double getLogPrior() {
    return logPrior;
  }
  
  public double getLogLikelihood() {
    return logLikelihood;
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
  
  public Distribution<?> getDistributionForVariable(Variable<?> var) {
    DistributionEdge distEdge = varDistEdgeMap.get(var);
    if(distEdge == null) {
      return null;
    }
    return distEdge.getDistribution();
  }
  
  public Distribution<?> getDistributionForVariable(String varName) {
    return getDistributionForVariable(getVariable(varName));
  }
  
  private enum State {
    READY,
    IN_PROPOSAL,
    PROPOSAL_COMPLETE,
    IN_REJECTION
  }
}
