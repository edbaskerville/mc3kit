package mc3kit.util;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class BitSetIterator implements Iterator<Integer> {
  private BitSet bitSet;
  private int index;
  
  public BitSetIterator(BitSet bitSet) {
   this.bitSet = bitSet;
   index = -1;
  }

  @Override
  public boolean hasNext() {
    return bitSet.nextSetBit(index + 1) != -1;
  }

  @Override
  public Integer next() {
    index = bitSet.nextSetBit(index + 1);
    if(index == -1) throw new NoSuchElementException("No more elements.");
    
    return index;
  }

  @Override
  public void remove() {
    bitSet.clear(index);
  }
}
