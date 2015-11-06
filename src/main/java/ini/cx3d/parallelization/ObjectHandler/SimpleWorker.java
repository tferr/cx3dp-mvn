/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.ObjectHandler;

import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.parallelization.ObjectHandler.commands.CommandManager;

public class SimpleWorker extends Worker
{
	public SimpleWorker(int i) {
		super(i);
		// TODO Auto-generated constructor stub
	}

	private boolean quit = false;
	private Thread executer;
	
	
	public void quit()
	{
		quit = true;
		executer.interrupt();
	}
	
	public void run() {
		AbstractSimpleCommand<?> r= null;
		executer = Thread.currentThread();
		while(true)
		{		    
				
				if(quit)
				{
					ThreadHandler.workerFinished(this);
					return;
				}
				
				r = (AbstractSimpleCommand<?>)CommandManager.fetchSimpleCommand();
				boolean runagain= r.run();
				if(runagain) CommandManager.addCommandToQueue(r,true);
				else CommandManager.executedOne();
				Thread.yield();
			
		}
	}
	
	public Thread getTherad()
	{
		return executer;
	}

	
}
