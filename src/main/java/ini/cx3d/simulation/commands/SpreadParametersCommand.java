
/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.simulation.commands;

import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.simulation.MultiThreadScheduler;

public class SpreadParametersCommand extends AbstractSimpleCommand<Boolean>
{
	private int gridresolution = 40;
	private int maxnodesperPM = 1000;
	private int globalmin=5000;


	/* if false, the physics is not computed......*/
	private boolean runPhyics = true;
	private boolean runDiffusion = true;

	public SpreadParametersCommand()
	{
		
		gridresolution = MultiThreadScheduler.gridresolution;
		maxnodesperPM = MultiThreadScheduler.maxnodesperPM;
		globalmin = MultiThreadScheduler.globalmin;
		runPhyics = MultiThreadScheduler.runPhyics;
		runDiffusion = MultiThreadScheduler.runExtracellularDiffusion;
	}
	@Override
	public boolean apply() {
		
		MultiThreadScheduler.gridresolution = gridresolution;
		MultiThreadScheduler.maxnodesperPM = maxnodesperPM;
		MultiThreadScheduler.globalmin= globalmin;
		MultiThreadScheduler.runPhyics = runPhyics;
		MultiThreadScheduler.runExtracellularDiffusion= runDiffusion;
		return false;
	}
	
}

