/**
 * 
 */
package mc3kit.graph;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GraphTest {

  Graph graph;
  
  @Before
  public void setUp() throws Exception {
    graph = new Graph();
  }

  public void tearDown() throws Exception {
  }

  @Test
  public void testAddNode() throws Exception {
    Node node = new Node();
    
    graph.addNode(node);
    
    assertEquals(node, graph.getNode(0));
    assertEquals(1, graph.nodeCount());
    assertEquals(1, graph.nodeMap.size());
  }
}
