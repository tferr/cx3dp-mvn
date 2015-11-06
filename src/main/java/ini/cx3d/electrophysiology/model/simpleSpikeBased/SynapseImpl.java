package ini.cx3d.electrophysiology.model.simpleSpikeBased;

import ini.cx3d.electrophysiology.ElectroPhysiolgySynapse;
import ini.cx3d.electrophysiology.model.SynapseELModel;
import ini.cx3d.electrophysiology.model.Token;

import java.util.ArrayList;

public class SynapseImpl implements SynapseELModel{

	private ElectroPhysiolgySynapse o;
	private ArrayList<Token> future;
	public double weight = 1;
	private double decay;
	private double rise;
	
	public double tauDecay;
	public double tauRise;
	
	
	@Override
	public void process() {
		double deltaVoltage = 0;
		for(Token t: o.getAxon().getTokens())
		{
			TokenImpl timpl = (TokenImpl) t;
			deltaVoltage += timpl.voltage;
		}
		
		deltaVoltage *= weight;
		
		decay *=Math.exp(-0.0001/tauDecay);
		rise *=Math.exp(-0.0001/tauRise);
		
		decay +=deltaVoltage;
		rise += deltaVoltage;
		
		future = new ArrayList<Token>();
		
		TokenImpl t = new TokenImpl();
		t.voltage = (decay-rise);
		
		future.add(t);	
	}

	@Override
	public SynapseELModel getI() {
		// TODO Auto-generated method stub
		return new SynapseImpl();
	}

	@Override
	public void setELObject(ElectroPhysiolgySynapse o) {
		// TODO Auto-generated method stub
		this.o=o;
	}

	@Override
	public ArrayList<Token> getCalculations() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
}
