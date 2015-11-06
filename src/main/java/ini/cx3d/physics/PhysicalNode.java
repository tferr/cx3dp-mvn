package ini.cx3d.physics;

import ini.cx3d.gui.spacialOrganisation.DrawSlotGrid;
import ini.cx3d.physics.diffusion.DiffusionNode;
import ini.cx3d.simulation.ECM;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.serialisation.CustomSerializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;




/**
 * PhysicalNode represents a piece of the simulation space, whether it contains a physical object of not. 
 * As such, it contains a list of all the chemicals (<code>Substance</code>) that are present at this place, 
 * as well as the methods for the diffusion. In order to be able to diffuse chemicals, it contains a node
 * that is part of the neighboring system (eg triangulation). A <code>PhysicalNode</code> can only diffuse 
 * to and receive from the neighbouring <code>PhysicalNode</code>s.
 * <p>
 * The <code>PhysiacalObject</code> sub-classes (<code>PhysicalSphere</code>, <code>PhysicalCylinder</code>)
 * inherit from this class. This emphasize the fact that they take part in the definition of space and 
 * that diffusion of chemical occurs across them.
 * <p>
 *
 * As all the CX3D runnable objects, the PhysicalNode updates its state (i.e. diffuses and degradates) only 
 * if needed. The private field <code>onTheSchedulerListForPhysicalNodes</code> is set to <code>true</code>
 * in this case. (For degradation, there is an update mechanism, catching up from the last time it was performed).
 * 
 * @author fredericzubler
 *
 */
public abstract class PhysicalNode implements Serializable, CustomSerializable{
	// NOTE : all the "protected" fields are not "private" because they have to be accessible by subclasses

	/**
	 * 
	 */
	private static final long serialVersionUID = 7152423336619513468L;
	/* Unique identification for this CellElement instance. Used for marshalling/demarshalling*/



	/* If true, the PhysicalNode will be run by the Scheduler.*/
	protected boolean onTheSchedulerListForPhysicalNodes = true;

	

	/* My anchor point in the neighboring system */
	protected SpaceNodeFacade soNode;
	private double creationTime;
	protected boolean ignoreboundaries; 

	private  transient double lastecmupdateTime = -1;

	protected ArrayList<ObjectReference> lastneighboursRefs = new ArrayList<ObjectReference>();
	


	public PhysicalNode(){
		creationTime = ECM.getInstance().getECMtime();
	}

	// *************************************************************************************
	// *        instanceof-like methods                                                    *
	// *************************************************************************************


	/** Returns true if this PhysicalNode is a PhysicalObject.*/ 
	public boolean isAPhysicalObject(){
		//The function is overwritten in PhysicalObject. 
		return false;
	}

	/** Returns true if this PhysicalNode is a PhysicalCylinder.*/ 
	public boolean isAPhysicalCylinder(){
		// The function is overwritten in PhysicalSphere. 
		return false;
	}

	/** Returns true if this PhysicalNode is a PhysicalSphere.*/
	public boolean isAPhysicalSphere(){
		// The function is overwritten in PhysicalSphere 
		return false;
	}



	// *************************************************************************************
	// *        INTERACTION WITH PHYSICAL_OBJECTS (secretion, reading concentration etc.)  *
	// *************************************************************************************

	/**
	 * Returns the concentration of an extra-cellular Substance at this PhysicalNode.
	 * @param id the name of the substance
	 * @return the concentration
	 */
	public double getExtracellularConcentration(String id){

		return soNode.getExtracellularConcentration(id);
	}



	/**
	 * Returns the concentration of an extra-cellular Substance outside this PhysicalNode.
	 * @param id the name of the substance
	 * @param location the place where concentration is probed
	 * @return the concentration
	 */
	public double getExtracellularConcentration(String id, double[] location){
		return soNode.getExtracellularConcentration(id,location);
	}


	/**
	 * Returns the gradient at the space node location for a given substance.
	 * The way this method is implemented was suggested by Andreas Steimer. 
	 * @param id the name of the Substance we have to compute the gradient of.
	 * @return [dc/dx, dc/dy, dc/dz]
	 */
	public double[] getExtracellularGradient(String id){
		return soNode.getExtracellularGradient(id);
	}

	/** Modifies the quantity (increases or decreases) of an extra-cellular Substance. 
	 * If this <code>PhysicalNode</code> already has an <code>Substance</code> instance
	 * corresponding to the type given as argument (with the same id), the fields 
	 * quantity and concentration in it will be modified, based on a computation depending
	 * on the simulation time step and the space volume. If there is no such Substance
	 * instance already, a new instance is requested from ECM.
	 * <p>
	 * This method is not used for diffusion, but only by biological classes...
	 * @param id the name of the Substance to change.
	 * @param quantityPerTime the rate of quantity production
	 */
	public void modifyExtracellularQuantity(String id, double quantityPerTime){
		soNode.modifyExtracellularQuantity(id, quantityPerTime);
	}


	// *************************************************************************************
	// *                            RUN (diffusion, degradation)                           *
	// *************************************************************************************








	// *************************************************************************************
	// *      GETTERS & SETTERS                                                            *
	// *************************************************************************************


	/** Returns the position of the <code>SpatialOrganizationNode</code>. 
	 * Equivalent to getSoNode().getPosition(). */
	// not a real getter... 
	public double[] soNodePosition(){

		return soNode.getPosition();

	}

	/**
	 * returns all <code>PhysicalNodes</code> considered as neighbors.
	 */
	public Collection<PhysicalNode> getNeighboringPhysicalNodes(){
		
		return soNode.getNeighbors();
		
	}
	
	public Collection<PhysicalNode> getNeighboringPhysicalNodes_test(){
		
		return soNode.getNeighbors_test();
		
	}
	

	/** Sets the SpatialOrganizationNode (vertex in the triangulation neighboring system).*/
	public SpaceNodeFacade getSoNode(){

		return soNode;

	}
	/** Returns the SpatialOrganizationNode (vertex in the triangulation neighboring system).*/
	public void setSoNode(SpaceNodeFacade son){

		this.soNode = son;
		//			if(!soNode.isLocal())
		//			{
		//				new RuntimeException("only local moving allowed!");
		//			}
	}


	/** if <code>true</code>, the PhysicalNode will be run by the Scheduler.**/
	public boolean isOnTheSchedulerListForPhysicalNodes() {

		return onTheSchedulerListForPhysicalNodes;

	}
	/** if <code>true</code>, the PhysicalNode will be run by the Scheduler.**/
	public void setOnTheSchedulerListForPhysicalNodes(
			boolean onTheSchedulerListForPhysicalNodes) {

		this.onTheSchedulerListForPhysicalNodes = onTheSchedulerListForPhysicalNodes;

	}

	/** Solely used by the PhysicalNodeMovementListener to update substance concentrations.**/
	int getMovementConcentratioUpdateProcedure() {
		throw new RuntimeException("concentration not cuppled anymore!");
	}
	/** Solely used by the PhysicalNodeMovementListener to update substance concentrations.**/
	void setMovementConcentratioUpdateProcedure(
			int movementConcentratioUpdateProcedure) {
		throw new RuntimeException("concentration not cuppled anymore!");
	}

	/** Add an extracellular or membrane-bound chemicals 
	 *  in this PhysicalNode. */
	public void addExtracellularSubstance(Substance is) {
		soNode.modifyExtracellularQuantity(is.getId(), 0);
	}

	/** Remove an extracellular or membrane-bound chemicals that are present
	 *  in this PhysicalNode. */
	public void removeExtracellularSubstance(Substance is) {
		throw new RuntimeException("should be done externaly now!!! but may relay through here");
	}

	/** All the (diffusible) chemicals that are present in the space defined by this physicalNode. */
	public HashT<String, Substance> getExtracellularSubstances() {
		return ((DiffusionNode)soNode.getDiffusionNode()).getSubstances();
	}

	/** All the (diffusible) chemicals that are present in the space defined by this physicalNode. */
	public void setExtracellularSubstances(
			HashT<String, Substance> extracellularSubstances) {
		throw new RuntimeException("should be done externaly now!!! but may relay through here");
	}




	public void applyChanges(PhysicalNode node)
	{
		this.onTheSchedulerListForPhysicalNodes = node.onTheSchedulerListForPhysicalNodes;
	}

	public void removeLocally() {
//				ShowConsoleOutput.println("removing localy node only "+soNode.getID());
		ECM.getInstance().removePhysicalNode(this);				
	}

	public void installLocally() {
				

		ECM.getInstance().addPhysicalNode(this);

	}




	/**
	 * Degradate (according to degrad. constant) and diffuse 8according to diff. constant)
	 * all the <code>Substance</code> stored in this <code>PhysicalNode</code>.
	 */
	public void runExtracellularDiffusion(){

	}

	public PhysicalCylinder getAsPhysicalCylinder()
	{
		return null;
	}
	public PhysicalSphere getAsPhysicalSphere()
	{
		return null;
	}


	public void prefetchDependencies()
	{
		checkHostDependence();
	}
	
	public void prefetchDependencies_test()
	{
		checkHostDependence_test();
	}

	protected void checkForRemoteReference(PhysicalNode n )
	{
		if(!ManagerResolver.I().isLocal(n.getSoNode().getObjectRef().partitionId))
			lastneighboursRefs.add( n.getSoNode().getObjectRef());
	}

	@Override
	public void deserialize(DataInputStream is) throws IOException {
		this.onTheSchedulerListForPhysicalNodes = true;
		this.creationTime = -100;
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		
	}

	public boolean hasRemoteNeighbours()
	{
		if(lastneighboursRefs.size()>0) return true;
		return false;
	}



	public void checkNeighbors(HashT<String,HashT<Long,PhysicalNode>> remotes,String beloningHost)
	{
		

		for(String  host: dependingHosts)
		{
			if(host.equals(beloningHost)) continue;
			if(!remotes.containsKey(host))
			{
				remotes.put(host,new HashT<Long, PhysicalNode>());
			}
			remotes.get(host).put(soNode.getID(), this);
		}


	}

	public ArrayList<String> dependingHosts = new ArrayList<String>();
	public void checkHostDependence() {
		
		dependingHosts = new ArrayList<String>();	
		ArrayList<Long> temp = soNode.getNeighborKeys();
		int host_t = this.getHost().hashCode();
		for (Long n : temp) {
			String host = soNode.getNeigbour(n).getHost();
			int t = host.hashCode();
			if(host_t!=t)
			{
				if(!dependingHosts.contains(host))
				{
					dependingHosts.add(host);
				}
			}	
		}
	
	}
	
	
	public void checkHostDependence_test() {
//		dependingHosts = new ArrayList<String>();
		dependingHosts = new ArrayList<String>();
		ArrayList<Long> temp = soNode.getNeighborKeys();
		int host_t = this.getHost().hashCode();
		for (Long n : temp) {
			String host = soNode.getNeigbour(n).getHost();
			int t = host.hashCode();
			if(host_t!=t)
			{
				if(!dependingHosts.contains(host))
				{
					dependingHosts.add(host);
				}
			}	
		}		
	}

	protected String getHost()
	{
		return getSoNode().getObjectRef().getPartitionId().getHost();
	}
	
	public void updateDependenciesIgnoreTimestep()
	{
		DrawSlotGrid.setSpacenode(this.soNode);
	
		soNode.updateDependenceiesIgnoreTimesetp();
		try
		{
			checkHostDependence();
		}
		catch (RuntimeException e) {
			
			//only test remove later on!!!
			soNode.updateDependenceiesIgnoreTimesetp();
			checkHostDependence();
		}
		DrawSlotGrid.setSpacenode(this.soNode);
	}

	public  ArrayList<String> getDependingHosts() {
		return dependingHosts;
		
	}

	public ArrayList<PhysicalNode> getDependingNodes() {
		return new ArrayList<PhysicalNode>();
	}


	public double getCreationTime() {
		return creationTime;
	}

	public void setIgnoreboundaries(boolean ignoreboundaries) {
		this.ignoreboundaries = ignoreboundaries;
	}

	public boolean getIgnoreboundaries() {
		return ignoreboundaries;
	}

}

