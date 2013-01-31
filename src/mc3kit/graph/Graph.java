package mc3kit.graph;

import java.util.*;

/**
 * Represents a directed graph.
 * @author Ed Baskerville
 *
 */
public class Graph {
  private int nextNodeId;
  private int nextEdgeId;
  
  Set<Node> nodes;
  Map<Integer, Node> nodeMap;
  Set<Edge> edges;
  Map<Integer, Edge> edgeMap;

  Map<Node, Set<Edge>> tailNodeMap;
  Map<Node, Set<Edge>> headNodeMap;
  
  public Graph() {
    nodes = new HashSet<Node>();
    nodeMap = new HashMap<Integer, Node>();
    edges = new HashSet<Edge>();
    edgeMap = new HashMap<Integer, Edge>();

    tailNodeMap = new HashMap<Node, Set<Edge>>();
    headNodeMap = new HashMap<Node, Set<Edge>>();
  }
  
  /**
   * Adds a node to the graph.
   * @param node The node to add.
   * @throws NodeException if the node is already in this graph or another graph.
   */
  public Graph addNode(Node node) throws NodeException {
    if(nodes.contains(node)) {
      throw new NodeException(this, node, "Node already in this graph.");
    }
    
    if(node.graph != null) {
      throw new NodeException(this, node, "Node already in another graph.");
    }
    
    int id = getNextNodeId();
    node.id = id;
    node.graph = this;
    nodes.add(node);
    nodeMap.put(id,  node);
    headNodeMap.put(node, new HashSet<Edge>());
    tailNodeMap.put(node, new HashSet<Edge>());
    
    return this;
  }
  
  /**
   * Removes a node from the graph.
   * @param node The node to remove.
   * @throws NodeException If the node is not in the graph.
   */
  public void removeNode(Node node) throws NodeException {
    if(!nodes.contains(node)) {
      throw new NodeException(this, node, "Node not in graph.");
    }
    assert(node.graph == this);
    
    nodes.remove(node);
    nodeMap.remove(node.id);
    node.graph = null;
    nodeMap.remove(node);
    headNodeMap.remove(node);
    tailNodeMap.remove(node);
    
    node.id = -1;
  }
  
  /**
   * Adds an edge to the graph.
   * @param edge The edge to remove.
   * @throws EdgeException If the edge's nodes aren't already in the graph,
   * or if the edge is already in the graph. 
   */
  public Graph addEdge(Edge edge) throws EdgeException {
    if(edges.contains(edge)) {
      throw new EdgeException(this, edge, "Edge already in graph.");
    }
    
    if(edge.graph != null) {
      throw new EdgeException(this, edge, "Edge already in another graph.");
    }
    
    if(edge.tail == null) {
      throw new EdgeException(this, edge, "Edge tail is null.");
    }
    
    if(edge.head == null) {
      throw new EdgeException(this, edge, "Edge head is null.");
    }
    
    if(!nodes.contains(edge.tail)) {
      throw new EdgeException(this, edge, "Edge tail isn't in this graph.");
    }
    
    if(!nodes.contains(edge.head)) {
      throw new EdgeException(this, edge, "Edge head isn't in this graph.");
    }
    
    assert(edge.id == -1);
    
    int id = getNextEdgeId();
    edge.id = id;
    edge.graph = this;
    edges.add(edge);
    edgeMap.put(id, edge);
    tailNodeMap.get(edge.tail).add(edge);
    headNodeMap.get(edge.head).add(edge);
    
    return this;
  }
  
  public void removeEdge(Edge edge) throws EdgeException {
  }
  
  /**
   * Gets a node by id.
   * @param id
   * @return The corresponding node.
   */
  public Node getNode(int id) throws NodeException {
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
  
  private int getNextNodeId() {
    return nextNodeId++;
  }
  
  private int getNextEdgeId() {
    return nextEdgeId++;
  }
  
  public void comprehensiveConsistencyCheck() throws GraphException {
    nodeIdentityCheck();
  }
  
  private void nodeIdentityCheck() throws NodeException {
  }
}
