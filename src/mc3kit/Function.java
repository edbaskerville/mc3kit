package mc3kit;

public abstract class Function extends ModelNode {

  public Function() {
    this(null);
  }

  public Function(String name) {
    super(name);
  }
  
  @Override
  public abstract boolean update();
}
