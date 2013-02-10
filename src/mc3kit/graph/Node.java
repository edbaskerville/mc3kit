package mc3kit.graph;

public class Node {
  int id;
  String name;
  Graph graph;
  
  public Node() {
    this.id = -1;
  }
  
  public Node(String name) {
    this();
    this.name = name;
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
  
  public String getName() {
    return name;
  }
}
