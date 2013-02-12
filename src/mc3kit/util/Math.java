package mc3kit.util;

import static java.lang.Math.log;
import cern.jet.random.*;
import cern.jet.random.engine.RandomEngine;

public final class Math {
  public static double LOG_PI = log(java.lang.Math.PI);
  public static double LOG_TWO_PI = log(2 * java.lang.Math.PI);
  
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
            || log(rng.nextDouble()) < logAcceptanceProbability) {
          accepted = true;
        }
      }
    }

    return accepted;
  }
}
