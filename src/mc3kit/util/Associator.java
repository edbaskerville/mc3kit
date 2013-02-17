package mc3kit.util;

import mc3kit.ModelException;
import mc3kit.ModelNode;

public interface Associator {
  void associate(ModelNode tail, ModelNode head) throws ModelException;
}
