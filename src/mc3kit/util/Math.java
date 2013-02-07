package mc3kit.util;

import cern.jet.random.*;

public final class Math {
  public static int[] getRandomPermutation(int size, Uniform uniform)
  {
    int[] vals = new int[size];
    for(int i = 0; i < size; i++)
      vals[i] = i;
    shuffleInPlace(vals, uniform);
    return vals;
  }
  
  public static void shuffleInPlace(int[] list, Uniform uniform)
  {
    for(int i = 0; i < list.length - 1; i++)
    {
      int tmp = list[i];
      int j = uniform.nextIntFromTo(i, list.length - 1);
      list[i] = list[j];
      list[j] = tmp;
    }
  }
}
