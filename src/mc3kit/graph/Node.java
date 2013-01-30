package mc3kit.graph;

public class Node {
  int id;
  Graph graph;
  
  public Node() {
    this.id = -1;
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
