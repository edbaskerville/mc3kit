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

package mc3kit.graph;

import java.util.*;

public class Node extends Observable {
	String name;
	Graph graph;
	
	public Node() {
	}
	
	public Node(String name) {
		this();
		this.name = name;
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public String getName() {
		return name;
	}
	
	public int getOrder() {
		return graph.getOrder(this);
	}
	
	public Set<Edge> getHeadEdges() {
		return graph.getHeadEdges(this);
	}
	
	public Set<Edge> getTailEdges() {
		return graph.getTailEdges(this);
	}
	
	@Override
	public String toString() {
		return name == null ? super.toString() : name;
	}
}
