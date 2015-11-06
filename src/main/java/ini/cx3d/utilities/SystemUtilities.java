package ini.cx3d.utilities;

import ini.cx3d.gui.simulation.OutD;

/**
 * This class contains some methods for measuring execution speed and memory.
 * @author fredericzubler
 *
 */
public abstract class SystemUtilities {
	
	/** Prints the total memory used (with Runtime.getRuntime.totalMemory().*/
	public static void totalMemoryUsed(){
		OutD.print("Total memory used : ");
		OutD.print(Runtime.getRuntime().totalMemory() / 1024);
		OutD.println(" kbytes.");
	}
	

	/** Prints the total memory used (with Runtime.getRuntime.totalMemory().*/
	public static void totalMemoryLeft(){
		OutD.print("Total memory left : ");
		OutD.print(Runtime.getRuntime().freeMemory() / 1024);
		OutD.println(" kbytes.");
	}
	
	
	
	
	// the time when the chronometer is started.
	private static long clicValue;
	
	/** Starts the chronometer.*/
	public static void tic(){
		clicValue = System.currentTimeMillis();
	}
	
	/** Prints the time lapse since the chronometer was last started.*/
	public static void tac(){
		long endTime = System.currentTimeMillis();
		long timeElapsed = endTime-clicValue;
		if(timeElapsed<1000){
			OutD.println(timeElapsed+" ms");
		}else{
			OutD.println(((double)timeElapsed)/1000.0+" s");
		}
	}
	
	/** Prints the time lapse since the chronometer was last started,
	 * the restarts it again.*/
	public static void tacAndTic(){
		long endTime = System.currentTimeMillis();
		OutD.println((endTime-clicValue)+" ms.");
		clicValue = System.currentTimeMillis();
	}
	
	/** Pause in the execution of the  current thread. */
	public static void freeze(int time){
		try {
			Thread.sleep(time);
		} catch (Exception e){}
	}

}
