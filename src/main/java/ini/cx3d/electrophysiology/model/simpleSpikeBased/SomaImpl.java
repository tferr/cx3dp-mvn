package ini.cx3d.electrophysiology.model.simpleSpikeBased;

import ini.cx3d.electrophysiology.ElectroPhysiolgySoma;
import ini.cx3d.electrophysiology.ElectroPhysiologySheduler;
import ini.cx3d.electrophysiology.model.SomaELModel;
import ini.cx3d.electrophysiology.model.Token;
import ini.cx3d.physics.PhysicalBond;
import ini.cx3d.physics.PhysicalCylinder;

import java.util.ArrayList;

public class SomaImpl implements SomaELModel{

	
	public double restingPotential = -70;
	public double resetPotential = -70;
	public double threshold = -50;
	public double voltage = -70;
	public double MemebraneTimeconstant = 0.01;
	public double refactoryTimeconstant = 0.003;
	public double peekcurrent = 0;
	
	private ArrayList<Token> future;
	private ElectroPhysiolgySoma o;
	private double reftime = 1;
	public double SponteniousFiringRate = 1;
	public double GaussianVariance = 1;
	public ArrayList<Double> spiketimes = new ArrayList<Double>();
	
	
	@Override
	public void process() {
		
		future = new ArrayList<Token>();
		if(refactoryTimeconstant>=reftime)
		{
			reftime+=0.001;
			return;
		}
		double decay = (voltage-restingPotential)*MemebraneTimeconstant;
		voltage -=decay;
		
		voltage +=(threshold-resetPotential)*SponteniousFiringRate/1000;
		
		for(PhysicalBond b: o.getPhysical().getPhysicalBonds())
		{
			if(b.getElectroPhysiolgy()!=null)
			{
				
				for(Token t:b.getElectroPhysiolgy().getTokens())
				{
					TokenImpl timpl = (TokenImpl) t;
					voltage+=timpl.voltage;
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
					voltage+=timpl.voltage;
				}
			}
		}
		
		if(voltage>threshold)
		{
			reftime = 0;
			voltage = resetPotential;
			spiketimes.add(ElectroPhysiologySheduler.currentTime);
			for(PhysicalCylinder m : o.getPhysical().getDaughters())
			{
				if(m.getNeuriteElement().isAnAxon())
				{
					TokenImpl timpl = new TokenImpl();
					timpl.voltage = peekcurrent;
					future.add(timpl);
				}
			}
		}
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
		spiketimes = new ArrayList<Double>();
		
	}
}
