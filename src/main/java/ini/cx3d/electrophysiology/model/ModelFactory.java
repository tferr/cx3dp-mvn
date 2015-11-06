package ini.cx3d.electrophysiology.model;

import ini.cx3d.electrophysiology.model.simpleRateBased.AxonImpl;
import ini.cx3d.electrophysiology.model.simpleRateBased.DendriteImpl;
import ini.cx3d.electrophysiology.model.simpleRateBased.SomaImpl;
import ini.cx3d.electrophysiology.model.simpleRateBased.SynapseImpl;
import ini.cx3d.physics.PhysicalObject;

import java.util.ArrayList;

public class ModelFactory {
	public static SomaELModel somaModel = new SomaImpl();
	public static DendriteELModel dentriteModel = new DendriteImpl();
	public static AxonELModel axonModel= new AxonImpl();
	public static ArrayList<SynapseFactory> f = new ArrayList<SynapseFactory>();
	
	static
	{
		f.add(new SynapseFactory() {
			
			public SynapseELModel getSynapseModel(PhysicalObject o1, PhysicalObject o2) {
				return new SynapseImpl();
			}
		});
	}

	public static SynapseELModel getSynapseModel(PhysicalObject p1, PhysicalObject p2) {
		
		for(SynapseFactory temp:f)
		{
			SynapseELModel s = temp.getSynapseModel(p1, p2);
			if(s != null) return s;
		}
		return null;
	}
}
