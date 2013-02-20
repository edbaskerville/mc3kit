package mc3kit;

public class BinaryVariable extends Variable implements BinaryValued {
  
  private boolean value;
  
  public BinaryVariable(Model model, boolean value) {
    this(model, (String)null, value);
  }
  
  public BinaryVariable(Model model, String name) {
    super(model, name, false);
  }
  
  public BinaryVariable(Model model, String name, boolean value) {
    super(model, name, true);
    this.value = value;
  }
  
  public BinaryVariable(Model model, boolean value, BinaryDistribution dist) throws ModelException {
    this(model, null, value, dist);
  }
  
  public BinaryVariable(Model model, String name, boolean value, BinaryDistribution dist) throws ModelException {
    super(model, name, true, dist);
    this.value = value;
  }
  
  public BinaryVariable(Model model, String name, BinaryDistribution dist) throws ModelException {
    super(model, name, false, dist);
  }

  @Override
  public boolean getValue() {
    return value;
  }

  @Override
  public void setValue(boolean value) {
    if(isObserved()) {
      throw new UnsupportedOperationException("Can't set value on an observed variable.");
    }
    
    this.value = value;
    setChanged();
    notifyObservers();
  }
  
  @Override
  public BinaryVariable setDistribution(Distribution dist) throws ModelException {
    super.setDistribution(dist);
    return this;
  }

  @Override
  public boolean update() {
    BinaryDistribution dist = (BinaryDistribution)getDistribution();
    setLogP(dist.getLogP(this));
    return false;
  }

  @Override
  public Object makeOutputObject() {
    return value ? 1 : 0;
  }
  
  @Override
  public String makeOutputString() {
    return value ? "1" : "0";
  }
}
