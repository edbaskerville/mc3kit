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
  
  public Model() {
  }
  
  public String[] getUnobservedVariableNames() {
    return new String[0];
  }
  
  public VariableProposer<?> makeVariableProposer(String varName) {
    return null;
  }
  
  public <T extends Node> T add(String name, T node) {
    return null;
  }
  
  public Node get(String name) {
    return null;
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
