

package ini.cx3d.biology;

import ini.cx3d.physics.PhysicalBond;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.utilities.serialisation.CustomSerializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Super class for the local biological discrete elements (SomaElement & NeuriteElement).
 * Contains a <code>VecT</code> of <code>LocalBiologyModule</code>.
 * @author fredericzubler
 *
 */
public abstract class CellElement implements Serializable, CustomSerializable {




	/* List of all the SubElements : small objects performing some biological operations.*/
	protected ArrayList<LocalBiologyModule> localBiologyModulesList = new ArrayList<LocalBiologyModule>();

	
	protected HashMap<String,String> properties = new HashMap<String, String>(); 
	
	/** Simple constructor.*/
	public CellElement() {
	
	}

	
	public String getPropertiy(String key)
	{
		String temp = properties.get(key);
		if(temp ==null) temp = "";
		return temp;
	}
	
	public void setPropertiy(String key,String value)
	{
		 properties.put(key,value);
	}
	
	
	// *************************************************************************************
	// *      METHODS FOR LOCAL BIOLOGY MODULES                                            *
	// *************************************************************************************

	/* Calls the run() method in all the <code>SubElements</code>. 
	 * Is done automatically during the simulation, and thus doesn't have to be called by the user*/ 
	protected void runLocalBiologyModules(){
		//This type of loop because the removal of a SubElements from the subElementsList
		// could cause a ConcurrentModificationException.
		ArrayList<LocalBiologyModule> locbio = new ArrayList<LocalBiologyModule>(localBiologyModulesList);
		for (int i = 0; i < locbio.size(); i++) {
			if(locbio.get(i)!=null)
			{
				locbio.get(i).setCellElement(this);
				locbio.get(i).run();
			}
		}
		for (PhysicalBond pb : getPhysical().getPhysicalBonds()) {
			if(pb ==null)continue;
			if(pb.getSynapse()!=null)
			{
				pb.getSynapse().runLocalBiologyModules(this);
			}
		}
	}

	
	
	

	/** Adds the argument to the <code>LocalBiologyModule</code> list, and registers this as it's 
	 * <code>CellElements</code>.*/
	public void addLocalBiologyModule(LocalBiologyModule m){
		m.setCellElement(this); // set the callback before adding it to the modules otherwise stuff might not be set in mutlithreading
		localBiologyModulesList.add(m);
	}

	/** Removes the argument from the <code>LocalBiologyModule</code> list.*/
	public void removeLocalBiologyModule(LocalBiologyModule m){
		localBiologyModulesList.remove(m);
	}
    
    /** Removes all the <code>LocalBiologyModule</code> in this <code>CellElements</code>.*/
	public void cleanAllLocalBiologyModules() {
		localBiologyModulesList.clear();
	}

	/** Returns the localBiologyModule List (not a copy).*/
	public ArrayList<LocalBiologyModule> getLocalBiologyModulesList() {
		return localBiologyModulesList;
	}
	
	/** Sets the localBiologyModule List.*/
	public void setLocalBiologyModulesList(ArrayList<LocalBiologyModule> localBiologyModulesList) {
		this.localBiologyModulesList = localBiologyModulesList;
	}


	// *************************************************************************************
	// *      METHODS FOR SETTING CELL                                                     *
	// *************************************************************************************

	/**
	 * Sets the <code>Cell</code> this <code>CellElement</code> is part of. 
	 * @param cell
	 */
	public abstract void setCell(Cell cell);
	
	/**
	 * 
	 * @return the <code>Cell</code> this <code>CellElement</code> is part of.
	 */
	public abstract Cell getCell();
	
	// *************************************************************************************
	// *      METHODS FOR DEFINING TYPE (neurite element vs soma element)                                                  *
	// *************************************************************************************

	/** Returns <code>true</code> if is a <code>NeuriteElement</code>.*/
	public abstract boolean isANeuriteElement();
	/** Returns <code>true</code> if is a <code>SomaElement</code>.*/
	public abstract boolean isASomaElement();

	// *************************************************************************************
	// *      METHODS FOR CALLS TO PHYSICS (POSITION, ETC)                                 *
	// *************************************************************************************

	/** Returns the location of the point mass of the <code>PhysicalObject</code> 
	 * associated with this <code>CellElement</code>.
	 */
	public double[] getLocation() {
		return getPhysical().getMassLocation();
	}

	/** The <code>PhysicalSphere or <code>PhysicalCylinder</code> linked with this <code>CellElement</code>.*/
	public abstract PhysicalObject getPhysical();

	/** The <code>PhysicalSphere or <code>PhysicalCylinder</code> linked with this <code>CellElement</code>.*/
	public abstract void setPhysicalAndInstall(PhysicalObject p);

	/**
	 * Displaces the point mass of the <code>PhysicalObject</code> associated with
	 * this <code>CellElement</code>.
	 * @param speed in microns/hours
	 * @param direction (norm not taken into account)
	 */
	public void move(double speed, double[] direction){
		getPhysical().movePointMass(speed, direction);
	}
	
	public abstract void installLocally();
	public abstract void removeLocally();
	
	@Override
	public void deserialize(DataInputStream is) throws IOException {
		int k = is.readInt();
		for(int i=0;i<k;i++)
		{
			properties.put(is.readUTF(), is.readUTF());
		}
		
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		os.writeInt(properties.size());
		for(String k: properties.keySet())
		{
			os.writeUTF(k);
			os.writeUTF(properties.get(k));
		}
		
	}


	public boolean contains(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}


	
}
