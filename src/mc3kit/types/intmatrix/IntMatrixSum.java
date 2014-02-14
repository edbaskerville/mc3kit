package mc3kit.types.intmatrix;

import java.util.*;

import mc3kit.*;
import mc3kit.model.Model;
import mc3kit.model.ModelEdge;
import mc3kit.model.ModelNode;

public class IntMatrixSum extends IntMatrixFunction {
	Map<IntMatrixValued, Summand> summandMap;
	
	public IntMatrixSum(Model model, int rowCount, int columnCount) {
		super(model, rowCount, columnCount);
		summandMap = new HashMap<>();
	}
	
	public IntMatrixSum add(IntMatrixValued summandNode) throws MC3KitException {
		return add(summandNode, true);
	}
	
	public IntMatrixSum add(IntMatrixValued summandNode, boolean positive)
			throws MC3KitException {
		if(summandMap.containsKey(summandNode)) {
			throw new MC3KitException("Summand already present.");
		}
		
		ModelEdge edge = getModel().addEdge(this, (ModelNode) summandNode);
		Summand summand = new Summand(edge, positive);
		summandMap.put(summandNode, summand);
		
		return this;
	}
	
	public IntMatrixSum remove(IntMatrixValued summandNode)
			throws MC3KitException {
		if(!summandMap.containsKey(summandNode)) {
			throw new MC3KitException("Summand not present");
		}
		
		Summand summand = summandMap.get(summandNode);
		getModel().removeEdge(summand.edge);
		summandMap.remove(summandNode);
		
		return this;
	}
	
	@Override
	public boolean update() {
		for(int i = 0; i < rowCount; i++) {
			for(int j = 0; j < columnCount; j++) {
				value[i][j] = 0;
			}
		}
		
		for(Map.Entry<IntMatrixValued, Summand> entry : summandMap.entrySet()) {
			IntMatrixValued x = entry.getKey();
			Summand s = entry.getValue();
			for(int i = 0; i < rowCount; i++) {
				for(int j = 0; j < rowCount; j++) {
					value[i][j] += x.getValue(i, j) * (s.positive ? 1 : -1);
				}
			}
		}
		
		/*
		 * for(int i = 0; i < rowCount; i++) { for(int j = 0; j < rowCount; j++)
		 * { System.err.printf("%d\t", value[i][j]); } System.err.println(); }
		 */
		
		return true;
	}
	
	private class Summand {
		boolean positive;
		ModelEdge edge;
		
		Summand(ModelEdge edge, boolean positive) {
			this.positive = positive;
			this.edge = edge;
		}
	}
}
