package ini.cx3d.simulation.commands;

import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.parallelization.communication.SimpleResponse;
import ini.cx3d.simulation.SimulationState;

public class RequestStage extends AbstractSimpleCommand<SimulationState>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3027596160438103731L;

	public RequestStage()
	{

	}
	@Override
	public boolean apply() {

		send(new SimpleResponse<SimulationState>(mailboxID,SimulationState.getLocal()));
		return false;
	}
	@Override
	public String toString()
	{
		return "request stage";
	}

}
