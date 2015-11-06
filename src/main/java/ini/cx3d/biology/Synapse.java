package ini.cx3d.biology;

import ini.cx3d.physics.PhysicalBond;

import java.io.Serializable;
import java.util.ArrayList;

public class Synapse implements Serializable{
	private PhysicalBond pbond;
	/* List of all the SubElements : small objects performing some biological operations.*/
	protected ArrayList<AbstractLocalBiologyModule> localBiologyModulesList = new ArrayList<AbstractLocalBiologyModule>();
	
	public void addLocalSynapseBiologyModule(AbstractLocalBiologyModule s)
	{
		localBiologyModulesList.add(s);
	}
	
	public void removeLocalSynapseBiologyModule(AbstractLocalBiologyModule s)
	{
		localBiologyModulesList.remove(s);
	}
	
	public void removeLocalSynapseBiologyModule()
	{
		localBiologyModulesList.clear();
	}
	
	public void setPhysicalBond(PhysicalBond pb)
	{
		pbond = pb;
	}
	
	public PhysicalBond getPhysicalBond()
	{
		return pbond;
	}
	
	public void installLocally()
	{
		
	}
	
	public void removeLocally()
	{
		
	}
	
	/* Calls the run() method in all the <code>SubElements</code>. 
	 * Is done automatically during the simulation, and thus doesn't have to be called by the user*/ 
	protected void runLocalBiologyModules(CellElement cele){
		ArrayList<AbstractLocalBiologyModule> locbio = new ArrayList<AbstractLocalBiologyModule>(localBiologyModulesList);
		for (int i = 0; i < locbio.size(); i++) {
			
			if(locbio.get(i) instanceof AbstractLocalBiologySynapseModule)
			{
				((AbstractLocalBiologySynapseModule)locbio.get(i)).setCellElement(this);
			}
			locbio.get(i).run();
		}
	}
	
	
	
}
