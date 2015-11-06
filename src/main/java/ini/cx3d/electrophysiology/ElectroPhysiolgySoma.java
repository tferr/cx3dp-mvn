package ini.cx3d.electrophysiology;

import ini.cx3d.electrophysiology.model.ModelFactory;
import ini.cx3d.electrophysiology.model.SomaELModel;
import ini.cx3d.electrophysiology.model.Token;
import ini.cx3d.physics.PhysicalSphere;

import java.util.ArrayList;

public class ElectroPhysiolgySoma implements ElectorphysiologyCompartment {
	private PhysicalSphere sp;
	SomaELModel model = ModelFactory.somaModel.getI();
	protected ArrayList<Token> currentTokens = new ArrayList<Token>();
	
	public void setParrent(PhysicalSphere sp)
	{
		if(sp.getElectroPhysiolgy() != this)
		{
			this.sp = sp;
			sp.setElectroPhysiolgy(this);
		}
	}
	
	
	
	
	public Long getParentID() {
		
		return sp.getSoNode().getID();
	}


	public SomaELModel getModel()
	{
		return model;
	}

	@Override
	public void process() {
		model.setELObject(this);
		model.process();
		
	}




	@Override
	public void reset() {
		currentTokens = new ArrayList<Token>();
		model.reset();
	}

	public PhysicalSphere getPhysical()
	{
		return sp;
	}


	@Override
	public void applyCalculations() {
		this.currentTokens = model.getCalculations();
		
	}
	
	@Override
	public ArrayList<Token> getTokens() {
		// TODO Auto-generated method stub
		return currentTokens;
	}
	
	@Override
	public void installLocally() {
		ElectroPhysiologyManager.I().add(this);
		
	}

	@Override
	public void removeLocally() {
		ElectroPhysiologyManager.I().remove(this);
		
	}
}
