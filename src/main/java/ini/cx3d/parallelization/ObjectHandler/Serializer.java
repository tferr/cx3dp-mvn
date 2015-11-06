/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.ObjectHandler;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.utilities.TimeToken;
import ini.cx3d.utilities.Timer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class Serializer {
	static int counter = 0;
	public void serialize(Serializable c,ObjectOutputStream oos)
	{
		try {
			TimeToken Serialisation =Timer.start("Serialisation*");
			oos.writeObject(c);
			Timer.stop(Serialisation);
		}
		catch (Exception e) {
			e.printStackTrace(); 
			
		}	
		
	}

	public Object deserialize(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		TimeToken timerT = Timer.start("deserialize*");
		Object o = ois.readObject();
		Timer.stop(timerT);
		return o;
	}


	public static <T extends Serializable> T cloneSerializable(T a)
	{

		try {
			ObjectOutputStream oos=null;
			ByteArrayOutputStream buy=null; 

			oos = new ObjectOutputStream(buy=new ByteArrayOutputStream(5000));
			(new Serializer()).serialize(a, oos );
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buy.toByteArray()));
			a = (T)(new Serializer()).deserialize(ois);
			return a;
		} 
		catch (Exception e)
		{
			OutD.println("serialisation problem "+e);
		}
		return null;
	}

	private byte [] getBytes(Serializable c)
	{
		try {
			ObjectOutputStream oos=null;
			ByteArrayOutputStream buy=null; 

			oos = new ObjectOutputStream(buy=new ByteArrayOutputStream(5000));
			oos.writeObject(c);
			return  buy.toByteArray();
		} 
		catch (Exception e)
		{
			OutD.println("serialisation problem "+e);
		}
		return null;

	}

	public int getSize(Serializable c) {
		try {
			ObjectOutputStream oos=null;
			ByteArrayOutputStream buy=null; 

			oos = new ObjectOutputStream(buy=new ByteArrayOutputStream());
			(new Serializer()).serialize(c, oos );
			return buy.toByteArray().length;
		} 
		catch (Exception e)
		{
			OutD.println("serialisation problem "+e);
		}
		return 0;
	}
}