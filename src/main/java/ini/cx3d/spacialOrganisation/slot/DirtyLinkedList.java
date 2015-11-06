package ini.cx3d.spacialOrganisation.slot;

import java.util.ArrayList;


public class DirtyLinkedList<E> extends ArrayList<E>{


	public boolean add(E e)
	{
			return super.add(e);
	}
	
	
	@Override
	public boolean remove(Object o)
	{
		return super.remove(o);
	}
	
	public boolean isDirty()
	{
		return false;
	}
	
	public void setDirty()
	{
//		dirtyTime = SimulationState.getLocal().simulationTime+Param.SIMULATION_TIME_STEP*2;
	}


	public E unlockedGet(int l) {
		// TODO Auto-generated method stub
		return super.get(l);
	}
}
