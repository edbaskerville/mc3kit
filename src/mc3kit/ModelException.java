package mc3kit;

@SuppressWarnings("serial")
public class ModelException extends MC3KitException {

  Model model;
  
  public ModelException(String msg, Model model) {
    this(msg, model, null);
  }

  public ModelException(String msg, Model model, Throwable cause) {
    super(msg, cause);
    this.model = model;
  }
}
