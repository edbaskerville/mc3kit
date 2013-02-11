package mc3kit;

@SuppressWarnings("serial")
public class ModelNodeException extends ModelException {
  
  ModelNode node;

  public ModelNodeException(String msg, Model model, ModelNode node) {
    this(msg, model, node, null);
  }

  public ModelNodeException(String msg, Model model, ModelNode node, Throwable cause) {
    super(msg, model, cause);
    this.node = node;
  }
}
