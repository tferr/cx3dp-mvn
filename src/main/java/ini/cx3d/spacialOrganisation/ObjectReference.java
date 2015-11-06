/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.spacialOrganisation;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.Matrix;
import ini.cx3d.utilities.RingBuffer;

import java.io.Serializable;


public class ObjectReference implements Serializable {
	
	public ObjectReference(long address,double [] position,PartitionAddress partitionid)
	{
		this.address = address;
		this.position = position.clone();
		this.partitionId = partitionid.address;
	}
	
	public ObjectReference(long address,double [] position,long partitionid)
	{
		this.address = address;
		this.position = position.clone();
		this.partitionId = partitionid;
	}
	
	private ObjectReference(){}
	
	private double [] position;
	public long address;
	public long partitionId;
	private double radius=0;
	private static HashT<Long,RingBuffer<String>> history = new HashT<Long,RingBuffer<String>>();
	
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof ObjectReference)) return false;
		boolean t= (((ObjectReference)o).address == address);
		return t;
	}
	
	public int hashCode()
	{
		return (new Long(address)).hashCode();
	}
	
	public String toString()
	{
		return "address= "+address+" position= ("+getPosition()[0]+","+getPosition()[1]+","+getPosition()[2]+")";  //partitionID= "+getPartitionId().address+" hostid= "+getPartitionId().getHost();
	}
	
	public double distanceTo(ObjectReference ref)
	{
		return Matrix.distance(getPosition(), ref.getPosition());
	}
	
	public void setPartitionId(PartitionAddress partAddress) {
///######################## this should never be null!
		if(partAddress !=null)
		{
			this.partitionId = partAddress.address;
		}
		else
		{
			this.partitionId =-1;
		}
	}
	

	public PartitionAddress getPartitionId() {
		long partid = partitionId;
//		if(partid==-896779896L)
//		{
//			ShowConsoleOutput.println("strange number is it?");
//		}
		AbstractPartitionManager pid =  ManagerResolver.I().resolve(partid);
//##########################################thuis will need to go eventually!!!		
		if(pid ==null)
		{
			OutD.println("ohhhh");
			ObjectReference sk = ORR.I().get(address);
			ManagerResolver.I().debug_print();
			
			OutD.println("partition id was="+partid);
			
			OutD.println("ObjectReference.getPartitionId()+partitionId"+partitionId);
			OutD.println("not found first attempt");
			new Exception().printStackTrace();
			
			ObjectReference sk2 = ORR.I().get(address);
			pid =  ManagerResolver.I().resolve(partitionId);
			if(pid ==null)
			{
				OutD.println("waiting had no effect");
				throw new RuntimeException("waeeeee this has no adress localy why?");
				
			}
		}
		return pid.getAddress();
		
	}
	
	public ObjectReference getCopy()
	{
		ObjectReference temp = new ObjectReference();
		temp.address = this.address;
		temp.partitionId = this.partitionId;
		temp.position = this.position.clone();
		return temp;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getRadius() {
		return radius;
	}

	public void setPosition(double [] position) {
		this.position = position;
	}

	public double [] getPosition() {
		return position;
	}


	public RingBuffer<String> getHistory() {
		if(!history.containsKey(this.address))
		{
			history.put(this.address, new RingBuffer<String>(10));
		}
		return history.get(this.address);
	}
	
	public static HashT<Long,RingBuffer<String>> getAllHistory()
	{
		return history;
	}
}
