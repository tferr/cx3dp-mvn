package ini.cx3d.electrophysiology;

import ini.cx3d.electrophysiology.model.SynapseELModel;
import ini.cx3d.electrophysiology.model.Token;
import ini.cx3d.physics.PhysicalBond;
import ini.cx3d.physics.PhysicalCylinder;

import java.util.ArrayList;

public class ElectroPhysiolgySynapse implements ElectorphysiologyCompartment {
	private PhysicalBond bond;
	private SynapseELModel model;
	protected ArrayList<Token> currentTokens = new ArrayList<Token>();
	
	


	@Override
	public void process() {
		getModel().setELObject(this);
		getModel().process();
		
	}


	


	@Override
	public void reset() {
		currentTokens = new ArrayList<Token>();
		model.reset();
	}




	@Override
	public void applyCalculations() {
		currentTokens = getModel().getCalculations();
		
	}
	
	@Override
	public ArrayList<Token> getTokens() {
		// TODO Auto-generated method stub
		return currentTokens;
	}
	
	@Override
	public void installLocally() {
		
		
	}

	@Override
	public void removeLocally() {
		
		
	}




	public void setModel(SynapseELModel model) {
		this.model = model;
	}




	public SynapseELModel getModel() {
		return model;
	}




	@Override
	public Long getParentID() {
		// TODO Auto-generated method stub
		return 0L;
	}
	
	public ElectroPhysiolgyAxon getAxon()
	{
		if(bond.getFirstPhysicalObject() instanceof PhysicalCylinder)
		{
			ElectroPhysiolgyNeurite n = ((PhysicalCylinder)bond.getFirstPhysicalObject()).getElectroPhysiolgy();
			if(n instanceof ElectroPhysiolgyAxon) return (ElectroPhysiolgyAxon) n; 
		}
		if(bond.getSecondPhysicalObject() instanceof PhysicalCylinder)
		{
			ElectroPhysiolgyNeurite n = ((PhysicalCylinder)bond.getSecondPhysicalObject()).getElectroPhysiolgy();
			if(n instanceof ElectroPhysiolgyAxon) return (ElectroPhysiolgyAxon) n; 
		}
		return null;
	}




	public void setParent(PhysicalBond b) {
		if(b.getElectroPhysiolgy() != this)
		{
			this.bond = b;
			b.setElectroPhysiolgy(this);
		}
	}
}
