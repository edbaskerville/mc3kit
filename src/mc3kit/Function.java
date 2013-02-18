package mc3kit;

public abstract class Function extends ModelNode {

  public Function(Model model) {
    this(model, null);
  }

  public Function(Model model, String name) {
    super(name);
    if(model != null) {
      model.addFunction(this);
    }
  }
  
  @Override
  public abstract boolean update();
}
