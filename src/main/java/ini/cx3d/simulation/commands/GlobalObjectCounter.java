package ini.cx3d.simulation.commands;

import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.simulation.SimulationState;

public class GlobalObjectCounter extends AbstractSimpleCommand<Boolean>{

	private int totalcount=0;

	@Override
	public boolean apply() {
		int localcount = SimulationState.getLocal().totalObjectCount;
		if(Hosts.getPrevHost() !=null)
		{
			GlobalObjectCounter c = new GlobalObjectCounter();
			c.totalcount = this.totalcount+localcount;
			c.remoteExecute(Hosts.getPrevHost());
		}
		else
		{
			GlobalObjectCounterSet s = new GlobalObjectCounterSet();
			s.tot = this.totalcount+localcount;
			s.apply();
		}
		
		return false;
	}

}

class GlobalObjectCounterSet extends AbstractSimpleCommand<Boolean>
{
	public int tot;
	private int hopcount =0;
	
	@Override
	public boolean apply() {
		
		SimulationState.totalGlobalObjectCount= tot;
		if(Hosts.getNextHost() !=null)
		{
			hopcount ++;
			remoteExecute(Hosts.getNextHost());
		}
		
		return false;
	}
	
}