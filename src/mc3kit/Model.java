package mc3kit;

import java.util.*;
import java.io.*;

import static java.lang.String.format;
import static java.lang.Math.*;

import cern.jet.random.engine.RandomEngine;

import mc3kit.graph.*;

/**
 * Represents a directed probabilistic graphical model consisting of random
 * variables, distributions, and (deterministic) functions in a directed
 * acyclic graph.
 * @author Ed Baskerville
 *
 */
@SuppressWarnings("serial")
public class Model implements Observer, Serializable {
  
  Chain chain;
  
  Graph graph;
  
  List<Variable> unobservedVariables;
  Map<Variable, DistributionEdge> varDistEdgeMap;
  
  double logPrior;
  double logLikelihood;
  
  double oldLogPrior;
  double oldLogLikelihood;
  
  State state;
  Set<Variable> changedValueVars;
  Set<ModelNode> newEdgeHeads;
  
  public Model(Chain initialChain) {
    this.chain = initialChain;
    graph = new Graph();
    unobservedVariables = new ArrayList<Variable>();
    varDistEdgeMap = new HashMap<Variable, DistributionEdge>();
    state = State.UNINITIALIZED;
    changedValueVars = new HashSet<Variable>();
    newEdgeHeads = new HashSet<ModelNode>();
  }
  
  public String[] getUnobservedVariableNames() {
    String[] varNames = new String[unobservedVariables.size()];
    for(int i = 0; i < varNames.length; i++) {
      varNames[i] = unobservedVariables.get(i).getName();
    }
    return varNames;
  }
  
  /*** CALCULATIONS ***/
  
  public void beginConstruction() throws ModelException {
    if(state != State.UNINITIALIZED) {
      throw new ModelException("beginConstruction called with wrong state", this);
    }
    
    state = State.IN_CONSTRUCTION;
  }
  
  public void endConstruction() throws ModelException {
    logPrior = 0.0;
    logLikelihood = 0.0;
    
    if(state != State.IN_CONSTRUCTION) {
      throw new ModelException("endConstruction called with wrong state", this);
    }
    
    for(Node node : graph.orderedNodesHeadToTail()) {
      if(node instanceof Variable) {
        Variable var = (Variable)node;
        if(!var.isObserved() && !changedValueVars.contains(var)) {
          var.sample();
        }
      }
      
      ((ModelNode)node).update();
      
      if(node instanceof Variable) {
        Variable var = (Variable)node;
        if(var.isObserved()) {
          logLikelihood += var.getLogP();
        }
        else {
          logPrior += var.getLogP();
        }
      }
    }
    
    changedValueVars.clear();
    newEdgeHeads.clear();
    
    state = State.READY;
  }
  
  public void recalculate() throws MC3KitException {
    oldLogPrior = logPrior;
    oldLogLikelihood = logLikelihood;
    
    logPrior = 0.0;
    logLikelihood = 0.0;
    
    for(Node node : graph.orderedNodesHeadToTail()) {
      ((ModelNode)node).update();
      
      if(node instanceof Variable) {
        Variable var = (Variable)node;
        if(var.isObserved()) {
          logLikelihood += var.getLogP();
        }
        else {
          logPrior += var.getLogP();
        }
      }
    }
    
    double logPriorDiff = abs(oldLogPrior - logPrior);
    double logLikeDiff = abs(oldLogLikelihood - logLikelihood);
    
    if(logPriorDiff > 1e-8 || logLikeDiff > 1e-8) {
      throw new MC3KitException(format("Too much error in prior (%f, should be %f, diff %f) or likelihood (%f, should be %f, diff %f)", 
          oldLogPrior, logPrior, logPriorDiff, oldLogLikelihood, logLikelihood, logLikeDiff)
      );
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
    
    oldLogPrior = logPrior;
    oldLogLikelihood = logLikelihood;
    propagateChanges(true);
    
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
    
    propagateChanges(false);
    logLikelihood = oldLogLikelihood;
    logPrior = oldLogPrior;
    
    state = State.READY;
  }
  
  private void propagateChanges(boolean isProposal) throws ModelException {
    Set<ModelEdge> emptyEdgeSet = new HashSet<ModelEdge>(0);
    
    // Map of nodes to parent edges that have changed, for the sake of efficient updating
    Map<ModelNode, Set<ModelEdge>> visitedEdges = new HashMap<ModelNode, Set<ModelEdge>>();
    
    // Queue of nodes to update in topological order, starting with variables
    // whose values have changed and heads of new edges
    SortedMap<Integer, ModelNode> updateQueue = new TreeMap<Integer, ModelNode>();
    for(Variable var : changedValueVars) {
      updateQueue.put(var.getOrder(), var);
    }
    for(ModelNode node : newEdgeHeads) {
      updateQueue.put(node.getOrder(), node);
    }
    
    // Traverse graph in topological order
    int lastOrder = Integer.MIN_VALUE;
    while (!updateQueue.isEmpty()) {
      int order = updateQueue.firstKey();
      assert order > lastOrder;
      lastOrder = order;
      
      ModelNode node = updateQueue.get(order);
      updateQueue.remove(order);
      
      Set<ModelEdge> fromEdges = visitedEdges.get(node);
      if(fromEdges == null) fromEdges = emptyEdgeSet;
      
      // Get old log-probability for random variables
      double oldLogP = 0.0;
      if(node instanceof Variable) {
        oldLogP = ((Variable)node).getLogP();
      }
      
      // Update the node
      boolean changed;
      if(isProposal) {
        changed = node.update(fromEdges);
      }
      else {
        changed = node.updateAfterRejection(fromEdges);
      }
      
      // Update log-prior or log-likelihood for random variables
      if(node instanceof Variable) {
        Variable var = (Variable)node;
        double newLogP = var.getLogP();
        if(var.isObserved()) {
          logLikelihood += (newLogP - oldLogP);
        }
        else {
          logPrior += (newLogP - oldLogP);
        }
      }
      
      // If the node changed, add all the edges representing dependencies
      // on it (the head) to the visited-edge map for the dependent nodes
      // (the tails)
      if(changed || changedValueVars.contains(node)) {
        for(Edge edge : node.getHeadEdges()) {
          ModelNode tail = (ModelNode)edge.getTail();
          if(!visitedEdges.containsKey(tail)) {
            visitedEdges.put(tail, new HashSet<ModelEdge>());
          }
          visitedEdges.get(tail).add((ModelEdge)edge);
          updateQueue.put(tail.getOrder(), tail);
        }
      }
    }
    
    changedValueVars.clear();
    newEdgeHeads.clear();
  }
  
  /*** GRAPH CONSTRUCTION/MANIPULATION ***/
  
  public <T extends ModelNode> T addNode(T node) {
    if(node instanceof Variable) {
      addVariable((Variable)node);
    }
    else if(node instanceof Function) {
      addFunction((Function)node);
    }
    else if(node instanceof Distribution) {
      addDistribution((Distribution)node);
    }
    else {
      throw new IllegalArgumentException("Unknown node type.");
    }
    return node;
  }
  
  public <V extends Variable> V addVariable(V var) {
    if(var.model != null) {
      throw new IllegalArgumentException("Variable already in model");
    }
    
    graph.addNode(var);
    var.model = this;
    
    if(!var.isObserved()) {
      if(var.getName() == null) {
        throw new IllegalArgumentException("Unobserved random variables must have a name.");
      }
      unobservedVariables.add(var);
    }
    
    var.addObserver(this);
    
    return var;
  }
  
  public <F extends Function> F addFunction(F func) {
    if(func.model != null) {
      throw new IllegalArgumentException("Function already in model.");
    }
    
    graph.addNode(func);
    func.model = this;
    
    return func;
  }
  
  public <D extends Distribution> D addDistribution(D dist) {
    if(dist.model != null) {
      throw new IllegalArgumentException("Distribution already in model.");
    }
    
    graph.addNode(dist);
    dist.model = this;
    
    return dist;
  }
  
  public void addEdge(ModelEdge edge) throws ModelException {
    if(!(state == State.IN_CONSTRUCTION || state == State.IN_PROPOSAL || state == State.IN_REJECTION)) {
      throw new ModelException("Adding edge in wrong state", this);
    }
    
    graph.addEdge(edge);
    newEdgeHeads.add(edge.getHead());
  }
  
  public void removeEdge(ModelEdge edge) throws ModelException {
    if(!(state == State.IN_CONSTRUCTION || state == State.IN_PROPOSAL || state == State.IN_REJECTION)) {
      throw new ModelException("Removing edge in wrong state", this);
    }
    
    graph.removeEdge(edge);
  }
  
  public <V extends Variable, D extends Distribution> void setDistribution(V var, D dist) throws ModelException {
    // Check for existing edge: if unchanged, return; if not, remove the edge
    if(varDistEdgeMap.containsKey(var)) {
      DistributionEdge distEdge = varDistEdgeMap.get(var);
      assert(distEdge.getVariable() == var);
      if(distEdge.getDistribution() == dist) {
        return;
      }
      graph.removeEdge(distEdge);
      varDistEdgeMap.remove(var);
    }
    
    // Create a new edge
    DistributionEdge distEdge = new DistributionEdge(this, var, dist);
    varDistEdgeMap.put(var, distEdge);
  }
  
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
  
  public ModelNode get(String name) {
    return (ModelNode)graph.getNode(name);
  }
  
  public Variable getVariable(String name) {
    return (Variable)graph.getNode(name);
  }
  
  public DoubleVariable getDoubleVariable(String name) {
    return (DoubleVariable)graph.getNode(name);
  }
  
  public DoubleFunction getDoubleFunction(String name) {
    return (DoubleFunction)graph.getNode(name);
  }
  
  public Distribution getDistribution(String name) {
    return (Distribution)graph.getNode(name);
  }
  
  public Distribution getDistributionForVariable(Variable var) {
    DistributionEdge distEdge = varDistEdgeMap.get(var);
    if(distEdge == null) {
      return null;
    }
    return distEdge.getDistribution();
  }
  
  public Distribution getDistributionForVariable(String varName) {
    return getDistributionForVariable(getVariable(varName));
  }
  
  private enum State {
    UNINITIALIZED,
    IN_CONSTRUCTION,
    READY,
    IN_PROPOSAL,
    PROPOSAL_COMPLETE,
    IN_REJECTION
  }

  @Override
  public void update(Observable obj, Object arg1) {
    if(obj instanceof Variable) {
      Variable var = (Variable)obj;
      assert state == State.IN_CONSTRUCTION || state == State.IN_PROPOSAL || state == State.IN_REJECTION;
      if(state == State.IN_PROPOSAL || state == State.IN_REJECTION) {
        assert !var.isObserved();
      }
      
      if(!var.isObserved()) {
        changedValueVars.add(var);
      }
    }
  }
  
  public void setChain(Chain chain) {
    this.chain = chain;
  }
  
  public Chain getChain() {
    return chain;
  }
  
  public RandomEngine getRng() {
    return chain.getRng();
  }
}
