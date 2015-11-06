/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.ObjectHandler.commands;

import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;

public class KillCommand extends AbstractSimpleCommand<Integer> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean apply() {
		System.err.println("EXITED CAUSE OF REQUEST FROM "+this.client +"  BOX ID"+this.getResponseMailboxID());
		ThreadHandler.EmergencyBreak();
		System.exit(0);
		return false;
	}
	

}
