package ini.cx3d.utilities;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HashT<K, V> extends HashMap<K, V>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9113001583286465791L;
	private ReadWriteLock rwlock = new ReentrantReadWriteLock();
	private boolean haschanged = false;
	
	
	public HashT()
	{
		super();
	}
	
	public HashT(int capacity)
	{
		super(capacity);
	}
	
	@Override
	public V put(K k,V v)
	{
		
//		TimeToken t = Timer.start("writelock1");
		rwlock.writeLock().lock();
//		Timer.stop(t);
		V v2 = super.put(k, v);
		rwlock.writeLock().unlock();
		haschanged= true;
		return v2;
	}
	
	@Override
	public V get(Object k)
	{
//		TimeToken t = Timer.start("readlock1");
//		rwlock.readLock().lock();
//		Timer.stop(t);
		V v2 = super.get(k);
//		rwlock.readLock().unlock();
		return v2;
	}
	
	@Override
	public V remove(Object k)
	{
//		TimeToken t = Timer.start("removelock1");
		rwlock.writeLock().lock();
//		Timer.stop(t);
		V v2 = super.remove(k);
		rwlock.writeLock().unlock();
		haschanged= true;
		return v2;
	}
	
	@Override
	public int size()
	{
//		TimeToken t = Timer.start("sizelock1");
		rwlock.readLock().lock();
//		Timer.stop(t);
		int v2 = super.size();
		rwlock.readLock().unlock();
		return v2;
	}
	
	
	public void writeLock()
	{
//		TimeToken t = Timer.start("writelock_2");
		rwlock.writeLock().lock();
//		Timer.stop(t);
	}
	
	public void writeUnLock()
	{
		rwlock.writeLock().unlock();
	}
	
	
	public void startIteration()
	{
		rwlock.readLock().lock();
	}
	
	
	public void stopIteration()
	{
		rwlock.readLock().unlock();
	}


	public boolean getHaschanged() {
		boolean temp= haschanged;
		haschanged = false;
		return temp;
	}
	

	
}
