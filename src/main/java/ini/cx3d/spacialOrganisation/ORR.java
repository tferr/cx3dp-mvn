/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.spacialOrganisation;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.RingBuffer;

import java.io.Serializable;



public class ORR implements Serializable{

	private static ORR current = new ORR();
	public static ORR I()
	{
		return current;
	}

	public static void SetI(ORR m)
	{
		ORR.current = m;
	}


	private HashT<Long,ObjectReference> ors= new HashT<Long,ObjectReference>(MultiThreadScheduler.maxnodesperPM);

	public void put(ObjectReference r)
	{

		ObjectReference ref2 = ors.get(r.address);
		if(ref2==null)
		{
			//ors.writeLock(r.address);
			ors.writeLock();
			ref2 = ors.get(r.address);
			if(ref2==null)
			{
				ref2 = ors.put(r.address, r);
			}
			ors.writeUnLock();
			//ors.writeUnLock(r.address);
			ref2 = ors.get(r.address);
		}
		
		synchronized (ref2) {
			ref2.partitionId = r.partitionId;
			ref2.setPosition(r.getPosition());
//			try
//			{
//				AbstractPartitionManager m = ManagerResolver.I().resolve(ref2.partitionId);
//				PartitionAddress temp = m.getAddress();
//				if(!temp.containsCoordinates(ref2.getPosition()))
//				{
//					System.out.println("not contained!!!");
//				}
//			}
//			catch(Exception e)
//			{
//				OutD.println("on put ref we had an exception"+e);
//			}

		}

	}


	public ObjectReference get(long l)
	{

		ObjectReference temp = ors.get(l);
		if(temp==null)
		{
			ors.writeLock();
			temp =  ors.get(l);
			ors.writeUnLock();
		}
		return temp;
	}

	public boolean contains(Long a) {
		return ors.containsKey(a);
	}




	public void debug_anyContainingPartitionManager(PartitionManager partitionManager) {
		for (Long a: ors.keySet()) {
			ObjectReference r = ors.get(a);
			if(r.partitionId == partitionManager.address.address)
			{
				throw new RuntimeException();
			}
		}

	}

	public void debug_checkConsisitency()
	{
		for (Long a: ors.keySet()) {
			ObjectReference r = ors.get(a);
			PartitionAddress partitionid =null;
			try{
				partitionid= r.getPartitionId();
			}
			catch(NullPointerException e) 
			{
				ManagerResolver.I().debug_print();
			}
			AbstractPartitionManager m =  ManagerResolver.I().resolve(partitionid);
			if(m==null )
			{
				System.out.println("no pm "+m);
			}
			PhysicalNode temp = m.getPhysicalNode(r);
			if(temp==null)
			{
				System.out.println("no physicalnode "+r.address);
			}
		}
		//		for (PhysicalNode n : ECM.getInstance().getPhysicalNodeList()) {
		//			n.getUsedRefs();
		//		}

	}

	public void debug_print() {
		for (Long a : ors.keySet()) {
			ObjectReference r = ors.get(a);
			OutD.println(r.address+" on "+r.partitionId);
		}

	}

	public void debug_checkConsisitencyWithfetching()
	{
		//		VecT<Long> os = new VecT<Long>(ors.keySet());
		//		for (Long a: os) {
		//			ObjectReference r = ors.get(a);
		//			PhysicalNode temp =  ManagerResolver.I().resolve(r).getPhysicalNode(r);
		//		}


	}

	public static void addHistory(ObjectReference r,String ts)
	{
		//ORR.current.get(r.address).getHistory().add(ts);
	}

	public static void printout(long l)
	{
		OutD.println("writing out "+l);
		RingBuffer<String> p = ObjectReference.getAllHistory().get(l);
		if(p==null)
		{
			OutD.print("not found l's");
		}
		else
		{
			for(int  i=0;i<p.length();i++)
			{
				String s = p.get(i);
				OutD.println(s);
			}
		}
	}
}
