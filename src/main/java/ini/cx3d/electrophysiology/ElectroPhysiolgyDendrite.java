package ini.cx3d.electrophysiology;

import ini.cx3d.electrophysiology.model.DendriteELModel;
import ini.cx3d.electrophysiology.model.ModelFactory;

public class ElectroPhysiolgyDendrite extends ElectroPhysiolgyNeurite{
	DendriteELModel model = ModelFactory.dentriteModel.getI();
	@Override
	public void process() {
		model.setELObject(this);
		model.process();
		
	}


	@Override
	public void applyCalculations() {
		this.currentTokens = model.getCalculations();
	}

	@Override
	public void installLocally() {
		ElectroPhysiologyManager.I().add(this);
		
	}

	@Override
	public void removeLocally() {
		ElectroPhysiologyManager.I().remove(this);
		
	}
	
	public DendriteELModel getModel()
	{
		return model;
	}
	
	public void reset()
	{
		super.reset();
		model.reset();
	}
}
