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

package mc3kit.model;

import java.util.*;
import java.util.logging.Logger;

import com.google.gson.Gson;

import cern.jet.random.engine.RandomEngine;
import mc3kit.MC3KitException;
import mc3kit.graph.*;
import mc3kit.mcmc.Chain;
import mc3kit.types.doublevalue.DoubleValued;

public abstract class ModelNode extends Node {
	Model model;
	
	Map<String, Object> properties;
	
	protected ModelNode() {
		properties = new HashMap<>();
	}
	
	protected ModelNode(String name) {
		super(name);
		properties = new HashMap<>();
	}
	
	public boolean update() throws MC3KitException {
		return true;
	}
	
	public boolean update(Set<ModelEdge> fromEdges) throws MC3KitException {
		return update();
	}
	
	public boolean updateAfterRejection() throws MC3KitException {
		return update();
	}
	
	public boolean updateAfterRejection(Set<ModelEdge> fromEdges)
			throws MC3KitException {
		return update(fromEdges);
	}
	
	public Model getModel() {
		return model;
	}
	
	protected ModelEdge updateEdge(ModelEdge edge, ModelNode headNode)
			throws MC3KitException {
		if(edge != null) {
			if(edge.getHead() == headNode) {
				return edge;
			}
			getModel().removeEdge(edge);
		}
		
		if(headNode == null) {
			return null;
		}
		
		edge = getModel().addEdge(this, headNode);
		return edge;
	}
	
	protected IndexedEdge updateEdge(IndexedEdge edge, int index, ModelNode headNode) throws MC3KitException {
		if(edge != null) {
			if(edge.getHead() == headNode) {
				return edge;
			}
			getModel().removeEdge(edge);
		}
		
		if(headNode == null) {
			return null;
		}
		
		edge = getModel().addEdge(index, this, headNode);
		return edge;
	}
	
	protected double getDoubleValue(ModelEdge edge) {
		return ((DoubleValued) edge.getHead()).getValue();
	}
	
	public Chain getChain() {
		return model.getChain();
	}
	
	public RandomEngine getRng() {
		return getChain().getRng();
	}
	
	public Logger getLogger() {
		return model.getLogger();
	}
	
	protected Gson getGson() {
		return getModel().getGson();
	}
	
	public void setProperty(String property, Object value) {
		properties.put(property, value);
	}
	
	public Object getProperty(String property) {
		return properties.get(property);
	}
	
	public Object getProperty(String property, Object defaultValue) {
		Object value = properties.get(property);
		return value == null ? defaultValue : value;
	}
}
