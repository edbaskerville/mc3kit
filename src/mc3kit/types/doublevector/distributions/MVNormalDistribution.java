package mc3kit.types.doublevector.distributions;

import cern.colt.matrix.DoubleMatrix1D;
import mc3kit.MC3KitException;
import mc3kit.model.*;
import mc3kit.step.univariate.VariableProposer;
import mc3kit.types.doublevector.DoubleVectorDistribution;

public class MVNormalDistribution extends DoubleVectorDistribution {
	ModelEdge mean;
	ModelEdge var;
	
	public MVNormalDistribution(Model model) {
		super(model);
	}
	
	public MVNormalDistribution(Model model, String name) {
		super(model, name);
	}
	
	@Override
	public VariableProposer makeVariableProposer(String varName) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean valueIsValid(DoubleMatrix1D value) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public double getLogP(Variable var) throws MC3KitException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void sample(Variable var) throws MC3KitException {
		// TODO Auto-generated method stub
		
	}
	
}
