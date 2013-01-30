package mc3kit.graph;

@SuppressWarnings("serial")
public class NodeException extends GraphException {
  
  public Node node;
  
  public NodeException(Graph graph, Node node, String message, Throwable cause) {
    super(graph, message, cause);
    this.node = node;
  }

  public NodeException(Graph graph, Node node, String message) {
    this(graph, node, message, null);
  }
}
