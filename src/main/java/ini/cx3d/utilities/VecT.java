package ini.cx3d.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class VecT<E> extends ArrayList<E>{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8752638936216096449L;
	private ReadWriteLock rwlock = new ReentrantReadWriteLock();
	
	public VecT() {
		super();
	}
	
	public VecT(Collection<? extends E> k) {
		super(k);
	}
	
	public VecT(int arg0)
	{
		super(arg0);
	}
	
	@Override
	public boolean add(E e)	
	{
//		TimeToken t = Timer.start("writelock2");
		rwlock.writeLock().lock();
//		Timer.stop(t);
		boolean s = super.add(e);
		rwlock.writeLock().unlock();
		return s;
	}
	
	@Override
	public boolean remove(Object e)
	{
//		TimeToken t = Timer.start("writelock2");
		rwlock.writeLock().lock();
//		Timer.stop(t);
		boolean s = super.remove(e);
		rwlock.writeLock().unlock();
		return s;
	}
	
	@Override
	public E remove(int i)
	{
//		TimeToken t = Timer.start("writelock2");
		rwlock.writeLock().lock();
//		Timer.stop(t);
		E s = super.remove(i);
		rwlock.writeLock().unlock();
		return s;
	}
	@Override
	public E get(int i)
	{
//		TimeToken t = Timer.start("readlock2");
		rwlock.readLock().lock();
//		Timer.stop(t);
		E s = super.get(i);
		rwlock.readLock().unlock();
		return s;
	}
	

	public E unlockedGet(int i)
	{
		E s = super.get(i);
		return s;
	}
	
	public void startIteration()
	{
//		TimeToken t = Timer.start("readlock3");
		rwlock.readLock().lock();
//		Timer.stop(t);
	}
	
	
	public void stopIteration()
	{
		rwlock.readLock().unlock();
	}
	
}
