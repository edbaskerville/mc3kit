package mc3kit;

public abstract class BinaryDistribution extends Distribution {

  public BinaryDistribution(Model model) {
    this(model, null);
  }
  
  public BinaryDistribution(Model model, String name) {
    super(model, name);
  }
}
