package ini.cx3d.utilities;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class HashT2<K, V>  extends AbstractMap<K,V>
implements Map<K,V>, Cloneable, Serializable
{
	
	private boolean haschanged = false;
	private HashT<K, V>[] hashes;
	private int size=0;
	
	private void init()
	{
		for(int i =0;i<hashes.length;i++)
		{
			hashes[i]= new HashT<K, V>();
		}
	}
	
	
	public HashT2()
	{
		this(127);
	}
	
	public HashT2(int capacity1)
	{
		hashes = new HashT[capacity1];
		init();
	}
	
	
	private HashT<K, V> resolveHash(Object k)
	{
		int o = Math.abs(k.hashCode())%hashes.length;
		return hashes[o];
	}
	
	@Override
	public V put(K k,V v)
	{
		
		V v2 = resolveHash(k).put(k, v);
		size++;
		haschanged= true;
		return v2;
	}
	
	@Override
	public V get(Object k)
	{

		V v2 = resolveHash(k).get(k);
		return v2;
	}
	
	@Override
	public V remove(Object k)
	{
		
		V v2 = resolveHash(k).remove(k);
		size--;
		return v2;
	}
	
	@Override
	public int size()
	{
		return size;
	}


	public boolean getHaschanged() {
		boolean temp= haschanged;
		haschanged = false;
		return temp;
	}


	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
	
		return null;
	}
	
	public Iterator<K> getKeyIterator()
	{
		return new KeyIterator<K>();
	}
	
	class KeyIterator<K> implements Iterator<K>
	{
		private int i=0;
		private Iterator<K> iter;
		
		public KeyIterator()
		{
			iter = (Iterator<K>) HashT2.this.hashes[i].keySet().iterator();
		}
		
		@Override
		public boolean hasNext() {
			if(i<hashes.length) return false;
			return true;
		}

		@Override
		public K next() {
			if(!iter.hasNext())
			{
				i++;
				iter = (Iterator<K>) HashT2.this.hashes[i].keySet().iterator();
			}
			return iter.next();
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}

	public void writeLock(K k) {
		resolveHash(k).writeLock();
	}
	
	public void writeUnLock(K k) {
		resolveHash(k).writeUnLock();
	}
	
}


