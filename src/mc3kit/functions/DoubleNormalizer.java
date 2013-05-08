package mc3kit.functions;

import mc3kit.*;
import mc3kit.model.Model;
import mc3kit.model.ModelNode;
import mc3kit.types.doublearray.DoubleArrayFunction;
import mc3kit.types.doublevalue.DoubleValued;

@SuppressWarnings("serial")
public class DoubleNormalizer extends DoubleArrayFunction {
  
  DoubleValued[] values;
  
  protected DoubleNormalizer() { }
  
  public DoubleNormalizer(Model model, DoubleValued... values) throws MC3KitException {
    super(model, values.length);
    for(DoubleValued value : values) {
      model.addEdge(this, (ModelNode)value);
    }
    this.values = values.clone();  
  }
  
  @Override
  public int getLength() {
    return 0;
  }
  
  @Override
  public boolean update() {
    double sum = 0.0;
    for(DoubleValued value : values) {
      sum += value.getValue();
    }
    for(int i = 0; i < values.length; i++) {
      setValue(i, values[i].getValue() / sum);
    }
    return true;
  }
}
