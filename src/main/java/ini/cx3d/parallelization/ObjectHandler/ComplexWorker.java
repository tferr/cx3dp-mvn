/**
 * Cleaned by Andreas Hauri 01.06.2010
 */


package ini.cx3d.parallelization.ObjectHandler;

import ini.cx3d.parallelization.ObjectHandler.commands.Command;
import ini.cx3d.parallelization.ObjectHandler.commands.CommandManager;

public class ComplexWorker extends Worker
{
	
	public ComplexWorker(int i) {
		super(i);
		// TODO Auto-generated constructor stub
	}

	private volatile boolean quit = false;
	private Thread executer;
	public void quit()
	{
		quit = true;
	}
	public  void run() {
		Command r= null;
		executer = Thread.currentThread();
		String name = executer.getName();
		int i = 0;
		while(true)
		{
	
			if(quit)
			{
				ThreadHandler.workerFinished(this);
				return;
			}
	
			executer.setName(name+"fetching");
			r = CommandManager.fetchComplexCommand(this.number);
			executer.setName(name+"running ");
			boolean runagain= r.run();
			executer.setName(name);
			if(runagain) CommandManager.addCommandToQueue(r,true);
			else CommandManager.executedOne();

		}
	}

	public Thread getTherad()
	{
		return executer;
	}

}