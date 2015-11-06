package ini.cx3d.electrophysiology;


public class ElectroPhysiologyManager {

	private static ElectroPhysiologyManager current = new ElectroPhysiologyManager();
	public ArrayHashTELP<ElectroPhysiolgyAxon> axons = new ArrayHashTELP<ElectroPhysiolgyAxon>();
	public ArrayHashTELP<ElectroPhysiolgyDendrite> dendrites = new ArrayHashTELP<ElectroPhysiolgyDendrite>();
	public ArrayHashTELP<ElectroPhysiolgySoma> somas = new ArrayHashTELP<ElectroPhysiolgySoma>();
	
	public static ElectroPhysiologyManager I()
	{
		return  current;
	}
	
	public void add(ElectroPhysiolgyDendrite a)
	{
		
		dendrites.put(a.getParentID(),a);
	}
	
	public void add(ElectroPhysiolgySoma a)
	{
		somas.put(a.getParentID(),a);
	}
	
	
	public void add(ElectroPhysiolgyAxon a)
	{
		axons.put(a.getParentID(),a);
	}

	public void remove(ElectroPhysiolgyAxon a) {
		axons.remove(a.getParentID());
		
	}
	public void remove(ElectroPhysiolgyDendrite a) {
		dendrites.remove(a.getParentID());
		
	}
	public void remove(ElectroPhysiolgySoma a) {
		somas.remove(a.getParentID());
		
	}
	
	
	
	
	
}
