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

package mc3kit.mcmc;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

import static java.lang.String.format;

import mc3kit.MC3KitException;
import mc3kit.model.Model;
import mc3kit.model.Variable;

import org.tmatesoft.sqljet.core.*;
import org.tmatesoft.sqljet.core.table.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

public class Chain {
	
	MCMC mcmc;
	int chainId;
	double priorHeatExponent;
	double likelihoodHeatExponent;
	RandomEngine rng;
	
	boolean initialized;
	long iteration;
	
	Model model;
	Logger logger;
	
	SqlJetDb db;
	Gson gson;
	
	Map<Integer, String> paramIdNameMap;
	Map<String, Integer> paramNameIdMap;
	Map<String, Double> sumMap;
	Map<String, Double> sumSqMap;
	
	Chain(MCMC mcmc, int chainId, double priorHeatExponent,
			double likelihoodHeatExponent, RandomEngine rng)
			throws MC3KitException {
		this.mcmc = mcmc;
		this.chainId = chainId;
		this.priorHeatExponent = priorHeatExponent;
		this.likelihoodHeatExponent = likelihoodHeatExponent;
		this.rng = rng;
		logger = mcmc.getLogger("mc3kit.Chain." + chainId);
		if(chainId != 0 && !mcmc.logAllChains) {
			logger.setLevel(Level.OFF);
		}
		
		File dbFile = null;
		Path dbDir = mcmc.getDbPath();
		if(dbDir != null) {
			Path dbPath = mcmc.getDbPath().resolve(
					Paths.get(format("%d.sqlite", chainId)));
			dbFile = dbPath.toFile();
		}
		
		if(dbFile == null) {
			db = new SqlJetDb(SqlJetDb.IN_MEMORY, true);
		}
		else {
			if(mcmc.shouldRestore()) {
				if(!dbFile.exists()) {
					throw new MC3KitException(
							format("Database file does not exist; turn off restore mode to start from scratch",
									dbFile));
				}
			}
			else {
				if(dbFile.exists()) {
					throw new MC3KitException(
							format("File %s already exists; set restore mode to start from existing database",
									dbFile));
				}
			}
			db = new SqlJetDb(dbFile, true);
		}
		
		try {
			db.open();
		}
		catch(SqlJetException e) {
			throw new MC3KitException("Could not open database.", e);
		}
	}
	
	void initialize() throws MC3KitException {
		if(initialized) {
			return;
		}
		initialized = true;
		
		gson = new GsonBuilder().disableHtmlEscaping()
				.serializeSpecialFloatingPointValues().create();
		
		initializeDatabase();
	}
	
	private void initializeDatabase() throws MC3KitException {
		boolean shouldRestore = mcmc.shouldRestore();
		
		try {
			if(shouldRestore) {
				db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			}
			else {
				db.beginTransaction(SqlJetTransactionMode.WRITE);
			}
			
			// Populate parameters table and record var id
			paramIdNameMap = new HashMap<Integer, String>();
			paramNameIdMap = new HashMap<String, Integer>();
			
			// Initialize model from database or from scratch
			if(shouldRestore) {
				ISqlJetTable pTable = db.getTable("parameters");
				ISqlJetCursor c = pTable.open();
				if(c.getRowCount() < 3) {
					throw new MC3KitException("parameter table too small");
				}
				do {
					int pid = (int) c.getInteger("pid");
					String pname = c.getString("pname");
					if(pid == 1) {
						assert pname.equals("logPrior");
					}
					else if(pid == 2) {
						assert pname.equals("logLikelihood");
					}
					else if(pid == 3) {
						assert pname.equals("rngState");
					}
					paramIdNameMap.put(pid, pname);
					paramNameIdMap.put(pname, pid);
					
				} while(c.next());
				
				iteration = mcmc.getLastSavedIteration();
				model = mcmc.modelFactory.createModel(this,
						getSample(iteration));
				
				// Restore RNG state
				ISqlJetTable sTable = db.getTable("samples");
				rng = gson.fromJson(
						sTable.lookup("iterationPidIndex", iteration, 3)
								.getString("value"), MersenneTwister.class);
				
				// Verify parameter names actually those in database
				int pid = 4;
				for(Variable var : model.getUnobservedVariables()) {
					String varName = var.getName();
					int dbPid = paramNameIdMap.get(varName);
					if(dbPid != pid) {
						throw new MC3KitException(
								format("Parameter %s should have pid %d (was %d in db)",
										varName, pid, dbPid));
					}
					pid++;
				}
			}
			else {
				model = mcmc.modelFactory.createModel(this);
				
				db.createTable("CREATE TABLE parameters (pid INTEGER PRIMARY KEY, pname TEXT)");
				ISqlJetTable pTable = db.getTable("parameters");
				
				int pid = 1;
				paramIdNameMap.put(pid++, "logPrior");
				paramIdNameMap.put(pid++, "logLikelihood");
				paramIdNameMap.put(pid++, "rngState");
				for(Variable var : model.getUnobservedVariables()) {
					paramIdNameMap.put(pid++, var.getName());
				}
				
				for(Map.Entry<Integer, String> entry : paramIdNameMap
						.entrySet()) {
					pid = entry.getKey();
					String pname = entry.getValue();
					paramNameIdMap.put(pname, pid);
					
					pTable.insert(pid, pname);
				}
				db.createTable("CREATE TABLE samples (iteration INTEGER, pid INTEGER, value)");
				db.createIndex("CREATE INDEX iterationIndex ON samples (iteration)");
				db.createIndex("CREATE INDEX iterationPidIndex ON samples (iteration, pid)");
				db.createIndex("CREATE INDEX pidIndex ON samples (pid)");
			}
			assert model != null;
			model.setChain(this);
			
			db.commit();
		}
		catch(SqlJetException e) {
			throw new MC3KitException("Error setting up/loading from database",
					e);
		}
	}
	
	private Map<String, Object> getSample(long iteration)
			throws MC3KitException {
		Map<String, Object> sample = new LinkedHashMap<>();
		try {
			ISqlJetTable table = db.getTable("samples");
			ISqlJetCursor c = table.lookup("iterationIndex", iteration);
			if(c.eof()) {
				throw new MC3KitException(format("No records for iteration %d",
						iteration));
			}
			do {
				sample.put(paramIdNameMap.get((int) c.getInteger("pid")),
						c.getValue("value"));
			} while(c.next());
		}
		catch(SqlJetException e) {
			throw new MC3KitException(format("Could not get iteration %d",
					iteration), e);
		}
		return sample;
	}
	
	public long getLastSavedIteration() throws MC3KitException {
		try {
			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			ISqlJetTable table = db.getTable("samples");
			ISqlJetCursor c = table.open();
			if(!c.last()) {
				throw new MC3KitException("Samples table has no rows");
			}
			long iteration = c.getInteger("iteration");
			db.commit();
			return iteration;
		}
		catch(SqlJetException e) {
			throw new MC3KitException(
					"Exception reading last saved iteration number", e);
		}
	}
	
	void beginIteration() throws MC3KitException {
		try {
			db.beginTransaction(SqlJetTransactionMode.WRITE);
		}
		catch(SqlJetException e) {
			throw new MC3KitException(
					"Error beginning database transaction to start iteration.",
					e);
		}
	}
	
	void endIteration() throws MC3KitException {
		try {
			db.commit();
			iteration++;
		}
		catch(SqlJetException e) {
			throw new MC3KitException(
					"Error committing database to end iteration.", e);
		}
	}
	
	void writeToDb() throws MC3KitException {
		try {
			ISqlJetTable table = db.getTable("samples");
			
			// If we're redoing a sample because chains finished at different
			// points before a restore,
			// delete the old sample
			ISqlJetCursor c = table.lookup("iterationIndex", iteration);
			while(!c.eof()) {
				c.delete();
			}
			
			table.insert(iteration, paramNameIdMap.get("logPrior"),
					model.getLogPrior());
			table.insert(iteration, paramNameIdMap.get("logLikelihood"),
					model.getLogLikelihood());
			table.insert(iteration, paramNameIdMap.get("rngState"),
					gson.toJson(rng));
			
			for(Map.Entry<String, Object> entry : model.toDbSample().entrySet()) {
				table.insert(iteration, paramNameIdMap.get(entry.getKey()),
						entry.getValue());
			}
		}
		catch(SqlJetException e) {
			throw new MC3KitException(format(
					"SqlJetException writing sample %d on chain %d.",
					iteration, chainId), e);
		}
	}
	
	public MCMC getMCMC() {
		return mcmc;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	public RandomEngine getRng() {
		return rng;
	}
	
	public int getChainId() {
		return chainId;
	}
	
	public int getChainCount() {
		return mcmc.chainCount;
	}
	
	public double getPriorHeatExponent() {
		return priorHeatExponent;
	}
	
	public double getLikelihoodHeatExponent() {
		return likelihoodHeatExponent;
	}
	
	public long getIteration() {
		return iteration;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public SqlJetDb getDb() {
		return db;
	}
	
	public Gson getGson() {
		return gson;
	}
}
