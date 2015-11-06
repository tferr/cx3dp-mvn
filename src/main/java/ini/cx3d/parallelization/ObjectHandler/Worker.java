/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.ObjectHandler;

public abstract class Worker implements Runnable
{	
	public Worker(int i) {
		this.number = i;
	}
	protected int number;
	public abstract void quit();
	public abstract void run();
	public abstract Thread getTherad();
}