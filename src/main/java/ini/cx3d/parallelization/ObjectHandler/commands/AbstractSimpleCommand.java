/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.ObjectHandler.commands;

public abstract class AbstractSimpleCommand<T> extends AbstractComplexCommand<T>{
	
	public CommandType commandType()
	{
		return CommandType.simple;
	}
}
