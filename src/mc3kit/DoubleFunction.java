package mc3kit;

public abstract class DoubleFunction extends Function implements DoubleValued {
  private double value;
  
  public DoubleFunction(Model model) {
    this(model, null);
  }

  public DoubleFunction(Model model, String name) {
    super(model, name);
  }

  @Override
  public double getValue() {
    return value;
  }

  @Override
  public void setValue(double value) {
    this.value = value;
  }
}
