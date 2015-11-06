package ini.cx3d.electrophysiology.model.simpleRateBased;

import ini.cx3d.electrophysiology.ElectroPhysiolgyDendrite;
import ini.cx3d.electrophysiology.model.DendriteELModel;
import ini.cx3d.electrophysiology.model.Token;
import ini.cx3d.physics.PhysicalBond;

import java.util.ArrayList;

public class DendriteImpl implements DendriteELModel{

	private ElectroPhysiolgyDendrite o;
	private ArrayList<Token> future;

	
	
	public DendriteImpl()
	{
		
	}
	
	@Override
	public void process() {
		future = new ArrayList<Token>();
		double rate = 0;
		for(PhysicalBond b: o.getPhysical().getPhysicalBonds())
		{
			if(b.getElectroPhysiolgy()!=null)
			{
				
				for(Token t:b.getElectroPhysiolgy().getTokens())
				{
					TokenImpl timpl = (TokenImpl) t;
					rate+=timpl.rateValue;
				}
			}
		}
		if(o.getPhysical().getDaughterLeft()!=null)
		{
			for(Token t :o.getPhysical().getDaughterLeft().getElectroPhysiolgy().getTokens())
			{
				TokenImpl timpl = (TokenImpl) t;
				rate +=timpl.rateValue;
			}
		}
		if(o.getPhysical().getDaughterRight()!=null)
		{
			for(Token t :o.getPhysical().getDaughterRight().getElectroPhysiolgy().getTokens())
			{
				TokenImpl timpl = (TokenImpl) t;
				rate +=timpl.rateValue;
			}
		}
		TokenImpl timpl = new TokenImpl();
		timpl.rateValue = rate;
		future.add(timpl);
		
	}

	

	@Override
	public DendriteELModel getI() {
		// TODO Auto-generated method stub
		return new DendriteImpl();
	}

	@Override
	public void setELObject(ElectroPhysiolgyDendrite o) {
		// TODO Auto-generated method stub
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
