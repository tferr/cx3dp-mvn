/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.parallelization.communication;

import java.io.Serializable;

public interface ISimulationState extends Serializable{
	public void apply();
}
