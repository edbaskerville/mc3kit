package mc3kit;

public class DoubleVariable extends Variable<DoubleDistribution> implements DoubleValued {

  public DoubleVariable() {
    this(null);
  }
  
  public DoubleVariable(double value) {
    this(null, value);
  }
  
  public DoubleVariable(String name) {
    super(name);
  }
  
  public DoubleVariable(String name, double value) {
    super(name);
  }

  public void setDistribution(DoubleDistribution dist) {
  }

  @Override
  public double getValue() {
    return 0;
  }

  @Override
  public void setValue(double value) {
  }
}
