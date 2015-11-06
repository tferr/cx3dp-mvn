package ini.cx3d.electrophysiology;

import ini.cx3d.electrophysiology.model.Token;
import ini.cx3d.physics.PhysicalCylinder;

import java.util.ArrayList;

public abstract class  ElectroPhysiolgyNeurite implements ElectorphysiologyCompartment{
	protected PhysicalCylinder cyl;
	protected ArrayList<Token> currentTokens = new ArrayList<Token>();
	
	@Override
	public ArrayList<Token> getTokens() {
		// TODO Auto-generated method stub
		return currentTokens;
	}
	
	public void setParrent(PhysicalCylinder cyl)
	{
		if(cyl.getElectroPhysiolgy() != this)
		{
			this.cyl = cyl;
			cyl.setElectroPhysiolgy(this);
		}
	}
	
	public PhysicalCylinder getPhysical()
	{
		return cyl;
	}
	
	
	public Long getParentID() {
		
		return cyl.getSoNode().getID();
	}
	
	public void reset ()
	{
		currentTokens = new ArrayList<Token>();
	}
	
}
