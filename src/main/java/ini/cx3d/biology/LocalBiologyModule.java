package ini.cx3d.biology;

import java.io.Serializable;

/**
 * 
 * Classes implementing this interface can be added in the CellElements, and be run.
 * They represent the biological model that CX3D is simulating.
 * Each instance of a localBiologyModule "lives" inside a particular CellElement. 
 * At SomaElement division or NeuriteElement branching, a cloned version is inserted into the new CellElement. 
 * If the clone() method is overwritten to return null, than the new CellElement doesn't contain a copy of the module.
 * 
 * @author fredericzubler, haurian 
 *
 */
public interface LocalBiologyModule  extends Serializable{
	
	/** Perform your specific action*/
	abstract public void run();

	/**@param cellElement the cellElement this module lives in*/
	public void setCellElement(CellElement cellElement);
	
	/** */
	public LocalBiologyModule getCopy();
	
	/** Specifies if instances of LocalBiologicalModules are are copied into new branches.*/
	public boolean isCopiedWhenNeuriteBranches();
	
	/** Specifies if instances of LocalBiologicalModules are copied when the soma divides.*/
	public boolean isCopiedWhenSomaDivides();
	
	/** Specifies if instances of LocalBiologicalModules are copied when the neurite elongates 
	 * (not in new branches!).*/
	public boolean isCopiedWhenNeuriteElongates();
	
	/** Specifies if instances of LocalBiologicalModules are copied into NeuriteElements in case of 
	 * extension of a new neurte froma a soma.*/
	public boolean isCopiedWhenNeuriteExtendsFromSoma();
	
	/** Specifies if instances of LocalBiologicalModules are deleted in a NeuriteElement that
	 * has just bifurcated (and is thus no longer a terminal neurite element).
	 */
	public boolean isDeletedAfterNeuriteHasBifurcated();
	
}
