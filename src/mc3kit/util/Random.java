package mc3kit.util;

import cern.jet.random.Uniform;

public final class Random {
  public static int nextIntFromToExcept(Uniform unif, int start, int end, int except)
  {
    int i = unif.nextIntFromTo(start, end - 1);
    if(i == except) i = end;
    return i;
  }
}
