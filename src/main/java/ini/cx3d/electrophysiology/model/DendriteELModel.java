package ini.cx3d.electrophysiology.model;

import ini.cx3d.electrophysiology.ElectroPhysiolgyDendrite;




public interface DendriteELModel extends Processable {
	public DendriteELModel getI();
	public void setELObject(ElectroPhysiolgyDendrite o);
}
