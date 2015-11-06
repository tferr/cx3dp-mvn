/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.spacialOrganisation;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionAddress;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class AbstractPartitionManager implements Serializable{



	public abstract void insert(ObjectReference closepoint,ObjectReference toinsert,PhysicalNode n);

	public abstract ObjectReference move(ObjectReference ref,double[] new_coordinates);

	public abstract void searchNeigbours(
			ObjectReference middle, double radius,IObjectReferenceSearchVisitor visitor);

	public abstract void getAllInRangeOnThisManager(
			ObjectReference middle, double radius, IObjectReferenceSearchVisitor visitor);

	public abstract void debug_print();

	public abstract int count();

	public abstract PartitionAddress getAddress();

	public abstract ObjectReference generateObjectAddress(double [] coordinates);

	public abstract void remove(ObjectReference ref);

	public abstract ArrayList<PartitionManager> split();

	public abstract PhysicalNode getPhysicalNode(ObjectReference ref) ;

	public abstract void removeLocaly();

	public abstract void installLocaly();

	public abstract boolean establishDiffusionGrid(int maxcount);

	public abstract AbstractDiffusionNode getDiffusionNode();

	public abstract void changeAddress(ArrayList<DiffusionAddress> adds);

	public abstract void clearCash();

	public abstract boolean prefetch();

	public abstract boolean shallIBeProcessed(ObjectReference objectRef);

}