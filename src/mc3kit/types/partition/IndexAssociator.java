package mc3kit.types.partition;

import mc3kit.*;

public interface IndexAssociator {
  void associate(int itemIndex, int groupIndex) throws MC3KitException;
}
