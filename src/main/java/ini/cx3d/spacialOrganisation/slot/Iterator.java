package ini.cx3d.spacialOrganisation.slot;

import ini.cx3d.spacialOrganisation.Iiterator;
import ini.cx3d.spacialOrganisation.ObjectReference;

public class Iterator implements Iiterator{

	
	private Slot s;
	private ObjectReference current;
	private int [] i = new int[3];
	private java.util.Iterator<ObjectReference> itr;
	
	public Iterator(Slot s) {
		this.s = s;
		DirtyLinkedList<ObjectReference> temp = getNextList();
		if(temp ==null)
		{
			current = null;
			return;
		}
		itr = temp.iterator();
	}
	
	@Override
	public ObjectReference getCurrent() {
		return current;
	}

	@Override
	public boolean isAtEnd() {
		// TODO Auto-generated method stub
		return current ==null;
	}


	@Override
	public boolean isCurrentSane() {
		// TODO Auto-generated method stub
		return true;
	}

	private DirtyLinkedList<ObjectReference> getNextList()
	{
		DirtyLinkedList<ObjectReference> temp = null;
		while(temp ==null)
		{
//			System.out.println("checking "+i[0]+" "+i[1]+ " "+i[2]+"; ");
			if(i[2]>=s.slots[0][0].length)
			{
				break;
			}
			temp = s.slots[i[0]][i[1]][i[2]];
			i[0]++;
			if(i[0]>=s.slots.length)
			{
				i[0]=0;
				i[1]++;
			}
			if(i[1]>=s.slots[0].length)
			{
				i[1]=0;
				i[2]++;
			}
			if(i[2]>=s.slots[0][0].length)
			{
				break;
			}
			
		}
		
		return temp; 
	}
	
	@Override
	public void next() {
		if(itr ==null)
		{
			current =null;
			return;
		}
		while(!itr.hasNext())
		{
			DirtyLinkedList<ObjectReference> temp = getNextList();
			if(temp ==null)
			{
				current = null;
				return;
			}
			itr = temp.iterator();
		}
		current =itr.next();

	}


}
