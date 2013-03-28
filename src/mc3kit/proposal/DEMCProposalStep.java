/***
  This file is part of mc3kit.
  
  Copyright (C) 2013 Edward B. Baskerville

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
***/

package mc3kit.proposal;

import static java.lang.Math.*;
import static mc3kit.util.Math.*;
import static java.lang.String.format;
import static mc3kit.util.Utils.*;
import mc3kit.*;
import mc3kit.util.*;

import java.io.Serializable;
import java.util.*;
import java.util.Arrays;
import java.util.logging.*;

import org.tmatesoft.sqljet.core.*;
import org.tmatesoft.sqljet.core.table.*;

import cern.colt.*;
import cern.colt.function.*;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.jet.random.*;
import cern.jet.random.engine.*;

@SuppressWarnings("serial")
public class DEMCProposalStep implements Step {
  private static enum CounterType 
  {
    ACCEPTANCE,
    REJECTION,
    IMPOSSIBLE
  }
  
  double targetAcceptanceRate;
  long tuneFor;
  long tuneEvery;
  long historyThin;
  long initialHistoryCount;
  long recordHistoryAfter;
  int minBlockSize;
  int maxBlockSize;
  boolean useParallel;
  boolean useLarge;
  boolean useSnooker;
  
  protected DEMCProposalStep() { }
  
  public DEMCProposalStep(
      double targetAcceptanceRate,
      long tuneFor,
      long tuneEvery,
      long historyThin,
      long initialHistoryCount,
      int minBlockSize,
      int maxBlockSize,
      boolean useParallel,
      boolean useLarge,
      boolean useSnooker
  ) {
    this(targetAcceptanceRate, tuneFor, tuneEvery, historyThin, initialHistoryCount, 0, minBlockSize, maxBlockSize, useParallel, useLarge, useSnooker);
  }
  
  /**
   * Constructor for a differential evolution MCMC (DEMC) step.
   * 
   * Cajo J.F. ter Braak and Jasper A. Vrugt
   * Differential Evolution Markov Chain with snooker updater and fewer chains 
   * Stat Comput (2008) 18: 435--446
   * DOI 10.1007/s11222-008-9104-9
   * 
   * The paper uses multiple chains; this implementation exists on a single chain and uses
   * historical samples exclusively to generate proposals.
   * 
   * The step will do the following:
   * (1) If useParallel is true, then do standard DEMC proposals (difference of historical samples).
   * (2) If useParallel AND useLarge is true, then also do double-size proposals.
   * (3) If useSnooker is on, also perform snooker proposals (project differences into a sensible direction).
   * 
   * For each of these, proposals will be done at multiple block sizes:
   * minBlockSize, 2 * minBlockSize, 4 * minBlockSize, 8 * minBlockSize, ..., N
   * where N = minBlockSize * 2^n and N <= maxBlockSize.
   * 
   * If all three proposal types are on, then a single step will make 3m proposals to each
   * variable, where m = # of different block sizes.
   * 
   * @param targetAcceptanceRate What fraction of proposals should be accepted?
   * @param tuneFor How many iterations the tuning period lasts.
   * @param tuneEvery How often to tune within the tuning period.
   * @param historyThin How many samples go by for every historical sample recorded for the DEMC.
   * @param initialHistoryCount The number of historical samples to accumulate before doing DEMC.
   * @param minBlockSize The smallest number of variables to propose at a time.
   * @param maxBlockSize The
   * @param useParallel
   * @param useLarge
   * @param useSnooker
   */
  public DEMCProposalStep(
      double targetAcceptanceRate,
      long tuneFor,
      long tuneEvery,
      long historyThin,
      long initialHistoryCount,
      long recordHistoryAfter,
      int minBlockSize,
      int maxBlockSize,
      boolean useParallel,
      boolean useLarge,
      boolean useSnooker
  ) {
    super();
    this.targetAcceptanceRate = targetAcceptanceRate;
    this.tuneFor = tuneFor;
    this.tuneEvery = tuneEvery;
    this.historyThin = historyThin;
    this.initialHistoryCount = initialHistoryCount;
    this.recordHistoryAfter = recordHistoryAfter;
    this.minBlockSize = minBlockSize;
    this.maxBlockSize = maxBlockSize;
    this.useParallel = useParallel;
    this.useLarge = useLarge;
    this.useSnooker = useSnooker;
  }

  @Override
  public List<Task> makeTasks(int chainCount) throws MC3KitException {
    List<Task> tasks = new ArrayList<Task>();
    for(int i = 0; i < chainCount; i++) {
      tasks.add(new DEMCProposalTask(i));
    }
    return tasks;
  }
  
  /*** TASK INTERFACE IMPLEMENTATION ***/
  
  class DEMCProposalTask implements Task {
    boolean initialized;
    
    int chainId;
    List<String> varNames;
    
    long historyCount;
    double[] sums;
    double[] sumSqs;
    
    List<BlockSizeManager> blockSizeManagers;
    
    /*** CONSTRUCTOR ***/
    
    public DEMCProposalTask(int chainId) {
      this.chainId = chainId;
    }
    
    @Override
    public int[] getChainIds() {
      return new int[] { chainId };
    }

    @Override
    public void step(Chain[] chains) throws MC3KitException {
      assert (chains.length == 1);

      Chain chain = chains[0];
      Logger logger = chain.getLogger();
      if(logger.isLoggable(Level.FINE)) {
        logger.fine(format("DEMCProposalStep stepping %d", chainId));
      }
      
      initialize(chain);
      
      long iteration = chain.getIteration();
      
      // Only iterate if we have enough initial values
      Model model = chain.getModel();
      if(historyCount >= initialHistoryCount) {
        for(BlockSizeManager bsm : blockSizeManagers) {
          bsm.step(chain, model);
        }
      }
      
      if(iteration > recordHistoryAfter) {
        recordHistory(chain);
      }
    }
    
    /*** PRIVATE METHODS ***/
    
    long getHistoryCount(Chain chain) throws MC3KitException {
      try {
        SqlJetDb db = chain.getDb();
        db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
        ISqlJetTable table = db.getTable("demcHistory");
        long rowCount = table.open().getRowCount();
        db.commit();
        return rowCount;
      }
      catch(SqlJetException e) {
        throw new MC3KitException(format("Got exception trying to count history (chain %d)", chain.getChainId()), e);
      }
    }
    
    DoubleMatrix1D makeVector(Model model) {
      DoubleMatrix1D vec = new DenseDoubleMatrix1D(varNames.size());
      for(int i = 0; i < varNames.size(); i++) {
        vec.setQuick(i, model.getDoubleVariable(varNames.get(i)).getValue());
      }
      return vec;
    }
    
    DoubleMatrix1D makeVector(Model model, int[] block) {
      DoubleMatrix1D vec = new DenseDoubleMatrix1D(block.length);
      for(int i = 0; i < block.length; i++) {
        vec.setQuick(i, model.getDoubleVariable(varNames.get(block[i])).getValue());
      }
      return vec;
    }
    
    boolean vectorIsValid(Model model, int[] block, DoubleMatrix1D xNew) throws MC3KitException {
      boolean valid = true;
      for(int i = 0; i < block.length; i++) {
        if(!model.getDoubleVariable(varNames.get(block[i])).valueIsValid(xNew.get(i))) {
          valid = false;
          break;
        }
      }
      return valid;
    }
    
    void setVector(Model model, int[] block, DoubleMatrix1D xNew) {
      for(int i = 0; i < block.length; i++) {
        model.getDoubleVariable(varNames.get(block[i])).setValue(xNew.get(i));
      }
    }
    
    private void initialize(Chain chain) throws MC3KitException {
      if(initialized) return;
      initialized = true;
      
      // Initialize table with one row per iteration for fast access to past samples
      try {
        SqlJetDb db = chain.getDb();
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        db.createTable("CREATE TABLE demcHistory (sample BLOB)");
        db.commit();
      }
      catch(SqlJetException e) {
        throw new MC3KitException(format("Couldn't create demc_history table on chain %d", chain.getChainId()), e);
      }
      
      Model model = chain.getModel();
      
      varNames = new ArrayList<String>();
      for(String varName : model.getUnobservedVariableNames()) {
        if(model.getVariable(varName) instanceof DoubleVariable) {
          varNames.add(varName);
        }
      }
      
      sums = new double[varNames.size()];
      sumSqs = new double[varNames.size()];
      
      // Create managers for each block size.
      // Block sizes are minBlockSize, 2*minBlockSize, 4*minBlockSize, ...
      int blockSize = minBlockSize;
      blockSizeManagers = new ArrayList<BlockSizeManager>();
      while(blockSize <= maxBlockSize && blockSize <= varNames.size())
      {
        if(model.getLogger().isLoggable(Level.FINE)) {
          model.getLogger().fine(format("Creating block size manager for block size = %d", blockSize));
        }
        blockSizeManagers.add(new BlockSizeManager(blockSize));
        blockSize *= 2;
      }
    }
    
    private void recordHistory(Chain chain) throws MC3KitException {
      Model model = chain.getModel();
      
      double[] values = new double[varNames.size()];
      for(int i = 0; i < values.length; i++) {
        values[i] = model.getDoubleVariable(varNames.get(i)).getValue();
        sums[i] += values[i];
        sumSqs[i] += values[i] * values[i];
      }
      
      try {
        SqlJetDb db = chain.getDb();
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        ISqlJetTable table = db.getTable("demcHistory");
        table.insert(toBytes(values));
        db.commit();
      }
      catch(SqlJetException e) {
        throw new MC3KitException(format("Error recording history on chain %d", chain.getChainId()), e);
      }
      
      historyCount++;
    }
    
    private DoubleMatrix1D[] getRandomSamples(Chain chain, RandomEngine rng, int count) throws MC3KitException
    {
      Uniform unif = new Uniform(rng);
      
      long[] indexes = new long[count];
      DoubleMatrix1D[] samples = new DoubleMatrix1D[count];
      
      for(int i = 0; i < count; i++)
      {
        boolean done = false;
        while(!done)
        {
          indexes[i] = unif.nextLongFromTo(0, historyCount - 1);
          done = true;
          for(int j = 0; j < i; j++)
          {
            if(indexes[i] == indexes[j])
            {
              done = false;
              break;
            }
          }
        }
        samples[i] = getSample(chain, indexes[i]);
      }
      
      return samples;
    }
    
    private DoubleMatrix1D getSample(Chain chain, long index) throws MC3KitException {
      try {
        SqlJetDb db = chain.getDb();
        db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        ISqlJetTable table = db.getTable("demcHistory");
        ISqlJetCursor c = table.open();
        c.goToRow(index+1);
        double[] values = fromBytes(c.getBlobAsArray("sample")); 
        db.commit();
        
        return new DenseDoubleMatrix1D(values);
      }
      catch(SqlJetException e) {
        throw new MC3KitException(e);
      }
    }
    
    /*** MANAGER FOR PROPOSALS FOR A SINGLE BLOCK SIZE ***/
    
    private class BlockSizeManager implements Serializable
    {
      int blockSize;
      
      double snookerGammaFactor;
      double parallelGammaFactor;
      
      MultiCounter<CounterType> parallelSmallCounter;
      MultiCounter<CounterType> parallelLargeCounter;
      MultiCounter<CounterType> snookerCounter;
      
      BlockSizeManager(int scale)
      {
        this.blockSize = scale;
        
        snookerGammaFactor = 1.0;
        parallelGammaFactor = 1.0;
        
        parallelSmallCounter = new MultiCounter<CounterType>();
        parallelLargeCounter = new MultiCounter<CounterType>();
        snookerCounter = new MultiCounter<CounterType>();
      }
      
      void step(Chain chain, Model model) throws MC3KitException
      {
        if(chain.getLogger().isLoggable(Level.FINER)) {
          chain.getLogger().finer(format("Stepping block size %d", blockSize));
        }
        
        proposeDEMC(chain, model);
        recordStats(chain, chainId);
        tune(chain, chainId);
      }
      
      void recordStats(Chain chain, int chainId) throws MC3KitException
      {
        long iteration = chain.getIteration();
        if(iteration % tuneEvery == 0) {
          Map<String, Object> infoObj = makeMap(
            "iteration", iteration,
            "chainId", chainId,
            "blockSize", blockSize,
            "snookerGammaFactor", snookerGammaFactor,
            "snookerRates", getCounterObject(snookerCounter),
            "parallelGammaFactor", parallelGammaFactor,
            "parallelSmallRates", getCounterObject(parallelSmallCounter),
            "parallelLargeRates", getCounterObject(parallelLargeCounter)
          );
          chain.getLogger().log(Level.INFO, "DEMCProposalStep acceptance rates", infoObj);
        }
      }
      
      void tune(Chain chain, int chainId) throws MC3KitException
      {
        long iteration = chain.getIteration();
        if(iteration % tuneEvery == 0)
        {
          Logger logger = chain.getLogger();
          
          if(iteration <= tuneFor)
          {
            if(logger.isLoggable(Level.FINE)) {
              logger.fine(format("Tuning for %d...", blockSize));
            }
            
            if(useParallel) {
              double parallelRate = parallelSmallCounter.getRate(CounterType.ACCEPTANCE);
              if(logger.isLoggable(Level.FINE)) {
                logger.fine(format("Old parallelGammaFactor: %f", parallelGammaFactor));
              }
              parallelGammaFactor = adjustTuningParameter(
                parallelGammaFactor, parallelRate, targetAcceptanceRate
              );
              if(logger.isLoggable(Level.FINE)) {
                logger.fine(format("New parallelGammaFactor: %f", parallelGammaFactor));
              }
            }
            
            if(useSnooker) {
              double snookerRate = snookerCounter.getRate(CounterType.ACCEPTANCE);
              if(logger.isLoggable(Level.FINE)) {
                logger.fine(format("Old snookerGammaFactor: %f", snookerGammaFactor));
              }
              snookerGammaFactor = adjustTuningParameter(
                snookerGammaFactor, snookerRate, targetAcceptanceRate
              );
              if(logger.isLoggable(Level.FINE)) {
                logger.fine(format("New snookerGammaFactor: %f", snookerGammaFactor));
              }
            }
          }
          parallelSmallCounter.reset();
          parallelLargeCounter.reset();
          snookerCounter.reset();
          
        }
      }
      
      Map<String, Object> getCounterObject(MultiCounter<CounterType> counter) throws MC3KitException
      {
        return makeMap(
          "count", counter.getCount(),
          "acceptance", counter.getRate(CounterType.ACCEPTANCE),
          "rejection", counter.getRate(CounterType.REJECTION),
          "impossible", counter.getRate(CounterType.IMPOSSIBLE)
        );
      }
      
      void proposeDEMC(Chain chain, Model xModel) throws MC3KitException
      {
        // Implementation of DEMC-Z and DEMC-ZS algorithms from
        //
        // Cajo J.F. ter Braak and Jasper A. Vrugt
        // Differential Evolution Markov Chain with snooker updater and fewer chains 
        // Stat Comput (2008) 18: 435--446
        // DOI 10.1007/s11222-008-9104-9
        // 
        // Modifications: one chain. All non-proposal vectors are sampled
        // from past. No noise: noise needs to be introduced in another
        // step (i.e. one-at-a-time proposals to variables).
        // 
        // Variable names chosen to match those in paper.
        
        Logger logger = xModel.getLogger();
        
        if(logger.isLoggable(Level.FINE)) {
          logger.finer(format("Proposing %d...", blockSize));
        }
        
        double priorHeatExp = chain.getPriorHeatExponent();
        double likeHeatExp = chain.getLikelihoodHeatExponent();
        RandomEngine rng = chain.getRng();
        
        // Random sample from past to determine variable order and alignment
        DoubleMatrix1D refVec = getRandomSamples(chain, rng, 1)[0];
        
        // Get order of entries in a way that makes covarying/anti-covarying
        // entries tend to get lumped together
        int[] entryOrder = getEntryOrder(refVec, logger);
        
        if(logger.isLoggable(Level.FINER)) {
          logger.finer(format("Entry order %d: %s", blockSize, Arrays.toString(entryOrder)));
        }
        
        // Do a snooker, small parallel, and large parallel proposal for each block
        for(int i = 0; i < entryOrder.length; i += blockSize)
        {
          if(logger.isLoggable(Level.FINER)) {
            logger.finer(format("Proposing blockSize %d, blockStart %d", blockSize, i));
          }
          int blockEnd = i + blockSize;
          if(blockEnd > entryOrder.length) blockEnd = entryOrder.length;
          int[] block = Arrays.copyOfRange(entryOrder, i, blockEnd);
          
          if(useParallel) {
            proposeBlockDEMCParallel(chain, priorHeatExp, likeHeatExp, false, block, xModel, rng);
            if(useLarge) {
              proposeBlockDEMCParallel(chain, priorHeatExp, likeHeatExp, true, block, xModel, rng);
            }
          }
          if(useSnooker) {
            proposeBlockDEMCSnooker(chain, priorHeatExp, likeHeatExp, block, xModel, rng);
          }
        }
      }
      
      void proposeBlockDEMCSnooker(
        Chain chain,
        double priorHeatExp, double likeHeatExp,
        int[] block,
        Model xModel,
        RandomEngine rng) throws MC3KitException
      {
        // Scale factor of 1.7 is optimal for normal/Student posteriors;
        // adjusted by factor to target this distribution
        double gamma = snookerGammaFactor * 1.7;
        
        proposeBlockDEMC(chain, priorHeatExp, likeHeatExp, gamma, false, true, block, xModel, rng);
      }
      
      void proposeBlockDEMCParallel(
          Chain chain,
          double priorHeatExp, double likeHeatExp,
          boolean isLarge,
          int[] block,
          Model xModel,
          RandomEngine rng) throws MC3KitException 
      {
        // Scale factor of 2.38/sqrt(2d) is optimal for normal/Student
        // posteriors.
        // For "large" proposals, to help avoid local minima,
        // gamma = 2 * base scale factor
        double gamma = parallelGammaFactor * 2.38 / sqrt(2 * block.length);
        if(isLarge)
          gamma *= 2.0;
        
        proposeBlockDEMC(chain, priorHeatExp, likeHeatExp, gamma, isLarge, false, block, xModel, rng);
      }
      
      void proposeBlockDEMC(
          Chain chain,
          double priorHeatExp, double likeHeatExp,
          double gamma,
          boolean isLarge,
          boolean isSnooker,
          int[] block,
          Model xModel,
          RandomEngine rng) throws MC3KitException 
      {
        Logger logger = xModel.getLogger();
        int d = block.length;
        
        DoubleMatrix1D xOld = makeVector(xModel, block);
        
        DoubleMatrix1D z = null;
        double xMinusZNormOld = 0.0;
        DoubleMatrix1D z1;
        DoubleMatrix1D z2;
        if(isSnooker) {
          DoubleMatrix1D[] samps;
          DoubleMatrix1D xMinusZOld;
          
          do
          {
            // Get three random samples:
            // * z: to define projection vector (x - z)
            // * zR1, zR2: to project onto (x - z) to define difference
            samps = getRandomSamples(chain, rng, 3);
            
            z = samps[0].viewSelection(block);
            xMinusZOld = subtract(xOld, z);
            xMinusZNormOld = norm2(xMinusZOld);
          }
          while(xMinusZNormOld == 0.0);
          DoubleMatrix1D zR1 = samps[1].viewSelection(block);
          DoubleMatrix1D zR2 = samps[2].viewSelection(block);
          
          // Project zR1, zR2 onto x - z
          divideInPlace(xMinusZOld, xMinusZNormOld);
          z1 = project(zR1, xMinusZOld);
          z2 = project(zR2, xMinusZOld);
        }
        else { 
          // Get two random samples to define difference
          DoubleMatrix1D[] samps = getRandomSamples(chain, rng, 2);
          z1 = samps[0].viewSelection(block);
          z2 = samps[1].viewSelection(block);
        }
        
        // Update x
        DoubleMatrix1D xNew = new DenseDoubleMatrix1D(d);
        for(int i = 0; i < d; i++)
        {
          xNew.setQuick(i,
            xOld.getQuick(i)
            + gamma * (z1.getQuick(i) - z2.getQuick(i))
          );
        }
        
        // Perform update
        if(logger.isLoggable(Level.FINE)) {
          logger.fine(format("Setting entries %s to %s", Arrays.toString(block), xNew));
        }
        double oldLogPrior = xModel.getLogPrior();
        double oldLogLike = xModel.getLogLikelihood();
        
        // Calculate final norm of difference, make sure it's not zero
        boolean invalidValues = false;
        double xMinusZNormNew = 0.0;
        if(isSnooker) { 
          DoubleMatrix1D xMinusZNew = subtract(xNew, z);
          xMinusZNormNew = norm2(xMinusZNew);
          
          if(xMinusZNormNew == 0.0) {
            invalidValues = true;
          }
        }
        if(!invalidValues) {
          invalidValues = !vectorIsValid(xModel, block, xNew);
        }
        
        boolean invalidPriorLike = false;
        double newLogPrior = Double.NEGATIVE_INFINITY;
        double newLogLike = Double.NEGATIVE_INFINITY;
        if(!invalidValues)
        {
          xModel.beginProposal();
          setVector(xModel, block, xNew);
          xModel.endProposal();
          newLogPrior = xModel.getLogPrior();
          newLogLike = xModel.getLogLikelihood();
          
          invalidPriorLike = Double.isInfinite(newLogPrior) || Double.isInfinite(newLogLike)
              || Double.isNaN(newLogPrior) || Double.isNaN(newLogLike);
        }
        
        // Acceptance/rejection
        boolean accepted;
        if(invalidValues || invalidPriorLike) {
          accepted = false;
        }
        else {
          double logProposalRatio;
          if(isSnooker) {
            logProposalRatio = (d - 1) * (log(xMinusZNormNew) - log(xMinusZNormOld));
          }
          else {
            logProposalRatio = 0.0;
          }
          
          accepted = shouldAcceptMetropolisHastings(
            rng,
            priorHeatExp, likeHeatExp,
            oldLogPrior, oldLogLike,
            newLogPrior, newLogLike,
            logProposalRatio
          );
        }
        
        MultiCounter<CounterType> counter = isSnooker ? snookerCounter :
          (isLarge ? parallelLargeCounter : parallelSmallCounter);
        if(accepted)
        {
          logger.fine("Accepted");
          xModel.acceptProposal();
          counter.record(CounterType.ACCEPTANCE);
        }
        else if(invalidValues)
        {
          logger.fine("Impossible");
          counter.record(CounterType.REJECTION, CounterType.IMPOSSIBLE);
        }
        else
        {
          logger.fine("Rejected");
          xModel.beginRejection();
          setVector(xModel, block, xOld);
          xModel.endRejection();
          
          counter.record(CounterType.REJECTION);
        }
      }
      
      // Sort entries by abs(std dev-normalized distance from mean)
      // so that covarying or anti-covarying quantities will tend to
      // cluster together
      int[] getEntryOrder(DoubleMatrix1D x, Logger logger)
      {
        final double[] xRel = new double[x.size()];
        final int[] order = new int[x.size()];
        
        // Generate original order and x values standardized by mean/stddev estimates
        for(int i = 0; i < xRel.length; i++)
        {
          double N = historyCount;
          double iMean = sums[i] / N;
          double iSD = N / (N - 1.0) * (sumSqs[i] / N - iMean * iMean);
          
          order[i] = i;
          if(iSD == 0)
            xRel[i] = 0;
          else
            xRel[i] = abs((x.getQuick(i) - iMean) / iSD);
          
          if(Double.isInfinite(xRel[i]) || Double.isNaN(xRel[i])) {
            logger.warning(format("Got infinite or NaN xRel for entry %s(%d): (%f - %f)/%f",
              varNames.get(i), i,
              x.getQuick(i), iMean, iSD
            ));
            xRel[i] = 0;
          }
        }
        
        IntComparator comparator = new IntComparator()
        {
          @Override
          public int compare(int a, int b)
          {
            return xRel[a] == xRel[b] ? 0 : (xRel[a] < xRel[b] ? -1 : 1);
          }
        };
        
        Swapper swapper = new Swapper()
        {
          @Override
          public void swap(int a, int b)
          {
            int tmpOrder;
            double tmpXRel;
            
            tmpOrder = order[a];
            tmpXRel = xRel[a];
            order[a] = order[b];
            xRel[a] = xRel[b];
            order[b] = tmpOrder;
            xRel[b] = tmpXRel;
          }
        };
        
        GenericSorting.quickSort(0, xRel.length, comparator, swapper);
        
        return order;
      }
    }
  }
}
