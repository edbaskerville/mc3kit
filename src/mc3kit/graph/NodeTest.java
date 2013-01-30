package mc3kit.graph;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NodeTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testNotInGraph() {
    Node node = new Node();
    
    try {
      node.getId();
      fail("getId() not throwing exception");
    }
    catch(NodeException e) {
      assertTrue(e.graph == null);
      assertTrue(e.node == node);
    }
  }
  
  @Test
  public void testAddToGraph() throws Exception {
    Graph graph = new Graph();
    
    Node node = new Node();
    graph.addNode(node);
    assertTrue(node.getId() == 0);
    assertTrue(node.getGraph() == graph);
  }
  
  @Test
  public void testRemoveFromGraph() throws Exception {
    Graph graph = new Graph();
    
    Node node = new Node();
    graph.addNode(node);
    graph.removeNode(node);
  }
}
