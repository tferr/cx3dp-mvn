package ini.cx3d.electrophysiology.model;

import ini.cx3d.electrophysiology.ElectroPhysiolgySynapse;



public interface SynapseELModel extends Processable{
	public SynapseELModel getI();
	public void setELObject(ElectroPhysiolgySynapse o);
}
