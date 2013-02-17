package mc3kit.util;

import java.util.*;

@SuppressWarnings("serial")
public class IterableBitSet extends BitSet implements Iterable<Integer> {

  public IterableBitSet() {
    super();
  }

  public IterableBitSet(int n) {
    super(n);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new BitSetIterator(this);
  }
}
