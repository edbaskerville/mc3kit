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
    if(this.value != value) {
      this.value = value;
      setChanged();
      notifyObservers();
    }
  }
  
  public boolean valueIsValid(double value) throws ModelNodeException {
    DoubleDistribution dist = getDistribution();
    if(dist == null) {
      throw new ModelNodeException("Can't ask whether value is valid without distribution.", getModel(), this);
    }
    
    return dist.valueIsValid(value);
  }

  @Override
  public boolean update() {
    DoubleDistribution dist = getDistribution();
    setLogP(dist.getLogP(this));
    return false;
  }
}
