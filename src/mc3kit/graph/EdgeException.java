package mc3kit.graph;

@SuppressWarnings("serial")
public class EdgeException extends GraphException {
  
  public Edge edge;
  
  public EdgeException(Graph graph, Edge edge, String message, Throwable cause) {
    super(graph, message, cause);
    this.edge = edge;
  }

  public EdgeException(Graph graph, Edge edge, String message) {
    this(graph, edge, message, null);
  }
}
