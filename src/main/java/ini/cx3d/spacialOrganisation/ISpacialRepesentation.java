package ini.cx3d.spacialOrganisation;



public interface ISpacialRepesentation {

	public void insertNode(ObjectReference closePoint,
			ObjectReference toins);

	public void searchRange(ObjectReference point,
			double radius, IObjectReferenceSearchVisitor visitor);

	public int count();

	public ObjectReference move(ObjectReference ref, double[] position);

	public void remove(ObjectReference ref);

	public void debug_print();

	public Iiterator getIterator();

	public void  installLocally();

	public boolean shallIBeProcessed(ObjectReference r);
	
	public boolean contains(ObjectReference r);

	public void DebugCheckAllNodes_afterRemoval();

}