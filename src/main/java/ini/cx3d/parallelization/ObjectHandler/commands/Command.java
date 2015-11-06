/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.ObjectHandler.commands;

import ini.cx3d.parallelization.ObjectHandler.Executable;

import java.io.Serializable;


public interface Command extends Executable, Serializable{
	public long getResponseMailboxID();
	public boolean apply();
	public void setClient(String client);
	public String getClient();
	public CommandType commandType();

}
