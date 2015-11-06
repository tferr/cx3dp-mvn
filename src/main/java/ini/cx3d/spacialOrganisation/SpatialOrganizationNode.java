/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.spacialOrganisation;

import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.utilities.serialisation.CustomSerializable;

import java.io.Serializable;
import java.util.Collection;


/**
 * Interface to define the basic properties of a node in the triangulation.
 * 
 * @author Dennis Goehlsdorf & Frederic Zubler
 *
 *
 * @param <T> The type of user objects associated with each node in the triangulation.
 */
public interface SpatialOrganizationNode extends Serializable, CustomSerializable{

	public void setRadius(double radius);

	public Collection<PhysicalNode> getNeighbors();

	public SpatialOrganizationNode getNewInstance(double[] position, PhysicalNode userObject);

	public double[] getPosition();

	public PhysicalNode getUserObject();

	public void setPosition(double[] pos);

	public void remove();

	public long getID();

	public boolean isLocal();

	public double getExtracellularConcentration(String id);

	public double getExtracellularConcentration(String id, double[] location);

	public double[] getExtracellularGradient(String id);

	public void modifyExtracellularQuantity(String id, double quantityPerTime);

	public double getVolume();

	public AbstractDiffusionNode getDiffusionNode();

	public ObjectReference getObjectRef();
	
	public boolean shallIBeProcessed();

}
