package ini.cx3d.electrophysiology.model;

import ini.cx3d.physics.PhysicalObject;

public abstract class SynapseFactory {
	
	public abstract SynapseELModel getSynapseModel(PhysicalObject o1, PhysicalObject o2);
	
}
