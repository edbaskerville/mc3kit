package mc3kit.partition;

import mc3kit.*;

public interface IndexAssociator {
  void associate(int itemIndex, int groupIndex) throws MC3KitException;
}
