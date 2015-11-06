package ini.cx3d.spacialOrganisation.fixGrid;

import ini.cx3d.spacialOrganisation.ObjectReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class VecOptimisticLock{

	
	private static final long serialVersionUID = -8752638936216096449L;
	private volatile long number;
	private int next = 0;
//	private String s = "";
//	private String rem = "";
	private int count = 0;
	private ObjectReference [] obj;

	public VecOptimisticLock(int arg0)
	{
		obj = new  ObjectReference [arg0];
	}
	
	
	public void add(ObjectReference r)	
	{	
		boolean inserted = false;
		while(inserted == false)
		{
			number = r.address;
			next = findnull();
			obj[next] = r;
			if(number == r.address)
			{
				inserted = true;
				count ++;
//				s+=r.address+";";
			}
			else
			{
				System.out.println("collision");
			}
		}
		
	}
	
	private int findnull() {
		while(obj[next] !=null)
		{
			next ++;
			next = next%obj.length;
		}
		if(obj[next] !=null)
		{
			System.out.println("why");
		}	
		return next;
	}


	public void remove(ObjectReference r)
	{
		int i = 0;
		if(r==null) return;
		while(!r.equals(obj[i]))
		{
			i++;
			if(i==obj.length) return;
		}
		obj[i] = null;
//		rem += r.address+";";
		
	}
	
	public ObjectReference get(int i)
	{

		return obj[i];
	}
	
	public boolean contains(ObjectReference r)
	{
		if(r==null) return false;
		for(int i = 0;i<obj.length;i++)
		{
			if(r.equals(obj[i])) return true;
		}
		
		return false; 
	}


	public int capacity() {

		return obj.length;
	}


	public void set(int i, ObjectReference objectReference) {
		obj[i] = objectReference;
	}
	
	
}
