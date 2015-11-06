/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.communication;

import ini.cx3d.utilities.HashT;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SimulationStateManager {
	public static HashT<String, ISimulationState> simulationStates = new HashT<String, ISimulationState>();
	public static ISimulationState localSimulationState;
	public static void write(ObjectOutputStream outputStream) throws IOException {
		outputStream.writeObject(localSimulationState);
		//outputStream.writeObject(simulationStates);

	}
	
	public static void read(ObjectInputStream in) throws ClassNotFoundException, IOException {
		localSimulationState = (ISimulationState)in.readObject();
		//simulationStates = (HashT<String, ISimulationState>) in.readObject();
	}
	
}
