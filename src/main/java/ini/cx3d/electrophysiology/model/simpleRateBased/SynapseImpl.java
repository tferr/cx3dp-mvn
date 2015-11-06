package ini.cx3d.electrophysiology.model.simpleRateBased;

import ini.cx3d.electrophysiology.ElectroPhysiolgySynapse;
import ini.cx3d.electrophysiology.model.SynapseELModel;
import ini.cx3d.electrophysiology.model.Token;

import java.util.ArrayList;

public class SynapseImpl implements SynapseELModel{

	private ElectroPhysiolgySynapse o;
	private ArrayList<Token> future;
	public double weight = 1;
	
	
	
	@Override
	public void process() {
		double value = 0;
		 //getVoltage from Axon!!!
		for(Token t: o.getAxon().getTokens())
		{
			TokenImpl timpl = (TokenImpl) t;
			value += timpl.rateValue;
		}
		value *= weight;
		
		future = new ArrayList<Token>();
		
		TokenImpl t = new TokenImpl();
		t.rateValue = value;
		
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
		return future;
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
}
