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
  Map<Integer, Node> nodeIdMap;
  Map<String, Node> nodeNameMap;
  Set<Edge> edges;
  Map<Integer, Edge> edgeIdMap;
  Map<String, Edge> edgeNameMap;

  Map<Node, Set<Edge>> tailNodeMap;
  Map<Node, Set<Edge>> headNodeMap;
  
  public Graph() {
    nodes = new HashSet<Node>();
    nodeIdMap = new HashMap<Integer, Node>();
    nodeNameMap = new HashMap<String, Node>();
    
    edges = new HashSet<Edge>();
    edgeIdMap = new HashMap<Integer, Edge>();
    edgeNameMap = new HashMap<String, Edge>();

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
    
    if(node.name != null && nodeNameMap.containsKey(node.name)) {
      throw new NodeException(this, node, "Node with this name already in graph.");
    }
    
    int id = getNextNodeId();
    node.id = id;
    node.graph = this;
    nodes.add(node);
    nodeIdMap.put(id,  node);
    if(node.name != null) {
      nodeNameMap.put(node.name, node);
    }
    tailNodeMap.put(node, new HashSet<Edge>());
    headNodeMap.put(node, new HashSet<Edge>());
    
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
    
    if(!(tailNodeMap.get(node).isEmpty() && headNodeMap.get(node).isEmpty())) {
      throw new NodeException(this, node, "Node has edges in graph.");
    }
    
    nodes.remove(node);
    nodeIdMap.remove(node.id);
    node.graph = null;
    nodeIdMap.remove(node);
    if(node.name != null) {
      nodeNameMap.remove(node.name);
    }
    tailNodeMap.remove(node);
    headNodeMap.remove(node);
    
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
    
    if(edge.name != null && edgeNameMap.containsKey(edge.name)) {
      throw new EdgeException(this, edge, "Edge with this name already in graph.");
    }
    
    assert(edge.id == -1);
    
    int id = getNextEdgeId();
    edge.id = id;
    edge.graph = this;
    edges.add(edge);
    edgeIdMap.put(id, edge);
    if(edge.name != null) {
      edgeNameMap.put(edge.name, edge);
    }
    
    tailNodeMap.get(edge.tail).add(edge);
    headNodeMap.get(edge.head).add(edge);
    
    return this;
  }
  
  public void removeEdge(Edge edge) throws EdgeException {
    if(!edges.contains(edge)) {
      throw new EdgeException(this, edge, "Edge not in graph.");
    }
    assert(edge.graph == this);
    
    edges.remove(edge);
    edgeIdMap.remove(edge.id);
    edge.graph = null;
    edgeIdMap.remove(edge);
    if(edge.name != null) {
      edgeNameMap.remove(edge.name);
    }
    tailNodeMap.get(edge.tail).remove(edge);
    headNodeMap.get(edge.head).remove(edge);
    
    edge.id = -1;
  }
  
  /**
   * Gets a node by id.
   * @param id
   * @return The corresponding node, or null if it does not exist.
   */
  public Node getNode(int id) {
    return nodeIdMap.get(id);
  }
  
  /**
   * Gets a node by name.
   * @param name
   * @return The corresponding node, or null if it does not exist.
   */
  public Node getNode(String name) {
    return nodeNameMap.get(name);
  }
  
  /**
   * Gets an edge by id
   * @param id
   * @return The corresponding edge, or null if it does not exist.
   */
  public Edge getEdge(int id) {
    return edgeIdMap.get(id);
  }
  
  /**
   * Gets an edge by name
   * @param name
   * @return The corresponding edge, or null if it does not exist.
   */
  public Edge getEdge(String name) {
    return edgeNameMap.get(name);
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
