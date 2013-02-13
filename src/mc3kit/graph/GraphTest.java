/**
 * 
 */
package mc3kit.graph;

import org.junit.*;
import static org.junit.Assert.*;

public class GraphTest {

  Graph graph;
  
  @Before
  public void setUp() throws Exception {
    graph = new Graph();
  }

  public void tearDown() throws Exception {
  }

  @Test
  public void addNodeSingle() throws Exception {
    Node node = new Node("node");
    
    graph.addNode(node);
    
    assertEquals(node, graph.getNode("node"));
    assertEquals(1, graph.nodeCount());
    assertEquals(1, graph.nodeNameMap.size());
  }
  
  @Test
  public void removeNodeSingle() throws Exception {
    Node node = new Node();
    graph.addNode(node);
    graph.removeNode(node);
    
    assertEquals(0, graph.nodes.size());
    assertEquals(0, graph.nodeNameMap.size());
    assertEquals(0, graph.headNodeMap.size());
    assertEquals(0, graph.tailNodeMap.size());
  }
  
  @Test
  public void addEdgeValid() throws Exception {
    Node node = new Node();
    Node node2 = new Node();
    Edge edge = new Edge(node, node2);
    
    graph
      .addNode(node)
      .addNode(node2)
      .addEdge(edge);
    
    assert(edge.graph == graph);
    assertEquals(2, graph.nodes.size());
    assertEquals(1, graph.tailNodeMap.get(node).size());
    assertEquals(1, graph.headNodeMap.get(node2).size());
  }
  
  @Test
  public void addEdgeNodesNotPresent() throws Exception {
    Node node = new Node();
    Node node2 = new Node();
    Edge edge = new Edge(node, node2);
    
    try {
      graph.addEdge(edge);
      fail("Should have been impossible to add edge.");
    }
    catch(EdgeException e) {
    }
  }
  
  @Test
  public void addEdgeInOtherGraph() throws Exception {
    Graph otherGraph = new Graph();
    Node node = new Node();
    Node node2 = new Node();
    Edge edge = new Edge(node, node2);
    otherGraph
      .addNode(node)
      .addNode(node2)
      .addEdge(edge);
    
    try {
      graph.addEdge(edge);
      fail("Should have been impossible to add edge.");
    }
    catch(EdgeException e) {
    }
  }
  
  @Test
  public void simpleOrder() throws Exception {
    // Ordering should start with node2 < node
    Node node = new Node("node");
    Node node2 = new Node("node2");
    graph.addNode(node).addNode(node2);
    assertTrue(node2.getOrder() < node.getOrder());
    
    // Adding edge node2 > node should reverse order
    Edge edge = new Edge(node2, node);
    graph.addEdge(edge);
    assertTrue(node.getOrder() < node2.getOrder());
  }
  
  @Test
  public void edgeRemovalAddition() throws Exception {
    // Ordering should start with node2 < node
    Node node = new Node("node");
    Node node2 = new Node("node2");
    graph.addNode(node).addNode(node2);
    assertTrue(node2.getOrder() < node.getOrder());
    
    // Adding edge node2 > node should reverse order
    Edge edge = new Edge(node2, node);
    graph.addEdge(edge);
    assertTrue(node.getOrder() < node2.getOrder());
    
    // Removing and adding the reverse edge should reverse back
    graph.removeEdge(edge);
    edge = new Edge(node, node2);
    graph.addEdge(edge);
    assertTrue(node2.getOrder() < node.getOrder());
  }
  
  @Test
  public void cycle() throws Exception {
    Node node = new Node("node");
    Node node2 = new Node("node2");
    Node node3 = new Node("node3");
    graph.addNode(node).addNode(node2).addNode(node3);
    
    graph.addEdge(new Edge(node, node2));
    graph.addEdge(new Edge(node2, node3));
    
    try {
      graph.addEdge(new Edge(node3, node));
      fail();
    }
    catch(EdgeException e) {
      e.printStackTrace();
    }
  }
}
