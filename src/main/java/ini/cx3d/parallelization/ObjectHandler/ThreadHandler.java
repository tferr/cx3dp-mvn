/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.ObjectHandler;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.commands.CommandManager;

import java.util.ArrayList;


public class ThreadHandler {


	public static int complexWorkercount= Runtime.getRuntime().availableProcessors();
	public static int simpleWorkercount = 1;

	private static ArrayList<ComplexWorker> complexWorkers  = new ArrayList<ComplexWorker>();
	private static ArrayList<SimpleWorker> simpleWorkers  = new ArrayList<SimpleWorker>();

	public static void init()
	{
		for(int i = 0;i<complexWorkercount;i++)
		{
			introduceNewComplexCommandWorker(i);
		}
		System.out.println("ComplexWorkers"+complexWorkercount);
		for(int i = 0;i<simpleWorkercount;i++)
		{
			introduceNewSimpleCommandWorker(i);
		}
		System.out.println("SimpleWorkers"+simpleWorkercount);
	}

	public static ComplexWorker introduceNewComplexCommandWorker(int i)
	{
		if(complexWorkers.size()<complexWorkercount*2)
		{
			
			ComplexWorker w = new ComplexWorker(i);
			Thread t = new Thread(w);
			complexWorkers.add(w);
			t.setName("ComplexWorker: "+complexWorkers.size());
			t.start();
			//t.setPriority(2);
			return w;
		}
		return null;
	}

	public static Worker introduceNewSimpleCommandWorker(int i)
	{
		if(simpleWorkers.size()<simpleWorkercount*2)
		{

			SimpleWorker w = new SimpleWorker(i);
			Thread t = new Thread(w);
			simpleWorkers.add(w);
			t.setName("SimpleWorker: "+simpleWorkers.size());
			t.start();
			//t.setPriority(2);
			return w;
		}
		return null;
	}

	static void workerFinished(ComplexWorker w)
	{
		complexWorkers.remove(w);
	}

	static void workerFinished(Worker w)
	{
		simpleWorkers.remove(w);
	}

	public static void EmergencyBreak()
	{
		OutD.println("ThreadHandler.EmergencyBreak()");
		for (Worker w : simpleWorkers) {
			w.quit();
		}
		for (ComplexWorker w : complexWorkers) {
			w.quit();
		}
		CommandManager.debug_show_Contents();
	}

	public static void WaitForStoping()
	{
		while(tee_break){Thread.yield();}
		while(!CommandManager.allExecuted())
		{
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private static boolean tee_break = false;
	public static synchronized void togglePause()
	{
		tee_break = !tee_break;
	}
	public static void setPause(boolean val) {
		tee_break=val;

	}
}


