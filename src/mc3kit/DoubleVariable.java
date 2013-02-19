package mc3kit;

public class DoubleVariable extends Variable implements DoubleValued {
  
  private double value;
  
  /**
   * Constructor for an <i>observed</i> double-valued variable.
   * @param model
   * @param value
   */
  public DoubleVariable(Model model, double value) {
    this(model, (String)null, value);
  }
  
  /**
   * Constructor for an <i>unobserved</i> double-valued variable.
   * @param model
   * @param name
   */
  public DoubleVariable(Model model, String name) {
    super(model, name, false);
  }
  
  /**
   * Constructor for an <i>observed</i> double-valued variable with a name.
   * @param model
   * @param name
   * @param value
   */
  public DoubleVariable(Model model, String name, double value) {
    super(model, name, true);
    this.value = value;
  }
  
  /**
   * Constructor for an <i>observed</i> double-valued variable with a distribution.
   * @param model
   * @param name
   * @param value
   * @throws ModelException 
   */
  public DoubleVariable(Model model, double value, Distribution dist) throws ModelException {
    this(model, null, value, dist);
  }
  
  /**
   * Constructor for an <i>observed</i> double-valued variable with a name and distribution.
   * @param model
   * @param name
   * @param value
   * @param dist
   * @throws ModelException
   */
  public DoubleVariable(Model model, String name, double value, Distribution dist) throws ModelException {
    super(model, name, true, dist);
    this.value = value;
  }
  
  /**
   * Constructor for an <i>unobserved</i> double-valued variable with a distribution.
   * @param model
   * @param name
   * @param dist
   * @throws ModelException
   */
  public DoubleVariable(Model model, String name, Distribution dist) throws ModelException {
    super(model, name, false, dist);
  }

  @Override
  public double getValue() {
    return value;
  }

  @Override
  public void setValue(double value) {
    if(isObserved()) {
      throw new UnsupportedOperationException("Can't set value on an observed variable.");
    }
    
    if(this.value != value) {
      this.value = value;
      setChanged();
      notifyObservers();
    }
  }
  
  @Override
  public DoubleVariable setDistribution(Distribution dist) throws ModelException {
    super.setDistribution(dist);
    return this;
  }
  
  public boolean valueIsValid(double value) throws MC3KitException {
    DoubleDistribution dist = (DoubleDistribution)getDistribution();
    if(dist == null) {
      throw new MC3KitException("Can't ask whether value is valid without distribution.");
    }
    
    return dist.valueIsValid(value);
  }

  @Override
  public boolean update() {
    DoubleDistribution dist = (DoubleDistribution)getDistribution();
    setLogP(dist.getLogP(this));
    return false;
  }

  @Override
  public Object makeOutputObject() {
    return value;
  }
  
  @Override
  public String makeOutputString() {
    return Double.toString(value);
  }
}
