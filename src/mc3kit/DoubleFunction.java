package mc3kit;

public abstract class DoubleFunction extends Function implements DoubleValued {
  private double value;
  
  public DoubleFunction() {
    this(null);
  }

  public DoubleFunction(String name) {
    super(name);
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
