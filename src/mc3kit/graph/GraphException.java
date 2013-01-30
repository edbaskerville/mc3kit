package mc3kit.graph;

@SuppressWarnings("serial")
public class GraphException extends Exception {
  
  Graph graph;

  public GraphException(Graph graph, String message, Throwable cause) {
    super(message, cause);
    this.graph = graph;
  }

  public GraphException(Graph graph, String message) {
    this(graph, message, null);
  }
}
