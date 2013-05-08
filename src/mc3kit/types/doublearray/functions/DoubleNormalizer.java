package mc3kit.types.doublearray.functions;

import mc3kit.*;
import mc3kit.model.*;
import mc3kit.types.doublevalue.*;
import mc3kit.types.doublearray.*;

public class DoubleNormalizer extends DoubleArrayFunction {
  
  DoubleValued[] values;
  
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
