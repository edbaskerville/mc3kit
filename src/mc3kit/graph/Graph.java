package mc3kit.graph;

import java.util.*;

/**
 * Represents a directed acyclic graph.
 * @author Ed Baskerville
 *
 */
public class Graph {
  private int nextId;
  
  Set<Node> nodes;
  Map<Integer, Node> nodeMap;
  Set<Edge<?,?>> edges;
  Map<Integer, Edge<?,?>> edgeMap;
  
  Map<Node, Set<Edge<?,?>>> headNodeMap;
  Map<Node, Set<Edge<?,?>>> tailNodeMap;
  
  public Graph() {
    nodes = new HashSet<Node>();
    nodeMap = new HashMap<Integer, Node>();
    edges = new HashSet<Edge<?,?>>();
    edgeMap = new HashMap<Integer, Edge<?,?>>();
  }
  
  public void addNode(Node node) throws NodeException {
    if(nodes.contains(node)) {
      throw new NodeException(this, node, "Node already exists.");
    }
    
    if(node.getGraph() != null) {
      throw new NodeException(this, node, "Node already has graph.");
    }
    
    int id = getNextId();
    node.id = id;
    node.graph = this;
    nodes.add(node);
    nodeMap.put(id,  node);
    headNodeMap.put(node, new HashSet<Edge<?,?>>());
    tailNodeMap.put(node, new HashSet<Edge<?,?>>());
  }
  
  public void removeNode(Node node) throws NodeException {
    if(!nodes.contains(node)) {
      throw new NodeException(this, node, "Node not in graph.");
    }
  }
  
  public void addEdge(Edge edge) throws GraphException {
  }
  
  public Node getNode(int id) {
    return nodeMap.get(id);
  }
  
  public Edge getEdge(int id) {
    return edgeMap.get(id);
  }
  
  public int nodeCount() {
    return nodes.size();
  }
  
  public int edgeCount() {
    return edges.size();
  }
  
  private int getNextId() {
    return nextId++;
  }
}
