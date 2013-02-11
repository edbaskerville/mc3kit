package mc3kit;

public class DoubleVariable extends Variable<DoubleDistribution> implements DoubleValued {
  
  private double value;
  
  public DoubleVariable() {
    this(null);
  }
  
  public DoubleVariable(double value) {
    this(null, value);
  }
  
  public DoubleVariable(String name) {
    super(name);
    setObserved(false);
  }
  
  public DoubleVariable(String name, double value) {
    super(name);
    setObserved(true);
    setValue(value);
  }

  @Override
  public double getValue() {
    return value;
  }

  @Override
  public void setValue(double value) {
    this.value = value;
    setChanged();
    notifyObservers();
  }
}
