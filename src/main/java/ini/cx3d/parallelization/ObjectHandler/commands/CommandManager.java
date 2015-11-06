/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.parallelization.ObjectHandler.commands;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandManager {
	
	private static AtomicInteger barrier = new AtomicInteger();
	private static int dispatcher = 0;
	private static LinkedBlockingQueue<Command> [] complexCommandQueues = new LinkedBlockingQueue[ThreadHandler.complexWorkercount];
	//private static AtomicInteger [] complexCommandQueuesC = new AtomicInteger[ThreadHandler.complexWorkercount];
	private static LinkedBlockingQueue<Command> simpleCommandQueue = new LinkedBlockingQueue<Command>();

	static 
	{
		reset();
	}
	
	private static void reset()
	{
		for(int i =0;i<complexCommandQueues.length;i++)
		{
			complexCommandQueues[i] = new LinkedBlockingQueue<Command>();
		//	complexCommandQueuesC[i] = new AtomicInteger(-1);
		}
	}
	
	
	public static void addCommandToQueue(Command newCmd) {
		
		addCommandToQueue(newCmd,false);
	}
	
	public static void addCommandToQueue(Command newCmd,boolean retry) {
		
		if(newCmd == null)
		{
			(new Exception()).printStackTrace();
		}
		if(newCmd.commandType() == CommandType.simple)
		{
			if(!retry) barrier.incrementAndGet();
			try {
				simpleCommandQueue.put(newCmd);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(newCmd.commandType() ==CommandType.complex)
		{
			if(!retry) barrier.incrementAndGet();
			dispatcher++;
			dispatcher = dispatcher%complexCommandQueues.length;
			complexCommandQueues[dispatcher].add(newCmd);
		}
		else
		{
			new RuntimeException("that command type should not exist");
		}
	}
	
	
	public static Command fetchComplexCommand(int i) {
			try {
				return complexCommandQueues[i].take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	}
	
	
	public static Command fetchSimpleCommand() {
			try {
				return simpleCommandQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	}
	

	public static boolean allExecuted() {
		return barrier.get()==0;
	}
	
	public static int getExecutionCount()
	{
		return barrier.get();
	}

	public static void executedOne() {
		
		barrier.decrementAndGet();
	}
	
	
	public static void debug_show_Contents()
	{
		OutD.println("CommandManager.show_Contents()");
		OutD.println("complex::::");
//		for (Command c : complexCommandQueue) {
//			if(c==null) continue;
//			OutD.println("command "+c+" on box "+c.getResponseMailboxID());
//		}
//		OutD.println("Simple:::::");
//		for (Command c : simpleCommandQueue) {
//			if(c==null) continue;
//			OutD.println("command "+c+" on box "+c.getResponseMailboxID());
//		}
	}
	
}
