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

package mc3kit;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

import org.tmatesoft.sqljet.core.*;
import org.tmatesoft.sqljet.core.table.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	
	Chain(MCMC mcmc, int chainId, double priorHeatExponent, double likelihoodHeatExponent, RandomEngine rng) {
		this.mcmc = mcmc;
		this.chainId = chainId;
		this.priorHeatExponent = priorHeatExponent;
		this.likelihoodHeatExponent = likelihoodHeatExponent;
		this.rng = rng;
		logger = mcmc.getLogger("mc3kit.Chain." + chainId);
    if(chainId != 0 && !mcmc.logAllChains) {
      logger.setLevel(Level.OFF);
    }
	}
	
	void initialize() throws MC3KitException {
	  if(initialized) {
	    return;
	  }
	  initialized = true;
	  
	  Path dbPath = mcmc.getDbPath().resolve(Paths.get("chains", format("%d.sqlite", chainId)));
	  File dbFile = dbPath.toFile();
	  
	  // TODO: load from database if supposed to
	  
	  // Set up initial chain state
	  if(dbFile.exists()) {
	    throw new MC3KitException(format("File %s already exists", dbPath));
	  }
	  
	  try {
	    db = new SqlJetDb(dbFile, true);
      db.open();
      db.beginTransaction(SqlJetTransactionMode.WRITE);

      // Populate parameters table and record var id
      paramIdNameMap = new HashMap<Integer, String>();
      paramNameIdMap = new HashMap<String, Integer>();
      
      db.createTable("CREATE TABLE parameters (pid INTEGER PRIMARY KEY, pname TEXT)");
      ISqlJetTable pTable = db.getTable("parameters");
      
      int pid = 1;
      paramIdNameMap.put(pid++, "logPrior");
      paramIdNameMap.put(pid++, "logLikelihood");
      for(Variable var : getModel().getUnobservedVariables()) {
        paramIdNameMap.put(pid++, var.getName());
      }
      
      for(Map.Entry<Integer, String> entry : paramIdNameMap.entrySet()) {
        pid = entry.getKey();
        String pname = entry.getValue();
        paramNameIdMap.put(pname, pid);
        pTable.insert(pid, pname);
      }
      
      // Create empty samples table
      db.createTable(
        "CREATE TABLE samples (iteration INTEGER, pid INTEGER, value TEXT)"
      );
      db.commit();
    }
    catch(SqlJetException e) {
      throw new MC3KitException(format("SqlJetException opening %s", dbFile), e);
    }
    
    gson = new GsonBuilder().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
	  
	  
	  iteration = 0; 
	}
	
	void writeToDb() throws MC3KitException {
	  try {
  	  db.beginTransaction(SqlJetTransactionMode.WRITE);
  	  
  	  ISqlJetTable table = db.getTable("samples");
  	  table.insert(iteration, paramNameIdMap.get("logPrior"), model.getLogPrior());
  	  table.insert(iteration, paramNameIdMap.get("logLikelihood"), model.getLogLikelihood());
  	  
  	  for(Map.Entry<String, String> entry : model.makeDbSample(gson).entrySet()) {
  	    table.insert(iteration, paramNameIdMap.get(entry.getKey()), entry.getValue());
  	  }
  	  
  	  db.commit();
	  }
	  catch(SqlJetException e) {
	    throw new MC3KitException(format("SqlJetException writing sample %d on chain %d.", iteration, chainId), e);
	  }
	}
	
	public MCMC getMCMC()
	{
		return mcmc;
	}
  
  public void setModel(Model model)
  {
    this.model = model;
  }
	
	public Model getModel()
	{
		return model;
	}
	
	public RandomEngine getRng()
	{
		return rng;
	}
	
	public int getChainId()
	{
		return chainId;
	}
	
	public int getChainCount()
	{
		return mcmc.chainCount;
	}
	
	public double getPriorHeatExponent()
	{
		return priorHeatExponent;
	}
	
	public double getLikelihoodHeatExponent()
	{
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
}
