package ini.cx3d.electrophysiology;

import ini.cx3d.electrophysiology.model.Token;

import java.io.Serializable;
import java.util.ArrayList;

public interface ElectorphysiologyCompartment  extends Serializable{
	public Long getParentID();
	public void process();
	public void reset();
	public void applyCalculations();
	public ArrayList<Token> getTokens();
	public void installLocally();
	public void removeLocally();
}
