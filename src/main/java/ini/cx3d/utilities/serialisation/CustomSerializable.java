package ini.cx3d.utilities.serialisation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface CustomSerializable {
	public void serialize(DataOutputStream os) throws IOException; 
	public void deserialize(DataInputStream is) throws IOException;
}
