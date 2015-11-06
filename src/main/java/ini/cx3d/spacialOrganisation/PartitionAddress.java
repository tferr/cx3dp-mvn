/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.spacialOrganisation;

import ini.cx3d.utilities.Cuboid;

import java.io.Serializable;
import java.util.ArrayList;

public class PartitionAddress extends Cuboid implements Serializable {
	
	
	public PartitionAddress(String host, long parentid,int i,double [] upperLeftCornerFront,double [] lowerRightCornerBack) {
		super(upperLeftCornerFront,lowerRightCornerBack);
		String temps = Long.toString(parentid, 8);
		this.address = Long.parseLong(temps+""+i,8);
		this.host = host;
	}
	public PartitionAddress(String host,double [] upperLeftCornerFront,double [] lowerRightCornerBack) {
		super(upperLeftCornerFront,lowerRightCornerBack);
		this.address = 1;
		this.host = host;
	}
	
	
	public PartitionAddress(String host, long i, double [] upperLeftCornerFront,double [] lowerRightCornerBack) {
		super(upperLeftCornerFront,lowerRightCornerBack);
		this.address = i;
		this.host = host;
	}


	private String host;
	public long address;

	public String toString()
	{
		return "address= "+getOctId()+"" +
				"host "+host+
				" uppercorner= ("+getUpperLeftCornerFront()[0]+","+getUpperLeftCornerFront()[1]+","+getUpperLeftCornerFront()[2]+")\n" +
				" lowercorner= ("+getLowerRightCornerBack()[0]+","+getLowerRightCornerBack()[1]+","+getLowerRightCornerBack()[2]+") " +
				" partitionID= "+address+" hostid= "+getHost();
	}
		
	public PartitionAddress getCopy() {	
		try {
			return (PartitionAddress) this.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}	

	@Override 
	public boolean equals(Object o)
	{
		if(!(o instanceof PartitionAddress)) return false;
		return address == ((PartitionAddress)o).address;
	}
	
	public int hashCode()
	{
		return new Long(address).hashCode();
	}
	
	
	
	public ArrayList<PartitionAddress> getNeighbours()
	{
		ArrayList<PartitionAddress> adds = new ArrayList<PartitionAddress>();
		for(PartitionAddress p: ManagerResolver.I().getAllPartitionAddresses())
		{
			if(this.areWeNeighbours(p))
			{
				adds.add(p);
			}
		}
		
		return adds;
	}

	
	public String getOctId() {
		
		return Long.toString(this.address, 8);
	}
	

	
}
