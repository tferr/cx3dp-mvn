package ini.cx3d.spacialOrganisation.fixGrid;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HTable<T> extends HashMap<Integer, T>{
	
	
	
	private static final long serialVersionUID = -9113001583286465791L;
	private ReadWriteLock rwlock = new ReentrantReadWriteLock();
	
	public void lockWrite()
	{
		rwlock.writeLock().lock();
	}
	
	
	public void unlockWrite()
	{
		rwlock.writeLock().unlock();
	}
	
	public void put(int i,T r )
	{
		super.put(i, r);
	}
	
	public T remove(int i)
	{
		return super.remove(i);
	}
	
	
	public T get(int i)
	{
		return super.get(i);
	}
	
}
