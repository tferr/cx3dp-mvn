package ini.cx3d.electrophysiology.model;


import java.io.Serializable;
import java.util.ArrayList;

public interface Processable extends Serializable {
	public void process();
	public ArrayList<Token> getCalculations();
	public void reset();
}
