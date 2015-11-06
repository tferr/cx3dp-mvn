package ini.cx3d.biology.synapse2;

import ini.cx3d.biology.Synapse;
import ini.cx3d.physics.PhysicalObject;

import java.io.Serializable;


public abstract class Excrescence implements Serializable{
	
	PhysicalObject po;
	
	public abstract Synapse synapseWith(Excrescence otherExcrescence);
	
	
	public PhysicalObject getPo() {
		return po;
	}
	public void setPo(PhysicalObject po) {
		this.po = po;
	}
	
	/** returns the absolute coord of the point where this element is attached on the PO.*/
	public double[] getPosition(){
		return po.getMassLocation();
	}
	
}
