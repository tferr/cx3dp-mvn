package ini.cx3d.simulation.commands;

import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.simulation.SimulationState;

public class SpreadStage extends AbstractSimpleCommand<Boolean>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3027596160438103731L;
	public SimulationState d;
	public SpreadStage(SimulationState d)
	{
		this.d = d;
	}
	@Override
	public boolean apply() {

		SimulationState.put(this.client,d);
		return false;
	}
	@Override
	public String toString()
	{
		return "spread stage";
	}

}
