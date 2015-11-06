package ini.cx3d.electrophysiology;

import ini.cx3d.electrophysiology.model.AxonELModel;
import ini.cx3d.electrophysiology.model.ModelFactory;
import ini.cx3d.physics.PhysicalBond;

public class ElectroPhysiolgyAxon extends ElectroPhysiolgyNeurite{
	AxonELModel model = ModelFactory.axonModel.getI();

	@Override
	public void process() {
		model.setELObject(this);
		model.process();
		for (PhysicalBond b : super.cyl.getPhysicalBonds()) {
			if(b.getElectroPhysiolgy()!=null)
			{
				b.getElectroPhysiolgy().process();
			}
		}
		
	}



	@Override
	public void applyCalculations() {
		this.currentTokens = model.getCalculations();
		for (PhysicalBond b : super.cyl.getPhysicalBonds()) {
			if(b.getElectroPhysiolgy()!=null)
			{
				b.elSynapse.currentTokens = b.elSynapse.getModel().getCalculations();
			}
		}
	}

	@Override
	public void installLocally() {
		ElectroPhysiologyManager.I().add(this);
		
	}
	
	

	@Override
	public void removeLocally() {
		ElectroPhysiologyManager.I().remove(this);
		
	}
	
	public AxonELModel getModel()
	{
		return model;
	}
	
	public void reset()
	{
		super.reset();
		model.reset();
	}

}
