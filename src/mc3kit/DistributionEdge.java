package mc3kit;

public class DistributionEdge<V extends Variable<D>, D extends Distribution<V>> extends ModelEdge {

  public DistributionEdge(V var, D dist) {
    super(var, dist);
  }
  
  @SuppressWarnings("unchecked")
  public V getVariable() {
    return (V)getTail();
  }
  
  @SuppressWarnings("unchecked")
  public D getDistribution() {
    return (D)getHead();
  }
}
