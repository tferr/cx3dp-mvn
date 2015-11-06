package ini.cx3d.biology;

import ini.cx3d.electrophysiology.model.SynapseELModel;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalSphere;



public abstract class AbstractLocalBiologySynapseModule extends AbstractLocalBiologyModule{

	protected Synapse synapse; // "protected" so subclasses can access it
	
	public Synapse getSynapse() {return synapse;}

	public void setCellElement(Synapse synapse) {
		this.synapse = synapse;
	}
	
	public CellElement getPre()
	{
		if(synapse.getPhysicalBond().getFirstPhysicalObject() instanceof PhysicalCylinder )
		{
			NeuriteElement a =  ((PhysicalCylinder) synapse.getPhysicalBond().getFirstPhysicalObject()).getNeuriteElement();
			if(a.isAnAxon())
			{
				return a;
			}
		}
		else
		{
			SomaElement a = ((PhysicalSphere) synapse.getPhysicalBond().getFirstPhysicalObject()).getSomaElement();
			return a;
		}
		
		if(synapse.getPhysicalBond().getSecondPhysicalObject() instanceof PhysicalCylinder )
		{
			NeuriteElement a =  ((PhysicalCylinder) synapse.getPhysicalBond().getSecondPhysicalObject()).getNeuriteElement();
			if(a.isAnAxon())
			{
				return a;
			}
		}
		else
		{
			SomaElement a = ((PhysicalSphere) synapse.getPhysicalBond().getSecondPhysicalObject()).getSomaElement();
			return a;
		}
		return null; 
	}
	
	public CellElement getPost()
	{
		if(synapse.getPhysicalBond().getFirstPhysicalObject() instanceof PhysicalCylinder )
		{
			NeuriteElement a =  ((PhysicalCylinder) synapse.getPhysicalBond().getFirstPhysicalObject()).getNeuriteElement();
			if(!a.isAnAxon())
			{
				return a;
			}
		}
		
		if(synapse.getPhysicalBond().getSecondPhysicalObject() instanceof PhysicalCylinder )
		{
			NeuriteElement a =  ((PhysicalCylinder) synapse.getPhysicalBond().getSecondPhysicalObject()).getNeuriteElement();
			if(!a.isAnAxon())
			{
				return a;
			}
		}
		return null; 
	}
	public abstract void run();
	
	public SynapseELModel getELModel()
	{
		return synapse.getPhysicalBond().getElectroPhysiolgy().getModel();
	}
}
