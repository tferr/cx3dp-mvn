/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.spacialOrganisation;

import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.HashT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class SpaceNodeRegistry implements Serializable{

	private HashT<ObjectReference, PhysicalNode> objref = new HashT<ObjectReference, PhysicalNode>(MultiThreadScheduler.maxnodesperPM);

	public  PhysicalNode resolve(ObjectReference key)
	{	
		PhysicalNode temp = objref.get(key);
		if(temp!=null) return temp;
		
		objref.writeLock();
		temp= objref.get(key);
		objref.writeUnLock();
		return temp;
	}

	public  void register(final ObjectReference key,final PhysicalNode node)
	{
		OptimisticExecuter.exectue(new IToExectue() {
			
			public void execute() {
				// TODO Auto-generated method stubORR.addHistory(key, "insert to registry");
				PhysicalNode n2=  objref.get(key);
				if(n2==node) return;
				objref.put(key,node);
//				DebugShouldContain(key);
				
			}
			public boolean check() {
				
				return containsKey(key);
			}
		});
			
		
	}

	private void DebugShouldContain(ObjectReference key) {
		if(!objref.containsKey(key))
		{
			System.out.println("should contain!");
		}
	}

	public  void remove(final ObjectReference key)
	{
		OptimisticExecuter.exectue(new IToExectue() {
			
			public void execute() {
				objref.remove(key);
			}
			public boolean check() {
				
				return !containsKey(key);
			}
		});
				
	}

	public void writelock()
	{
		objref.writeLock();
	}
	
	public void writeUnlock()
	{
		objref.writeUnLock();
	}

	public int count() {
		return objref.size();
	}

	public  Collection<PhysicalNode> getAll() {

		return objref.values();
	}

	public  ArrayList<ObjectReference> getAllObjectReferences() {
		return new ArrayList<ObjectReference>(objref.keySet());
	}

	public boolean containsKey(ObjectReference ref) {
		return this.objref.containsKey(ref);
	}

	public void installLocaly()
	{

	}

	public  void debug_checkforNull()
	{
		for (ObjectReference s : this.objref.keySet()) {
			if(this.objref.get(s)==null){
				throw new RuntimeException("whoo why null?");
			}

		}
	}

	public void debug_checkifListcontained(ISpacialRepesentation list) {
		Iiterator iter = list.getIterator();
		iter.next();
		while (! iter.isAtEnd()) {
			if(iter.isCurrentSane())
			{
				if(!objref.containsKey(iter.getCurrent()))
				{
					throw new RuntimeException("this should be contained!");
				}
				if(objref.get(iter.getCurrent())==null)
				{
					throw new RuntimeException("this should not be null:-(!");
				}
			}
			iter.next();
		}


	}

	public void DebugCheckAllNodes_afterRemoval() {
		try
		{
			ArrayAccessHashTable nodes = MultiThreadScheduler.getNodesToProcess();
			
				for (ObjectReference s : this.objref.keySet()) {
					if(this.objref.get(s)==null){
						if(nodes.containsKey(s.address))
						{
							System.out.println("error should not exist anymore!");
						}
					}
	
				}
					
			
		}
		catch(Exception e)
		{
			System.out.println("asdf");
		}
	}

}
