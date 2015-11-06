package ini.cx3d.electrophysiology.model.simpleSpikeBased;

import ini.cx3d.electrophysiology.ElectroPhysiolgyAxon;
import ini.cx3d.electrophysiology.model.AxonELModel;
import ini.cx3d.electrophysiology.model.Token;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalSphere;

import java.util.ArrayList;

public class AxonImpl implements AxonELModel {
	
	private ElectroPhysiolgyAxon o;
	private ArrayList<Token> future;

	@Override
	public void process() {
		
		future = new ArrayList<Token>();
		if(o.getPhysical().getMother() instanceof PhysicalSphere)
		{
			PhysicalSphere s = (PhysicalSphere)o.getPhysical().getMother();
			for(Token t :s.getElectroPhysiolgy().getTokens())
			{
				future.add(t);
			}
		}
		else
		{
			PhysicalCylinder s = (PhysicalCylinder)o.getPhysical().getMother();
			for(Token t :s.getElectroPhysiolgy().getTokens())
			{
				future.add(t);
			}
		}
		
	}

	@Override
	public AxonELModel getI() {
		// TODO Auto-generated method stub
		return new AxonImpl();
	}

	@Override
	public void setELObject(ElectroPhysiolgyAxon o) {
		// TODO Auto-generated method stub
		this.o=o;
	}

	@Override
	public ArrayList<Token> getCalculations() {
		// TODO Auto-generated method stub
		return future;
	}
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
}
