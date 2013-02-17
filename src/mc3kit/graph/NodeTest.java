package mc3kit.graph;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NodeTest {
  
  Node node;
  
  @Before
  public void setUp() throws Exception {
    node = new Node();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void notInGraph() {
    Node node = new Node();
    
    assertTrue(node.graph == null);
  }
  
  @Test
  public void addToGraph() throws Exception {
    Graph graph = new Graph();
    
    graph.addNode(node);
    assertTrue(node.getGraph() == graph);
  }
  
  @Test
  public void removeFromGraph() throws Exception {
    Graph graph = new Graph();
    
    graph.addNode(node);
    graph.removeNode(node);
    
    assertTrue(node.graph == null);
  }
  
  @Test
  public void alreadyInGraph() throws Exception {
    Graph graph = new Graph();
    graph.addNode(node);
    
    try {
      Graph graph2 = new Graph();
      graph2.addNode(node);
      fail("Should not have been able to add to second graph.");
    }
    catch(Exception e) {
    }
  }
}
