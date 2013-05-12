package mc3kit.step.demc;

import static java.lang.String.format;
import static mc3kit.util.Utils.*;

import java.util.*;
import java.util.logging.*;

import mc3kit.*;
import mc3kit.mcmc.Chain;
import mc3kit.mcmc.Task;
import mc3kit.model.Model;
import mc3kit.model.Variable;
import mc3kit.types.doublevalue.DoubleVariable;

import org.tmatesoft.sqljet.core.*;
import org.tmatesoft.sqljet.core.table.*;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DEMCProposalTask implements Task {
	DEMCProposalStep step;
	
	boolean initialized;
	
	int chainId;
	List<String> varNames;
	
	double[] sums;
	double[] sumSqs;
	
	List<DEMCBlockSizeManager> blockSizeManagers;
	
	/*** CONSTRUCTOR ***/
	
	public DEMCProposalTask(DEMCProposalStep step, int chainId) {
		this.step = step;
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
		
		// Only iterate if we have enough initial values
		Model model = chain.getModel();
		if(chain.getIteration() > step.iterateAfter) {
			for(DEMCBlockSizeManager bsm : blockSizeManagers) {
				bsm.step(chain, model);
			}
		}
		
		recordState(chain);
	}
	
	/*** PRIVATE METHODS ***/
	
	long getHistoryCount(Chain chain) throws MC3KitException {
		try {
			SqlJetDb db = chain.getDb();
			return db.getTable(step.getTableName()).open().getRowCount();
		}
		catch(SqlJetException e) {
			throw new MC3KitException(format(
					"Got exception trying to count history (chain %d)",
					chain.getChainId()), e);
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
			vec.setQuick(i, model.getDoubleVariable(varNames.get(block[i]))
					.getValue());
		}
		return vec;
	}
	
	boolean vectorIsValid(Model model, int[] block, DoubleMatrix1D xNew)
			throws MC3KitException {
		boolean valid = true;
		for(int i = 0; i < block.length; i++) {
			if(!model.getDoubleVariable(varNames.get(block[i])).valueIsValid(
					xNew.get(i))) {
				valid = false;
				break;
			}
		}
		return valid;
	}
	
	void setVector(Model model, int[] block, DoubleMatrix1D xNew) {
		for(int i = 0; i < block.length; i++) {
			model.getDoubleVariable(varNames.get(block[i])).setValue(
					xNew.get(i));
		}
	}
	
	private void initialize(Chain chain) throws MC3KitException {
		if(initialized)
			return;
		initialized = true;
		
		boolean shouldRestore = chain.getMCMC().shouldRestore();
		
		if(!shouldRestore) {
			// Initialize table with one row per iteration for fast access
			// to past samples
			try {
				SqlJetDb db = chain.getDb();
				db.createTable(format(
						"CREATE TABLE %s (iteration INTEGER, sample BLOB, sums BLOB, sumSqs BLOB, state TEXT)",
						step.getTableName()));
//				db.createIndex(format("CREATE INDEX %s ON %s (iteration)",
//						step.getIndexName(), step.getTableName()));
			}
			catch(SqlJetException e) {
				throw new MC3KitException(format(
						"Couldn't create demc_history table on chain %d",
						chain.getChainId()), e);
			}
		}
		
		Model model = chain.getModel();
		
		varNames = new ArrayList<String>();
		for(Variable var : model.getUnobservedVariables()) {
			if(var instanceof DoubleVariable) {
				varNames.add(var.getName());
			}
		}
		
		if(shouldRestore) {
			int chainId = chain.getChainId();
			
			long iteration = chain.getIteration() - 1;
			try {
				ISqlJetTable table = chain.getDb()
						.getTable(step.getTableName());
				ISqlJetCursor c = table.open();
				if(!c.last()) {
					throw new MC3KitException(format("No DEMC state to restore from on chain %d", chainId));
				}
				if(c.getInteger("iteration") != iteration) {
					throw new MC3KitException(format("DEMC state iteration does not match chain (%d)", chainId));
				}
				
				if(iteration > step.recordHistoryAfter) {
					sums = fromBytes(c.getBlobAsArray("sums"));
					sumSqs = fromBytes(c.getBlobAsArray("sumSqs"));
				}
				
				Gson gson = chain.getGson();
				JsonObject stateJson = new JsonParser().parse(
						c.getString("state")).getAsJsonObject();
				
				blockSizeManagers = new ArrayList<DEMCBlockSizeManager>();
				for(JsonElement bsmJson : stateJson
						.getAsJsonArray("blockSizeManagers")) {
					DEMCBlockSizeManager bsm = gson.fromJson(bsmJson,
							DEMCBlockSizeManager.class);
					bsm.setTask(this);
					blockSizeManagers.add(bsm);
				}
			}
			catch(SqlJetException e) {
				throw new MC3KitException(
						format("Could not restore DEMCProposalStep state from database (chain %d).",
								chain.getChainId()), e);
			}
		}
		else {
			sums = new double[varNames.size()];
			sumSqs = new double[varNames.size()];
			
			// Create managers for each block size.
			// Block sizes are minBlockSize, 2*minBlockSize, 4*minBlockSize, ...
			int blockSize = step.minBlockSize;
			blockSizeManagers = new ArrayList<DEMCBlockSizeManager>();
			while(blockSize <= step.maxBlockSize
					&& blockSize <= varNames.size()) {
				if(model.getLogger().isLoggable(Level.FINE)) {
					model.getLogger()
							.fine(format(
									"Creating block size manager for block size = %d",
									blockSize));
				}
				blockSizeManagers
						.add(new DEMCBlockSizeManager(this, blockSize));
				blockSize *= 2;
			}
		}
	}
	
	private void recordState(Chain chain) throws MC3KitException {
		long iteration = chain.getIteration();
		
		if(iteration % chain.getMCMC().getThin() == 0
				&& iteration > step.recordHistoryAfter) {
			try {
				ISqlJetTable table = chain.getDb()
						.getTable(step.getTableName());
//				ISqlJetCursor c = table.lookup(step.getIndexName(), iteration);
//				while(!c.eof()) {
//					c.delete();
//				}
				if(iteration > step.recordHistoryAfter) {
					Model model = chain.getModel();
					
					double[] values = new double[varNames.size()];
					for(int i = 0; i < values.length; i++) {
						values[i] = model.getDoubleVariable(varNames.get(i))
								.getValue();
						sums[i] += values[i];
						sumSqs[i] += values[i] * values[i];
					}
					table.insert(iteration, toBytes(values), toBytes(sums),
							toBytes(sumSqs), getJsonState(chain));
				}
			}
			catch(SqlJetException e) {
				throw new MC3KitException(format(
						"Error recording history on chain %d, iteration %d",
						chain.getChainId(), iteration), e);
			}
		}
	}
	
	private String getJsonState(Chain chain) {
		Map<String, Object> jsonObj = new HashMap<>();
		jsonObj.put("blockSizeManagers", blockSizeManagers);
		return chain.getGson().toJson(jsonObj);
	}
	
	DoubleMatrix1D[] getRandomSamples(Chain chain, RandomEngine rng, int count)
			throws MC3KitException {
//		long iteration = chain.getIteration();
//		long thin = chain.getMCMC().getThin();
		
		Uniform unif = new Uniform(rng);
		
		long[] iterations = new long[count];
		DoubleMatrix1D[] samples = new DoubleMatrix1D[count];
		
		long historyCount = getHistoryCount(chain);
		
		for(int i = 0; i < count; i++) {
			boolean done = false;
			while(!done) {
				iterations[i] = unif.nextLongFromTo(0, historyCount - 1);
				done = true;
				for(int j = 0; j < i; j++) {
					if(iterations[i] == iterations[j]) {
						done = false;
						break;
					}
				}
			}
			samples[i] = getSample(chain, iterations[i]);
		}
		
		return samples;
	}
	
	private DoubleMatrix1D getSample(Chain chain, long rowId)
			throws MC3KitException {
		try {
			SqlJetDb db = chain.getDb();
			ISqlJetTable table = db.getTable(step.getTableName());
//			ISqlJetCursor c = table.lookup(step.getIndexName(), iteration);
			ISqlJetCursor c = table.open();
			c.goTo(rowId);
			double[] values = fromBytes(c.getBlobAsArray("sample"));
			
			return new DenseDoubleMatrix1D(values);
		}
		catch(SqlJetException e) {
			throw new MC3KitException(e);
		}
	}
}
