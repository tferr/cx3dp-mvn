package ini.cx3d.electrophysiology.model;

import ini.cx3d.electrophysiology.ElectroPhysiolgySoma;


public interface SomaELModel  extends Processable{
	public SomaELModel getI();
	public void setELObject(ElectroPhysiolgySoma o);
}
