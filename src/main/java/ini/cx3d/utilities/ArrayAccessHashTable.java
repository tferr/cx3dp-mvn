package ini.cx3d.utilities;



import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.simulation.SimulationState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class ArrayAccessHashTable implements Serializable
{
	private ArrayList<PhysicalNode> nodes = new ArrayList<PhysicalNode>(100);
	private HashMap<Long, Integer> indexof = new HashMap<Long, Integer>(100);
	
	
	public ArrayAccessHashTable()
	{
		this(10000);
	}
	
	public ArrayAccessHashTable(int i)
	{
		nodes = new ArrayList<PhysicalNode>(i);
		indexof = new HashMap<Long, Integer>(i);
	}
	
	public void put(Long l,PhysicalNode n)
	{
		synchronized (this) {
			int i=nodes.size();
			nodes.add(i, n);
			indexof.put(l, i);
		}
	}
	
	public void remove(long l)
	{
		synchronized (this) {
			Integer k = indexof.remove(l);
			if(k !=null)
			{
				nodes.set(k, null);
			}
		}
	}
	
	public boolean containsKey(long id) {
		// TODO Auto-generated method stub
		Integer k = indexof.get(id);
		if(k == null) return false;
		return true;
	}
	
	public PhysicalNode getByKey(long l)
	{
		Integer k = indexof.get(l);
		if(k == null) return null;
		PhysicalNode c =  nodes.get(k);
		return c;
	}
	
	public PhysicalNode get(int i)
	{
		PhysicalNode c = nodes.get(i);
		if(c==null) return null;
		return c;
	}
	
	public PhysicalNode getIfTobeProcessed(int i)
	{
		PhysicalNode c = nodes.get(i);
		if(c==null) return null;
		
		if(c.getCreationTime()>=SimulationState.getLocal().simulationTime)
		{
			return null;
		}
		return c;
	}
	
	
	public int getInsertCount()
	{
		return nodes.size();
	}
	
	public int size()
	{
		return getInsertCount();
	}
	

	public boolean getHaschanged() {
		// TODO Auto-generated method stub
		return false;
	}
}


class Container implements Serializable
{
	PhysicalNode n;
	double d;
	public Container(PhysicalNode n,double d)
	{
		this.d = d;
		this.n = n;
	}
}