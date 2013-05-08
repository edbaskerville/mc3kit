package mc3kit.step.univariate;

import static java.lang.String.format;

import java.util.*;
import java.util.logging.*;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.google.gson.*;

import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import mc3kit.*;
import mc3kit.mcmc.Chain;
import mc3kit.mcmc.Task;
import mc3kit.model.Model;
import mc3kit.model.Variable;
import static mc3kit.util.Math.*;
import static mc3kit.util.Utils.*;

class UnivariateProposalTask implements Task {
	UnivariateProposalStep step;
	
	boolean initialized;
	
	int chainId;
	VariableProposer[] proposers;
	
	/*** CONSTRUCTOR ***/
	
	public UnivariateProposalTask(UnivariateProposalStep step, int chainId) {
		this.step = step;
		this.chainId = chainId;
	}
	
	/*** TASK INTERFACE IMPLEMENTATION ***/
	
	@Override
	public int[] getChainIds() {
		return new int[] { chainId };
	}
	
	@Override
	public void step(Chain[] chains) throws MC3KitException {
		assert (chains.length == 1);
		
		Chain chain = chains[0];
		long iteration = chain.getIteration();
		Logger logger = chain.getLogger();
		
		initialize(chain);
		
		RandomEngine rng = chain.getRng();
		Uniform unif = new Uniform(rng);
		
		// Run all proposers in random order
		for(int i : getRandomPermutation(proposers.length, unif)) {
			proposers[i].step(chain.getModel());
		}
		
		// Write out acceptance rates
		if(iteration % step.tuneEvery == 0 && logger.isLoggable(Level.INFO)) {
			Map<String, Double> acceptanceRates = new LinkedHashMap<String, Double>();
			for(VariableProposer proposer : proposers) {
				acceptanceRates.put(proposer.getName(),
						proposer.getAcceptanceRate());
			}
			Map<String, Object> infoObj = makeMap("iteration", iteration,
					"chainId", chainId, "acceptanceRates", acceptanceRates);
			logger.log(Level.INFO, "UnivariateProposalStep acceptance rates",
					infoObj);
		}
		
		// If we're still in the tuning period, tune
		if((iteration <= step.tuneFor) && iteration % step.tuneEvery == 0) {
			for(VariableProposer proposer : proposers) {
				proposer.tune(step.targetAcceptanceRate);
				proposer.resetTuningPeriod();
			}
		}
		
		if(iteration % chain.getMCMC().getThin() == 0) {
			JsonArray stateJson = new JsonArray();
			for(VariableProposer vp : proposers) {
				stateJson.add(vp.toJsonObject());
			}
			try {
				ISqlJetTable table = chain.getDb()
						.getTable(step.getTableName());
				ISqlJetCursor c = table.lookup(step.getIndexName(), iteration);
				while(!c.eof()) {
					c.delete();
				}
				
				table.insert(iteration, stateJson.toString());
			}
			catch(SqlJetException e) {
				throw new MC3KitException(
						format("Could not write out univariate state (chain %d, iteration %d",
								chainId, iteration));
			}
		}
	}
	
	/*** PRIVATE METHODS ***/
	
	private void initialize(Chain chain) throws MC3KitException {
		if(initialized)
			return;
		initialized = true;
		
		Model model = chain.getModel();
		
		List<Variable> vars = model.getUnobservedVariables();
		proposers = new VariableProposer[vars.size()];
		for(int i = 0; i < vars.size(); i++) {
			proposers[i] = makeVariableProposer(model, vars.get(i).getName());
		}
		
		try {
			SqlJetDb db = chain.getDb();
			
			if(chain.getMCMC().shouldRestore()) {
				ISqlJetTable table = db.getTable(step.getTableName());
				ISqlJetCursor c = table.lookup(step.getIndexName(),
						chain.getIteration());
				
				JsonParser parser = new JsonParser();
				JsonArray stateJson = parser.parse(c.getString("state"))
						.getAsJsonArray();
				for(int i = 0; i < proposers.length; i++) {
					proposers[i].fromJsonObject(stateJson.get(i)
							.getAsJsonObject());
				}
			}
			else {
				db.createTable(format(
						"CREATE TABLE %s (iteration INTEGER, state TEXT)",
						step.getTableName()));
				db.createIndex(format("CREATE INDEX %s ON %s (iteration)",
						step.getIndexName(), step.getTableName()));
			}
		}
		catch(SqlJetException e) {
			throw new MC3KitException(
					format("Couldn't create or load univariate proposal table on chain %d",
							chain.getChainId()), e);
		}
	}
	
	private VariableProposer makeVariableProposer(Model model, String varName)
			throws MC3KitException {
		Variable var = model.getVariable(varName);
		
		if(var == null) {
			throw new MC3KitException(format("No variable named %s", varName));
		}
		
		return var.makeProposer();
	}
}