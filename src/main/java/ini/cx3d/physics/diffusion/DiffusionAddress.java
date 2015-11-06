package ini.cx3d.physics.diffusion;

import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.utilities.Cuboid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class DiffusionAddress extends Cuboid implements Serializable {

	public DiffusionAddress(String host, long parentid,int i,double[] upperLeftCornerFront,double [] lowerRightCornerBack) {
		super(upperLeftCornerFront,lowerRightCornerBack);
		String temps = Long.toString(parentid, 8);
		this.id = Long.parseLong(temps+""+i,8);
		this.host = host;
	}
	
	DiffusionAddress() {
		
	}
	
	public DiffusionAddress(DiffusionAddress orig) {
		super(orig.getUpperLeftCornerFront(),orig.getLowerRightCornerBack());
		this.id = orig.id;
		this.setHost(orig.host);
	}
	
	public DiffusionAddress(String localHost,double[] upperLeftCornerFront, double[] lowerRightCornerBack) {
		super(upperLeftCornerFront,lowerRightCornerBack);
		this.id = 1;
		this.host = localHost;
	}

	private static final long serialVersionUID = 4483303145133853458L;
	public long id;
	private String host;
	
	public boolean isLocal()
	{
		return Hosts.isOnLocalHost(host);
	}
	
	public boolean containedLocal()
	{
		return DiffusionNodeManager.I().containsLocaly(this);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof DiffusionAddress)) return false;
		return (((DiffusionAddress)o).id == id);
	}
	
	public int hashCode()
	{
		return (new Long(id)).hashCode();
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void checkchanged(DiffusionAddress changed) {
		if(changed.id == id){ 
			this.host = changed.host;
		}
	}
	
	public String toString()
	{
		return this.id +": "+Long.toString(this.id, 8)+""+" "+this.host; 
	}
	
	@Override
	public void deserialize(DataInputStream is) throws IOException {
		super.deserialize(is);
		id = is.readLong();
		host = is.readUTF();
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		super.serialize(os);
		os.writeLong(this.id);
		os.writeUTF(host);
	}

	public String getOctId() {
		
		return Long.toString(this.id, 8);
	}

	
	
}
