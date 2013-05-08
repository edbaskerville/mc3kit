package mc3kit.step.demc;

import static java.lang.Math.*;
import static java.lang.String.format;
import static mc3kit.util.Math.*;
import static mc3kit.util.Utils.*;

import java.util.*;
import java.util.Arrays;
import java.util.logging.*;

import mc3kit.*;
import mc3kit.mcmc.Chain;
import mc3kit.model.Model;
import mc3kit.util.*;
import cern.colt.*;
import cern.colt.function.IntComparator;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.random.engine.RandomEngine;

public class DEMCBlockSizeManager {
	transient DEMCProposalStep step;
	transient DEMCProposalTask task;
	
	int blockSize;
	
	double snookerGammaFactor;
	double parallelGammaFactor;
	
	MultiCounter<DEMCCounterType> parallelSmallCounter;
	MultiCounter<DEMCCounterType> parallelLargeCounter;
	MultiCounter<DEMCCounterType> snookerCounter;
	
	DEMCBlockSizeManager(DEMCProposalTask task, int scale) {
		this.step = task.step;
		this.task = task;
		this.blockSize = scale;
		
		snookerGammaFactor = 1.0;
		parallelGammaFactor = 1.0;
		
		parallelSmallCounter = new MultiCounter<DEMCCounterType>();
		parallelLargeCounter = new MultiCounter<DEMCCounterType>();
		snookerCounter = new MultiCounter<DEMCCounterType>();
	}
	
	void setTask(DEMCProposalTask task) {
		this.step = task.step;
		this.task = task;
	}
	
	void step(Chain chain, Model model) throws MC3KitException {
		if(chain.getLogger().isLoggable(Level.FINER)) {
			chain.getLogger()
					.finer(format("Stepping block size %d", blockSize));
		}
		
		proposeDEMC(chain, model);
		recordStats(chain, chain.getChainId());
		tune(chain, chain.getChainId());
	}
	
	void recordStats(Chain chain, int chainId) throws MC3KitException {
		long iteration = chain.getIteration();
		if(iteration % step.tuneEvery == 0) {
			Map<String, Object> infoObj = makeMap("iteration", iteration,
					"chainId", chainId, "blockSize", blockSize,
					"snookerGammaFactor", snookerGammaFactor, "snookerRates",
					getCounterObject(snookerCounter), "parallelGammaFactor",
					parallelGammaFactor, "parallelSmallRates",
					getCounterObject(parallelSmallCounter),
					"parallelLargeRates",
					getCounterObject(parallelLargeCounter));
			chain.getLogger().log(Level.INFO,
					"DEMCProposalStep acceptance rates", infoObj);
		}
	}
	
	void tune(Chain chain, int chainId) throws MC3KitException {
		long iteration = chain.getIteration();
		if(iteration % step.tuneEvery == 0) {
			Logger logger = chain.getLogger();
			
			if(iteration <= step.tuneFor) {
				if(logger.isLoggable(Level.FINE)) {
					logger.fine(format("Tuning for %d...", blockSize));
				}
				
				if(step.useParallel) {
					double parallelRate = parallelSmallCounter
							.getRate(DEMCCounterType.ACCEPTANCE);
					if(logger.isLoggable(Level.FINE)) {
						logger.fine(format("Old parallelGammaFactor: %f",
								parallelGammaFactor));
					}
					parallelGammaFactor = adjustTuningParameter(
							parallelGammaFactor, parallelRate,
							step.targetAcceptanceRate);
					if(logger.isLoggable(Level.FINE)) {
						logger.fine(format("New parallelGammaFactor: %f",
								parallelGammaFactor));
					}
				}
				
				if(step.useSnooker) {
					double snookerRate = snookerCounter
							.getRate(DEMCCounterType.ACCEPTANCE);
					if(logger.isLoggable(Level.FINE)) {
						logger.fine(format("Old snookerGammaFactor: %f",
								snookerGammaFactor));
					}
					snookerGammaFactor = adjustTuningParameter(
							snookerGammaFactor, snookerRate,
							step.targetAcceptanceRate);
					if(logger.isLoggable(Level.FINE)) {
						logger.fine(format("New snookerGammaFactor: %f",
								snookerGammaFactor));
					}
				}
			}
			parallelSmallCounter.reset();
			parallelLargeCounter.reset();
			snookerCounter.reset();
			
		}
	}
	
	Map<String, Object> getCounterObject(MultiCounter<DEMCCounterType> counter)
			throws MC3KitException {
		return makeMap("count", counter.getCount(), "acceptance",
				counter.getRate(DEMCCounterType.ACCEPTANCE), "rejection",
				counter.getRate(DEMCCounterType.REJECTION), "impossible",
				counter.getRate(DEMCCounterType.IMPOSSIBLE));
	}
	
	void proposeDEMC(Chain chain, Model xModel) throws MC3KitException {
		// Implementation of DEMC-Z and DEMC-ZS algorithms from
		//
		// Cajo J.F. ter Braak and Jasper A. Vrugt
		// Differential Evolution Markov Chain with snooker updater and
		// fewer chains
		// Stat Comput (2008) 18: 435--446
		// DOI 10.1007/s11222-008-9104-9
		//
		// Modifications: one chain. All non-proposal vectors are
		// sampled
		// from past. No noise: noise needs to be introduced in another
		// step (e.g. one-at-a-time proposals to variables).
		//
		// Variable names chosen to match those in paper.
		
		Logger logger = xModel.getLogger();
		
		if(logger.isLoggable(Level.FINE)) {
			logger.finer(format("Proposing %d...", blockSize));
		}
		
		double priorHeatExp = chain.getPriorHeatExponent();
		double likeHeatExp = chain.getLikelihoodHeatExponent();
		RandomEngine rng = chain.getRng();
		
		// Random sample from past to determine variable order and
		// alignment
		DoubleMatrix1D refVec = task.getRandomSamples(chain, rng, 1)[0];
		
		// Get order of entries in a way that makes
		// covarying/anti-covarying
		// entries tend to get lumped together
		int[] entryOrder = getEntryOrder(refVec, logger, chain.getIteration(),
				chain.getMCMC().getThin());
		
		if(logger.isLoggable(Level.FINER)) {
			logger.finer(format("Entry order %d: %s", blockSize,
					Arrays.toString(entryOrder)));
		}
		
		// Do a snooker, small parallel, and large parallel proposal for
		// each block
		for(int i = 0; i < entryOrder.length; i += blockSize) {
			if(logger.isLoggable(Level.FINER)) {
				logger.finer(format("Proposing blockSize %d, blockStart %d",
						blockSize, i));
			}
			int blockEnd = i + blockSize;
			if(blockEnd > entryOrder.length)
				blockEnd = entryOrder.length;
			int[] block = Arrays.copyOfRange(entryOrder, i, blockEnd);
			
			if(step.useParallel) {
				proposeBlockDEMCParallel(chain, priorHeatExp, likeHeatExp,
						false, block, xModel, rng);
				if(step.useLarge) {
					proposeBlockDEMCParallel(chain, priorHeatExp, likeHeatExp,
							true, block, xModel, rng);
				}
			}
			if(step.useSnooker) {
				proposeBlockDEMCSnooker(chain, priorHeatExp, likeHeatExp,
						block, xModel, rng);
			}
		}
	}
	
	void proposeBlockDEMCSnooker(Chain chain, double priorHeatExp,
			double likeHeatExp, int[] block, Model xModel, RandomEngine rng)
			throws MC3KitException {
		// Scale factor of 1.7 is optimal for normal/Student posteriors;
		// adjusted by factor to target this distribution
		double gamma = snookerGammaFactor * 1.7;
		
		proposeBlockDEMC(chain, priorHeatExp, likeHeatExp, gamma, false, true,
				block, xModel, rng);
	}
	
	void proposeBlockDEMCParallel(Chain chain, double priorHeatExp,
			double likeHeatExp, boolean isLarge, int[] block, Model xModel,
			RandomEngine rng) throws MC3KitException {
		// Scale factor of 2.38/sqrt(2d) is optimal for normal/Student
		// posteriors.
		// For "large" proposals, to help avoid local minima,
		// gamma = 2 * base scale factor
		double gamma = parallelGammaFactor * 2.38 / sqrt(2 * block.length);
		if(isLarge)
			gamma *= 2.0;
		
		proposeBlockDEMC(chain, priorHeatExp, likeHeatExp, gamma, isLarge,
				false, block, xModel, rng);
	}
	
	void proposeBlockDEMC(Chain chain, double priorHeatExp, double likeHeatExp,
			double gamma, boolean isLarge, boolean isSnooker, int[] block,
			Model xModel, RandomEngine rng) throws MC3KitException {
		Logger logger = xModel.getLogger();
		int d = block.length;
		
		DoubleMatrix1D xOld = task.makeVector(xModel, block);
		
		DoubleMatrix1D z = null;
		double xMinusZNormOld = 0.0;
		DoubleMatrix1D z1;
		DoubleMatrix1D z2;
		if(isSnooker) {
			DoubleMatrix1D[] samps;
			DoubleMatrix1D xMinusZOld;
			
			do {
				// Get three random samples:
				// * z: to define projection vector (x - z)
				// * zR1, zR2: to project onto (x - z) to define
				// difference
				samps = task.getRandomSamples(chain, rng, 3);
				
				z = samps[0].viewSelection(block);
				xMinusZOld = subtract(xOld, z);
				xMinusZNormOld = norm2(xMinusZOld);
			} while(xMinusZNormOld == 0.0);
			DoubleMatrix1D zR1 = samps[1].viewSelection(block);
			DoubleMatrix1D zR2 = samps[2].viewSelection(block);
			
			// Project zR1, zR2 onto x - z
			divideInPlace(xMinusZOld, xMinusZNormOld);
			z1 = project(zR1, xMinusZOld);
			z2 = project(zR2, xMinusZOld);
		}
		else {
			// Get two random samples to define difference
			DoubleMatrix1D[] samps = task.getRandomSamples(chain, rng, 2);
			z1 = samps[0].viewSelection(block);
			z2 = samps[1].viewSelection(block);
		}
		
		// Update x
		DoubleMatrix1D xNew = new DenseDoubleMatrix1D(d);
		for(int i = 0; i < d; i++) {
			xNew.setQuick(i,
					xOld.getQuick(i) + gamma
							* (z1.getQuick(i) - z2.getQuick(i)));
		}
		
		// Perform update
		if(logger.isLoggable(Level.FINE)) {
			logger.fine(format("Setting entries %s to %s",
					Arrays.toString(block), xNew));
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
			invalidValues = !task.vectorIsValid(xModel, block, xNew);
		}
		
		boolean invalidPriorLike = false;
		double newLogPrior = Double.NEGATIVE_INFINITY;
		double newLogLike = Double.NEGATIVE_INFINITY;
		if(!invalidValues) {
			xModel.beginProposal();
			task.setVector(xModel, block, xNew);
			xModel.endProposal();
			newLogPrior = xModel.getLogPrior();
			newLogLike = xModel.getLogLikelihood();
			
			invalidPriorLike = Double.isInfinite(newLogPrior)
					|| Double.isInfinite(newLogLike)
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
				logProposalRatio = (d - 1)
						* (log(xMinusZNormNew) - log(xMinusZNormOld));
			}
			else {
				logProposalRatio = 0.0;
			}
			
			accepted = shouldAcceptMetropolisHastings(rng, priorHeatExp,
					likeHeatExp, oldLogPrior, oldLogLike, newLogPrior,
					newLogLike, logProposalRatio);
		}
		
		MultiCounter<DEMCCounterType> counter = isSnooker ? snookerCounter
				: (isLarge ? parallelLargeCounter : parallelSmallCounter);
		if(accepted) {
			logger.fine("Accepted");
			xModel.acceptProposal();
			counter.record(DEMCCounterType.ACCEPTANCE);
		}
		else if(invalidValues) {
			logger.fine("Impossible");
			counter.record(DEMCCounterType.REJECTION,
					DEMCCounterType.IMPOSSIBLE);
		}
		else {
			logger.fine("Rejected");
			xModel.beginRejection();
			task.setVector(xModel, block, xOld);
			xModel.endRejection();
			
			counter.record(DEMCCounterType.REJECTION);
		}
	}
	
	// Sort entries by abs(std dev-normalized distance from mean)
	// so that covarying or anti-covarying quantities will tend to
	// cluster together
	int[] getEntryOrder(DoubleMatrix1D x, Logger logger, long iteration,
			long thin) {
		final double[] xRel = new double[x.size()];
		final int[] order = new int[x.size()];
		
		// Generate original order and x values standardized by
		// mean/stddev estimates
		for(int i = 0; i < xRel.length; i++) {
			double N = (iteration - step.recordHistoryAfter) / thin;
			double iMean = task.sums[i] / N;
			double iSD = N / (N - 1.0) * (task.sumSqs[i] / N - iMean * iMean);
			
			order[i] = i;
			if(iSD == 0)
				xRel[i] = 0;
			else
				xRel[i] = abs((x.getQuick(i) - iMean) / iSD);
			
			if(Double.isInfinite(xRel[i]) || Double.isNaN(xRel[i])) {
				logger.warning(format(
						"Got infinite or NaN xRel for entry %s(%d): (%f - %f)/%f",
						task.varNames.get(i), i, x.getQuick(i), iMean, iSD));
				xRel[i] = 0;
			}
		}
		
		IntComparator comparator = new IntComparator() {
			@Override
			public int compare(int a, int b) {
				return xRel[a] == xRel[b] ? 0 : (xRel[a] < xRel[b] ? -1 : 1);
			}
		};
		
		Swapper swapper = new Swapper() {
			@Override
			public void swap(int a, int b) {
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
