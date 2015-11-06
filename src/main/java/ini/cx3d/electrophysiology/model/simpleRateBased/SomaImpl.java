package ini.cx3d.electrophysiology.model.simpleRateBased;

import ini.cx3d.electrophysiology.ElectroPhysiolgySoma;
import ini.cx3d.electrophysiology.model.SomaELModel;
import ini.cx3d.electrophysiology.model.Token;
import ini.cx3d.physics.PhysicalBond;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.simulation.ECM;

import java.util.ArrayList;

public class SomaImpl implements SomaELModel{

	

	public double threshold = 0;
	public double currentRate= 0;
	public double AnalogDecay = 0.01;
	public double maxrate = 250;
	public double gaussianNoise = 1;
	
	private ArrayList<Token> future;
	private ElectroPhysiolgySoma o;
	private double restingvalue=0;

	@Override
	public void process() {
		
		future = new ArrayList<Token>();
		double decay = (currentRate-restingvalue)*AnalogDecay;
		currentRate -=decay;
		currentRate += (ECM.getRandomDouble()-0.5)*gaussianNoise;
		for(PhysicalBond b: o.getPhysical().getPhysicalBonds())
		{
			if(b.getElectroPhysiolgy()!=null)
			{
				
				for(Token t:b.getElectroPhysiolgy().getTokens())
				{
					TokenImpl timpl = (TokenImpl) t;
					currentRate=Math.max(currentRate,timpl.rateValue);
				}
			}
		}
		for(PhysicalCylinder m : o.getPhysical().getDaughters())
		{
			if(!m.getNeuriteElement().isAnAxon())
			{
				for(Token t : m.getElectroPhysiolgy().getTokens())
				{
					TokenImpl timpl = (TokenImpl) t;
					currentRate=Math.max(currentRate,timpl.rateValue);
				}
			}
		}
		
		if(currentRate<0)
		{
			currentRate = 0;
		}
		if(currentRate>maxrate)
		{
			currentRate = maxrate;
		}
		
		if(currentRate>threshold)
		{
			TokenImpl timpl = new TokenImpl();
			timpl.rateValue = currentRate;
			future.add(timpl);
		}
		else
		{
			TokenImpl timpl = new TokenImpl();
			timpl.rateValue = 0;
			future.add(timpl);
		}
		
	}

	private void calcVoltage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SomaELModel getI() {
		return new SomaImpl();
	}

	@Override
	public void setELObject(ElectroPhysiolgySoma o) {
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
