package mc3kit.util;

import static java.lang.Math.*;
import cern.jet.random.*;
import cern.jet.random.engine.RandomEngine;
import cern.jet.stat.Gamma;

public final class Math {
  public static double LOG_PI = log(java.lang.Math.PI);
  public static double LOG_TWO_PI = log(2 * java.lang.Math.PI);
  
  public static double logGamma(double x)
  {
    if(x == 1.0) return 0.0;
    return Gamma.logGamma(x);
  }
  
  public static double logBeta(double x, double y) {
    return logGamma(x) + logGamma(y) - logGamma(x + y);
  }
  
  public static int[] getRandomPermutation(int size, Uniform uniform) {
    int[] vals = new int[size];
    for(int i = 0; i < size; i++)
      vals[i] = i;
    shuffleInPlace(vals, uniform);
    return vals;
  }

  public static void shuffleInPlace(int[] list, Uniform uniform) {
    for(int i = 0; i < list.length - 1; i++) {
      int tmp = list[i];
      int j = uniform.nextIntFromTo(i, list.length - 1);
      list[i] = list[j];
      list[j] = tmp;
    }
  }

  public static boolean shouldAcceptMetropolisHastings(RandomEngine rng,
      double priorHeatExponent, double likelihoodHeatExponent,
      double oldLogPrior, double oldLogLikelihood, double newLogPrior,
      double newLogLikelihood, double logProposalRatio) {
    boolean accepted = false;
    if(!Double.isInfinite(newLogPrior) && !Double.isNaN(newLogPrior)
        && !Double.isInfinite(newLogLikelihood)
        && !Double.isNaN(newLogLikelihood)) {
      double logPriorRatio = newLogPrior - oldLogPrior;
      double logLikelihoodRatio = newLogLikelihood - oldLogLikelihood;

      if(priorHeatExponent == Double.POSITIVE_INFINITY
          && likelihoodHeatExponent == Double.POSITIVE_INFINITY) {
        if(logPriorRatio + logLikelihoodRatio > 0)
          accepted = true;
      }
      else if(priorHeatExponent == Double.POSITIVE_INFINITY) {
        if(logPriorRatio > 0)
          accepted = true;
      }
      else if(likelihoodHeatExponent == Double.POSITIVE_INFINITY) {
        if(logLikelihoodRatio > 0)
          accepted = true;
      }
      else {
        double logAcceptanceProbability = logProposalRatio + priorHeatExponent
            * logPriorRatio + likelihoodHeatExponent * logLikelihoodRatio;

        if(logAcceptanceProbability >= 0.0
            || log(rng.nextDouble()) <= logAcceptanceProbability) {
          accepted = true;
        }
      }
    }

    return accepted;
  }
  
  public static double logSumExp(double[] values)
  {
    double shift = Double.MIN_VALUE;
    for(int i = 0; i < values.length; i++)
    {
      if(values[i] > shift)
        shift = values[i];
    }
    
    double sumExpValues = 0;
    for(int i = 0; i < values.length; i++)
    {
      sumExpValues += exp(values[i] - shift);
    }
    return shift + log(sumExpValues);
  }
  
  public static double logSumExp(double[] values, double[] coeffs)
  {
    double shift = Double.MIN_VALUE;
    for(int i = 0; i < values.length; i++)
    {
      if(values[i] > shift)
        shift = values[i];
    }
    
    double sumExpValues = 0;
    for(int i = 0; i < values.length; i++)
    {
      sumExpValues += exp(values[i] - shift) * coeffs[i];
    }
    return shift + log(sumExpValues);
  }
  
  public static double logSumExp(double[] values, boolean[] negative)
  {
    double shift = Double.NEGATIVE_INFINITY;
    for(int i = 0; i < values.length; i++)
    {
      assert !Double.isNaN(values[i]);
      if(Double.isInfinite(values[i]))
      {
        assert values[i] < 0.0;
        continue;
      }
      
      if(values[i] > shift)
      {
        shift = values[i];
      }
    }
    
    double sumExpValues = 0;
    for(int i = 0; i < values.length; i++)
    {
      if(Double.isInfinite(values[i])) continue;
      sumExpValues += exp(values[i] - shift) * (negative[i] ? -1.0 : 1.0);
    }
    return shift + log(sumExpValues);
  }
}
