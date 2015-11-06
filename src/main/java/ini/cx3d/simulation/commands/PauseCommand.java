/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.simulation.commands;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.parallelization.communication.SimpleResponse;

public class PauseCommand extends AbstractSimpleCommand<Boolean>{

	@Override
	public boolean apply() {
		//SpaceNodeFacade.pauselock.writeLock().lock();
		OutD.println("PauseCommand.apply()");
		send(new SimpleResponse<Boolean>(this.mailboxID,true));
		return false;
	}

}
