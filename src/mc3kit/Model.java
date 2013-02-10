package mc3kit;

import mc3kit.graph.*;
import mc3kit.proposal.VariableProposer;

import java.io.*;

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
  
  public Model() {
    graph = new Graph();
  }
  
  public String[] getUnobservedVariableNames() {
    return new String[0];
  }
  
  public VariableProposer<?> makeVariableProposer(String varName) {
    return null;
  }
  
  public <T extends Node> T addNode(T node) throws NodeException {
    graph.addNode(node);
    return node;
  }
  
  public Node get(String name) {
    return graph.getNode(name);
  }
  
  public void setDistribution(String vName, String dName) { 
    
  }
  
  public DoubleVariable getDoubleVariable(String name) {
    Node node = get(name);
    if(node instanceof DoubleVariable)
      return (DoubleVariable)get(name);
    return null;
  }
}
