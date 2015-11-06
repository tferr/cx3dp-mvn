/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.physics.diffusion;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.VecT;

import java.io.Serializable;
import java.util.ArrayList;



public class DiffReg implements Serializable{

	private static DiffReg current = new DiffReg();
	private long highest;
	public static DiffReg I()
	{
		return current;
	}
	
	public static void SetI(DiffReg m)
	{
		current=m;
	}

	private HashT<Long,DiffusionAddress> ors= new HashT<Long,DiffusionAddress>();
	
	public void put(DiffusionAddress r)
	{
		VecT<String > hosts = Hosts.getHosts();
		if(!r.isLocal() && !hosts.contains(r.getHost()))
		{
			System.out.println("was hosts no not again...");
		}
		highest = Math.max(r.id,highest);
		OutD.println("inserting addrses"+r.getOctId(),"red");
		ors.put(r.id, r);
	}

//	public boolean updateAddress(DiffusionAddress r)
//	{
//		DiffusionAddress d= new DiffusionAddress(r);
//		ors.put(r.id,d);
//		return true;
//	}

	public void remove(long l)
	{
		ors.remove(l);
	}

	public DiffusionAddress get(long l)
	{
		if(!ors.containsKey(l)) 
		{
			OutD.println("not contained!"+l);
		}
		return ors.get(l);
	}
	
	public ArrayList<DiffusionAddress> getMyChildern(long l)
	{
		ArrayList<DiffusionAddress> o = new ArrayList<DiffusionAddress>();
		for(int i=0;i<8;i++)
		{
			OutD.println(Long.toString(l, 8)+"the id");
			long id = Long.parseLong(Long.toString(l, 8)+""+i,8);
			getMeOrMyChildren(id, o);
		}
		return o;
	}
	
	public void getMeOrMyChildren(long l,ArrayList<DiffusionAddress> o)
	{
		
		if(l>highest)
		{
			//ShowConsoleOutput.println("could not find any along:"+ Long.toString(l,8));
			return;
		}
		DiffusionAddress s = ors.get(l);
		if(s!=null)
		{
			if(Hosts.getHosts().contains(s.getHost()))
			{
				if(DiffusionNodeManager.I().contains(s))
				{
					OutD.println(" replaced by "+Long.toString(l, 8)+" "+s.getOctId());
					o.add(s);
					return;
				}
			}
			
		}
		
		for(int i=0;i<8;i++)
		{
			long id = Long.parseLong(Long.toString(l, 8)+""+i,8);
			getMeOrMyChildren(id, o);
		
		}
		
	}
	
	public ArrayList<DiffusionAddress> get(ArrayList<Long> l)
	{
		ArrayList<DiffusionAddress> adds = new ArrayList<DiffusionAddress>();
		for (int i=0;i<l.size();i++)
		{
			Long a = l.get(i);
			if(!ors.containsKey(a))
			{
				OutD.println("not contained!"+a+" "+Long.toString(a, 8));
			}
			adds.add(ors.get(a));
		}
		return adds;
	}

	public boolean contains(Long a) {
		return ors.containsKey(a);
	}

}
