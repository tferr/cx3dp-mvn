package ini.cx3d.electrophysiology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class ArrayHashTELP<T> implements Serializable
{
	private ArrayList<T> nodes = new ArrayList<T>(100);
	private HashMap<Long, Integer> indexof = new HashMap<Long, Integer>(100);
	
	
	public ArrayHashTELP()
	{
		this(10000);
	}
	
	public ArrayHashTELP(int i)
	{
		nodes = new ArrayList<T>(i);
		indexof = new HashMap<Long, Integer>(i);
	}
	
	public void put(Long l,T n)
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
	
	public T getByKey(long l)
	{
		Integer k = indexof.get(l);
		if(k == null) return null;
		T c =  nodes.get(k);
		return c;
	}
	
	public T get(int i)
	{
		T c = nodes.get(i);
		if(c==null) return null;
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


class Container<T> implements Serializable
{
	T n;
	double d;
	public Container(T n,double d)
	{
		this.d = d;
		this.n = n;
	}
}