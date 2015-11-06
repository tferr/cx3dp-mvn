package ini.cx3d.electrophysiology.model;

import ini.cx3d.electrophysiology.ElectroPhysiolgyAxon;

public interface AxonELModel extends Processable{
	public AxonELModel getI();
	public void setELObject(ElectroPhysiolgyAxon o);
}
