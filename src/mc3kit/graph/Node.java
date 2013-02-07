package mc3kit.graph;

public class Node {
  int id;
  int order;
  Graph graph;
  
  public Node() {
    this.id = -1;
    this.order = -1;
  }
  
  public Graph getGraph() {
    return graph;
  }
  
  public int getId() throws NodeException {
    if(id == -1) {
      throw new NodeException(null, this, "No id assigned.");
    }
    return id;
  }
}
