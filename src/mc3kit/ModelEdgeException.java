package mc3kit;

@SuppressWarnings("serial")
public class ModelEdgeException extends ModelException {
  
  ModelEdge edge;

  public ModelEdgeException(String msg, Model model, ModelEdge edge) {
    this(msg, model, edge, null);
  }

  public ModelEdgeException(String msg, Model model, ModelEdge edge, Throwable cause) {
    super(msg, model, cause);
    this.edge = edge;
  }
}
