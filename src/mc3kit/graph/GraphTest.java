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
    Node node = new Node();
    
    graph.addNode(node);
    
    assertEquals(node, graph.getNode(0));
    assertEquals(1, graph.nodeCount());
    assertEquals(1, graph.nodeMap.size());
  }
  
  @Test
  public void removeNodeSingle() throws Exception {
    Node node = new Node();
    graph.addNode(node);
    graph.removeNode(node);
    
    assertEquals(0, graph.nodes.size());
    assertEquals(0, graph.nodeMap.size());
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
    assert(edge.id == 0);
    assertEquals(2, graph.nodes.size());
    assertEquals(1, graph.edgeMap.size());
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
}
