package ini.cx3d.physics;

import static ini.cx3d.utilities.Matrix.crossProduct;
import static ini.cx3d.utilities.Matrix.dot;
import static ini.cx3d.utilities.Matrix.norm;
import static ini.cx3d.utilities.Matrix.perp3;
import static ini.cx3d.utilities.Matrix.rotAroundAxis;
import static ini.cx3d.utilities.Matrix.scalarMult;
import ini.cx3d.Param;
import ini.cx3d.biology.CellElement;
import ini.cx3d.biology.synapse2.Excrescence;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.simulation.ECM;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.PartitionAddress;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;
import ini.cx3d.spacialOrganisation.SpatialOrganizationNode;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.Matrix;
import ini.cx3d.utilities.serialisation.CustomSerializable;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;



/**
 * Superclass of all the physical objects of the simulation (<code>PhysicalSphere</code> and 
 * <code>PhysicalCylinder</code>). It contains methods for different kinds of task: 
 * (1) to organize discrete elements composing the same neuron, in a tree-like structure 
 * (2) to communicate with the local biology module (<code>CellElement</code>) 
 * (3) to run the (inter-object) physics
 * (4) to run intracellular diffusion of <code>IntracellularSubstances</code>. 
 * <p>
 *
 * There are three different coordinates systems :
 * global : the global unique cartesian coordinates ([1,0,0], [0,1,0], [0,0,1])
 * local: the local coord (xAxis, yAxis, zAxis)
 * polar: cylindrical (for PhysicalCylinder) or spherical (for PhysicalSphere)
 * There exist methods to transform the polar (cylindrical/spherical) into a global
 * (cartesian) system, and for transform from global to local..
 * 
 */
public abstract class PhysicalObject extends PhysicalNode implements Serializable, Volumen {



	// * The simulation of Force in this simulation.*/
	static InterObjectForce interObjectForce = new AndreasDefaultForce();
	


	/* The unique point mass of the object*/
	public double[] massLocation = {0.0, 0.0, 0.0};
	protected double interObjectForceCoefficient = 1;
	protected CoordinateSystem cordSys= new CoordinateSystem();


	/* static friction (the minimum force amplitude for triggering a movement). */
	double adherence = 0.1;
	/* kinetic friction (scales the movement amplitude, therefore is considered as the mass)*/
	double mass = 1; 
	/* diameter of the object (wheter if sphere or cylinder).*/
	protected double diam = 1;

	/* Color used when displaying the object*/
	Color color = Param.VIOLET;


	/* All the internal and membrane-bound (diffusible and non-diffusible)
	 *  chemicals that are present inside the PhysicalObject.*/
	protected HashT<String, IntracellularSubstance> intracellularSubstances = new HashT<String, IntracellularSubstance>();

	/* List of the Physical bonds that this object can do (for cell adhesion where synapse formation occurs)*/
	protected ArrayList<PhysicalBond> physicalBonds = new ArrayList<PhysicalBond>();


	/* List of the Physical bonds that this object can do (for cell adhesion, to restore proper configuration)*/
	private Excrescence excrescence;

	/** Poor simple constructor.*/
	public PhysicalObject() {
		super();
	}

	/** Returns true because this object is a PhysicalObject.*/
	public boolean isAPhysicalObject(){
		// This function overwrites the one in PhysicalObject. 
		return true;
	}


	// *************************************************************************************
	// *      METHODS FOR NEURON TREE STRUCTURE                                            *
	// *************************************************************************************


	/**
	 * Returns true if this <code>PhysicalObject</code> and the <code>PhysicalObject</code> given as 
	 * argument have a mother-daughter or sister-sister (e.g. two daughters of a same mother) relation. 
	 */
	public abstract boolean isRelative(PhysicalObject po);


	/**
	 * Returns the absolute coordinates of the location where a <code>PhysicalObject</code> is attached
	 * to this <code>PhysicalObject</code>. Does not necessarily contain a check of the identity of the
	 *  element that makes the request.
	 * 
	 * @param daughterWhoAsks the PhysicalObject attached to us.
	 * @return the coord
	 */
	protected abstract double[] originOf(PhysicalObject daughterWhoAsks);


	/**
	 * Removal of a <code>PhysicalObject</code> from the list of our daughters.
	 * (Mainly in case of complete retraction of the daughter.*/
	protected abstract void removeDaugther(PhysicalObject daughterToRemove);


	/**
	 * Convenient way to change familly links in the neuron tree structure 
	 * (mother, daugther branch). This method is useful during elongation and 
	 * retraction for intercalation/removal of elements.
	 * of elements.
	 */
	protected abstract void updateRelative(PhysicalObject oldRelative, PhysicalObject newRelative);


	// *************************************************************************************
	// *      METHODS FOR LINK WITH THE BIOLOGY PART                                       *
	// *************************************************************************************


	/** Returns the <code>CellElement</code>linked to this <code>PhysicalObject</code>.*/
	public abstract CellElement getCellularElement();


	/** Adds an <code>Excrescence</code> instance to the Excrescence list of this 
	 * <code>PhysicalObject</code>.*/
	public void setExcrescence(Excrescence ex){

		excrescence = ex;
		ex.setPo(this);

	}


	/** Active displacement of the point mass of this <code>PhysicalObject</code>. ("active" means
	 * "triggered by a biological process" like in growth or migration). This method MUST NOT be used 
	 * for displacement by purely passive (physical) force.
	 * @param speed in microns/hour
	 * @param direction a vector indicating the direction of movement
	 */
	abstract public void movePointMass(double speed, double[] direction);


	// *************************************************************************************
	// *      METHODS FOR PHYSICS (MECHANICS) COMPUTATION                                              *
	// *************************************************************************************

	/** Compute physical forces, and move accordingly to one simulation time step.*/
	public abstract boolean runPhysics();
	
	public void prefetchDependencies()
	{
		super.prefetchDependencies();
		ArrayList<PhysicalBond> p = new ArrayList<PhysicalBond>();
		for(int i=0;i<this.physicalBonds.size();i++)
		{
			PhysicalBond pb = physicalBonds.get(i);
			if(pb == null )
			{
				p.add(pb);
				continue;
			}
			if(pb.getOppositePhysicalObject(this)==null)
			{
				p.add(pb);
				continue;
			}
			
		}
		//System.out.println("asdf");
		for(int i=0;i<p.size();i++)
		{
			physicalBonds.remove(p.get(i));
		}
	}

	public abstract void applyPhysicsCalculations();

	/**
	 * Returns the force that a daughter branch transmits to a mother's point
	 * mass. It consists of 1) the spring force between the mother and the
	 * daughter point masses and 2) the part of the inter-object mechanical
	 * interactions of the daughter branch (with other objects of the
	 * simulation) that is transmitted to the proximal end of the daughter 
	 * (= point-mass of the mother).
	 * 
	 * @param motherWhoAsks the PhysicalObject attached to the mass.
	 * @return the force in a double[]
	 */
	abstract double[] forceTransmittedFromDaugtherToMother(PhysicalObject motherWhoAsks);



	/**
	 * Returns the inter-object force that the <code>PhysicalObject</code> in which the method is called applies 
	 * onto the <code>PhysicalSphere</code> given as argument.
	 * @param s
	 * @return
	 */
	abstract public double[] getForceOn(PhysicalSphere s); 

	/**
	 * Returns the inter-object force that the <code>PhysicalObject</code> in which the method is called applies 
	 * onto the <code>PhysicalCylinder</code> given as argument.
	 * @param c
	 * @return
	 */
	abstract protected double[] getForceOn(PhysicalCylinder c); 

	/**
	 * Returns true if this <code>PhysicalObject</code> and the <code>PhysicalSphere</code> given as 
	 * argument are close enough to be considered as being in contact.
	 * @param s
	 * @return
	 */
	abstract protected boolean isInContactWithSphere(PhysicalSphere s);

	/**
	 * Returns true if this <code>PhysicalObject</code> and the <code>PhysicalSphere</code> given as 
	 * argument are close enough to be considered as being in contact.
	 * @param c
	 * @return
	 */
	abstract protected boolean isInContactWithCylinder(PhysicalCylinder c);

	/**
	 * Returns true if this <code>PhysicalObject</code> is in contact, i.e. if it is 
	 * close enough to the <code>PhysicalObject</code> given as argument.
	 * @param o
	 * @return
	 */
	public boolean isInContact(PhysicalObject o){

		if(o instanceof PhysicalSphere){
			return this.isInContactWithSphere((PhysicalSphere) o);
		}else{
			return this.isInContactWithCylinder((PhysicalCylinder) o);
		}

	}

	/**
	 * Returns all the neighboring objects considered as being in contact with this PhysicalObject.
	 * @return
	 */
	public ArrayList<PhysicalObject> getPhysicalObjectsInContact(){


		ArrayList<PhysicalObject> po = new ArrayList<PhysicalObject>(); 
		for (PhysicalNode n : getNeighboringPhysicalNodes()) {
			if(n.isAPhysicalObject() && isInContact(((PhysicalObject) n)))
				po.add((PhysicalObject) n);
		}
		return po;

	}

	// Some geometry with local and global coordinates..................................


	abstract public void changeDiameter(double speed);

	abstract public void changeVolume(double speed); 


	/**
	 * Returns the position in the global coordinate system (cartesian coordinates)
	 * of a point expressed in polar coordinates (cylindrical or spherical). 
	 * @param positionInPolarCoordinates a point defined in polar coordinate system of a PhysicalObject
	 * @return [x,y,z] the absolute value in space 
	 */
	abstract public double[] transformCoordinatesPolarToGlobal(double[] positionInPolarCoordinates);

	/**
	 * Returns the position in the polar coordinate system (cylindrical or spherical) 
	 * of a point expressed in global cartesian coordinates ([1,0,0],[0,1,0],[0,0,1]).
	 * @param positionInAbsoluteCoordinates the [x,y,z] cartesian values
	 * @return the position in local coord.
	 */
	abstract public double[] transformCoordinatesGlobalToPolar(double[] positionInAbsoluteCoordinates);

	/**
	 * Returns the position in the local coordinate system (xAxis, yXis, zAxis) 
	 * of a point expressed in global cartesian coordinates ([1,0,0],[0,1,0],[0,0,1]).
	 * @param positionInGlobalCoord
	 * @return
	 */
	public double[] transformCoordinatesGlobalToLocal(double[] positionInGlobalCoord){

		return cordSys.transformCoordinatesGlobalToLocal(positionInGlobalCoord);

	}

	/**
	 * Returns the position in in global cartesian coordinates ([1,0,0],[0,1,0],[0,0,1])
	 * of a point expressed in the local coordinate system (xAxis, yXis, zAxis).
	 * @param positionInLocalCoord
	 * @return
	 */
	public double[] transformCoordinatesLocalToGlobal(double[] positionInLocalCoord){

		return cordSys.transformCoordinatesLocalToGlobal(positionInLocalCoord);

	}


	/**
	 * Returns a unit vector, pointing out of the PhysicalObject if origin at location
	 * specified in argument.
	 * @param positionInLocalCoordinates the origin of the normal vector (local cartesian coord)
	 * @return a vector pointing "out", of unitary norm (absolute cartesian coord) 
	 */
	abstract public double[] getUnitNormalVector(double[] positionInPolarCoordinates);


	// Physical Bonds ...................................................................

	/** Simply adds the argument to the vector containing all the PhysicalBonds of this 
	 * PhysicalObject.*/  
	public void  addPhysicalBond(PhysicalBond pb){

		this.soNode.addedPhysicalBond(pb);
		physicalBonds.add(pb);
		

	}

	/** Simply removes the argument from the vector containing all the PhysicalBonds of this 
	 * PhysicalObject. */  
	public void removePhysicalBond(PhysicalBond pb){

		physicalBonds.remove(pb);

	}

	/** Returns true if there is a PhysicalBond that fixes me to this other PhysicalObject.*/
	public boolean getHasAPhysicalBondWith(PhysicalObject po){
		for(int i=0;i<physicalBonds.size();i++)
		{
			PhysicalBond pb = physicalBonds.get(i);
			if(pb == null ) continue;
			if(po == pb.getOppositePhysicalObject(this))
				return true;
		}
		return false;
	}

	/**
	 * Creates a new PhysicalBond between this PhysicalObject and the one given as argument. 
	 * The newly created PhysicalBond is inserted into the physical bon's list of both objects.
	 * @param po
	 * @return
	 */
	public PhysicalBond makePhysicalBondWith(PhysicalObject po){

		PhysicalBond pb = new PhysicalBond(this,po);
		//			this.physicalBonds.add(pb);
		//			po.addPhysicalBond(pb);
		return pb;

	}

	/**
	 * If there is a PhysicalBond between this PhysicalObject and po,
	 * it is removed (in both objects).  
	 * @param po the other PhysicalObject we want to test with
	 * @param removeThemAll if true, makes multiple removals (if multiple bonds)
	 * @return true if at least one PhysicalBond was removed
	 */
	public boolean removePhysicalBondWith(PhysicalObject po, boolean removeThemAll){

		boolean thereWasABond = false;
		for(int i=0;i<physicalBonds.size();i++)
		{
			PhysicalBond pb = physicalBonds.get(i);
			if(pb == null ) continue;
			if(po == pb.getOppositePhysicalObject(this)){
				physicalBonds.remove(i);
				po.physicalBonds.remove(pb);
				if(!removeThemAll){
					return true;
				}else{
					thereWasABond = true;
					i--; // we continue to check, and since we removed the ith
				}
			}			
		}
		return thereWasABond;

	}

	// *************************************************************************************
	// *      METHODS FOR DIFFUSION (INTRA-CELLULAR & MEMBRANE-BOUNDED SUBSTANCES)         *
	// *************************************************************************************

	/** Compute diffusion of <code>IntracellularSubstances</code> with relatives in the neuron 
	 * tree structure, and perform diffusion processes according to one simulation time step.*/
	public abstract void runIntracellularDiffusion();

	/** 
	 * Returns the concentration of an <code>IntracellularSubstance</code> in this
	 * PhysicalObject. If not present at all, zhe value 0 is returned.
	 * @param substanceId
	 * @return
	 */
	public double getIntracellularConcentration(String substanceId){

		Substance s = intracellularSubstances.get(substanceId);
		if(s == null){
			return 0;
		}else{
			return s.getConcentration(this);
		}

	}


	/** Modifies the quantity (increases or decreases) of an IntracellularSubstance. 
	 * If this <code>PhysicalNode</code> already has an <code>IntracellularSubstance</code> 
	 * instance corresponding to the type given as argument (with the same id), the fields 
	 * quantity and concentration in it will be modified, based on a computation depending
	 * on the simulation time step and the space volume (for the latter : only if volumeDependant
	 * is true in the IntracellularSubstance). If there is no such IntracellularSubstance
	 * instance already, a new instance is requested from ECM. 
	 * <p>
	 * This method is not used for diffusion, but only by biological classes...
	 * @param id the name of the Substance to change.
	 * @param quantityPerTime the rate of quantity production
	 */
	public void modifyIntracellularQuantity(String id, double quantityPerTime){


		IntracellularSubstance s = intracellularSubstances.get(id);
		if(s==null){
			s = ECM.getInstance().intracellularSubstanceInstance(id);
			intracellularSubstances.put(id, s);
		}
		double deltaQ = quantityPerTime*Param.SIMULATION_TIME_STEP;
		s.changeQuantityFrom(deltaQ);



	}

	/* adding an IntracellularSubstance instance (CAUTION : should not be used for biologic production,
	 * and therefore is not a public method. Instead , this method is used for filling up a new 
	 * PhysicalObject in case of extension).
	 */
	protected void addNewIntracellularSubstance(IntracellularSubstance s){

		intracellularSubstances.put(s.getId(), s);

	}

	/** Returns the concentration of a membrane bound IntracellularSubstance on
	 * this PhysicalObject. Recall that by definition, the PhysicalObject are 
	 * considered as expressing a cell type specific protein as well as the universal
	 * marker "U".
	 * @param id
	 * @return
	 */
	public double getMembraneConcentration(String id){

		// is it the substance that we have at our membrane because of our cell type?
		if( id == "U"){
			return 1.0;
		}
		// otherwise : do we have it on board ?
		IntracellularSubstance s = intracellularSubstances.get(id);
		if(s == null){
			return 0.0;
		}else{
			// if yes, is it a membrane substance ?
			if(!s.isVisibleFromOutside())
				return 0.0;
			return s.getConcentration(this);
		}

	}


	/** Modifies the quantity (increases or decreases) of an membrane-bound chemical.
	 *
	 * This method is not used for diffusion, but only by biological classes...
	 * @param id the name of the Substance to change.
	 * @param quantityPerTime the rate of quantity production
	 */

	public void modifyMembraneQuantity(String id, double quantityPerTime){
		// for now, the intracellular and membrane bound Substances are the same.
		modifyIntracellularQuantity(id, quantityPerTime); 
	}

	/* Returns the INSTANCE of IntracellularSubstance in this PhysicalObject with the same id
	 * than the IntracellularSubstance given as argument. If there is no such instance, a
	 * new one with similar properties is created, inserted into the intracellularSubstances
	 * vector and then returned. Only used between subclasses of physicalObject for intracellular
	 * diffusion. C.f. very similar method : PhysicalNode.giveYourSubstanceInstance.
	 */ 
	IntracellularSubstance giveYouIntracellularSubstanceInstance(IntracellularSubstance templateS){

		//possible change ahead!
		IntracellularSubstance s = intracellularSubstances.get(templateS.getId());
		if(s == null){
			s = (IntracellularSubstance)templateS.getCopy();
			intracellularSubstances.put(s.getId(), s);
		}
		return s;

	}


	/* Diffusion of diffusible IntracellularSubstances between two PhysicalObjects.
	 */
	protected void diffuseWithPhysicalObject(SpaceNodeFacade sonB, double distance){	

		// We store these temporary variable, because we still don't know if the
		// Substances depend on volumes or not

		PhysicalObject poA=null;

		try
		{
			poA = (PhysicalObject) sonB.getUserObject();

		}
		catch (Exception e) {
			throw new RuntimeException("this should be here now!");
		}



		double vA_v = poA.getVolume();
		double vthis_v = getVolume();
		double pre_a_v = (1.0/distance);
		double pre_m_v = (1.0/distance) * (1.0/vA_v + 1.0/vthis_v);
		double vA_l = poA.getLength();
		double vthis_l = getLength();
		double pre_a_l = (1.0/distance);
		double pre_m_l = (1.0/distance) * (1.0/vA_l + 1.0/vthis_l);

		// the variable we are effectively using
		double vA;
		double vthis;
		double pre_a;
		double pre_m;

		for (IntracellularSubstance sA: poA.intracellularSubstances.values()) {
			// for a given substance

			// does the substance depend on volumes or not ?
			if(sA.isVolumeDependant()){
				vA = vA_v;
				vthis = vthis_v;
				pre_a = pre_a_v;
				pre_m = pre_m_v;
			}else{
				vA = vA_l;
				vthis = vthis_l;
				pre_a = pre_a_l;
				pre_m = pre_m_l;
			}



			// stop here if 1) non diffusible substance or 2) concentration very low:
			double diffusionConstant = sA.getDiffusionConstant();


			// find the counterpart in po

			if(!intracellularSubstances.containsKey(sA.getId()))
			{
				IntracellularSubstance temp = (IntracellularSubstance) ECM.getInstance().getIntracelularSubstanceTemplates().get(sA.getId());
				intracellularSubstances.put(sA.getId(),temp);
			}
			IntracellularSubstance localSubstance = this.getIntracellularSubstance(sA.getId());

			double sBConcentration = localSubstance.getConcentration(this);
			double sAConcentration = sA.getConcentration(poA);

			// saving time : no diffusion if almost no difference;
			double absDiff = Math.abs(sAConcentration-sBConcentration);

			if(diffusionConstant<10E-5){  // 10E-14}
				continue; // to avoid a division by zero in the n/m if the diff const = 0;
			}

			if( (absDiff<10E-7) || (absDiff/sAConcentration<10E-4)){
				continue; 
			}
			// TODO : if needed, when we come here, we have to re-put ourselves on the
			// scheduler list for intra-cellular diffusion.

			// analytic computation of the diffusion between these two PhysicalObjects
			// (cf document "Diffusion" by F.Zubler for explanation).
			double qA = sA.getQuantity();
			double qthis = localSubstance.getQuantity();
			double Tot = qA + qthis;
			double a = pre_a*diffusionConstant;
			double m = pre_m*diffusionConstant;
			double n = a*Tot/vthis;
			double nOverM = n/m;
			double K = qA -nOverM;
			qA = K*Math.exp(-m*Param.SIMULATION_TIME_STEP) + nOverM;
			qthis = Tot - qA;

			localSubstance.addQuantity(qthis-localSubstance.getQuantity());
		}
	}






	// *************************************************************************************
	// *      GETTERS & SETTERS                                                            *
	// *************************************************************************************

	/** Returns the <code>java.awt.Color</code> used to draw this PhysicalObject in the GUI. */
	public Color getColor() {

		return color;

	}
	/** Sets the <code>java.awt.Color</code> used to draw this PhysicalObject in the GUI. */
	public void setColor(Color color) {

		this.color = color;

	}

	
	protected double[] getInternalMassLocation()
	{
		return massLocation;
	}
	/** Returns a copy of the masslocation.*/
	public double[] getMassLocation() {

		return  soNodePosition();


	}

	/**
	 * - CAUTION : Never use this method to move a PhysicalObject, because the physics is not updated. 
	 * <p>
	 * - Never?
	 * <p>
	 *  - I said NEVER !
	 * @param massLocation the massLocation to set
	 */
	public void setMassLocation(double[] massLocation) {

		if(Double.isNaN(massLocation[0]))
		{
			OutD.println("why!");
		}
		this.massLocation = massLocation;

	}


	public double[] getAxis() {

		return cordSys.getxAxis();		

	}


	/** Returns the first axis of the local coordinate system.*/
	public double[] getXAxis() {

		return cordSys.getxAxis();

	}

	/** Returns the second axis of the local coordinate system.*/
	public double[] getYAxis() {

		return cordSys.getyAxis();

	}


	/** Returns the third axis of the local coordinate system.*/
	public double[] getZAxis() {

		return cordSys.getzAxis();

	}


	/** If true, this PhysicalObject will be run by the Scheduler on the next occasion.*/
	public boolean isOnTheSchedulerListForPhysicalObjects() {

		return onTheSchedulerListForPhysicalNodes;

	}

	/** If true, this PhysicalObject will be run by the Scheduler on the next occasion.*/
	public void setOnTheSchedulerListForPhysicalObjects(
			boolean onTheSchedulerListForPhysicalObjects) {

		this.onTheSchedulerListForPhysicalNodes = onTheSchedulerListForPhysicalObjects;
	}

	/** Returns the vector containing all the PhysicalBonds of this PhysicalObject.*/
	public ArrayList<PhysicalBond> getPhysicalBonds() {

		return (ArrayList<PhysicalBond>) physicalBonds.clone();

	}

	/** Sets the vector containing all the PhysicalBonds of this PhysicalObject.
	 * This methof should not be used during the simulation. */
	public void setPhysicalBonds(ArrayList<PhysicalBond> physicalBonds) {

		this.physicalBonds = (ArrayList<PhysicalBond>) physicalBonds.clone();

	}

	/** Returns the vector containing all the Excrescences (PhysicalSpine, PhysicalBouton).*/
	public Excrescence getExcrescence(){

		return excrescence;

	}



	/** Returns the adherence to the extracellular matrix, i.e. the static friction 
	 * (the minimum force amplitude needed for triggering a movement). */
	public double getAdherence() {

		return adherence;

	}


	/** Sets the adherence to the extracellular matrix, i.e. the static friction 
	 * (the minimum force amplitude needed for triggering a movement). */
	public void setAdherence(double adherence) {

		this.adherence = adherence;

	}


	/** Returns the mass, i.e. the kinetic friction 
	 * (scales the movement amplitude, therefore is considered as the mass).*/
	public double getMass() {

		return mass;

	}


	/** Sets the mass, i.e. the kinetic friction 
	 * (scales the movement amplitude, therefore is considered as the mass).*/
	public void setMass(double mass) {

		this.mass = mass;

	}

	public double getDiameter() {

		return diam;

	}

	/**
	 * Sets the diameter to a new value, and update the volume accordingly.
	 * is equivalent to setDiamater(diameter, true)
	 * @param diameter
	 */
	public void setDiameter(double diameter){

		setDiameter(diameter,true);
		
	}

	/** 
	 * Sets the diameter. The volume is sets accordingly if desired. 
	 * 
	 * @param diameter the new diameter
	 * @param updateVolume if true, the volume is set to match the new diameter.
	 */
	public void setDiameter(double diameter, boolean updateVolume) {

		this.diam =Math.min(diameter,Param.MAX_DIAMETER);
		setSpaceNodeRadius();
	}

	/** Returns the volume of this PhysicalObject.*/
	public abstract double getVolume();

	public abstract void setVolume(double volume);

	/** Returns the length of a cylinder, or the diameter of a sphere.*/
	public abstract double getLength();

	/** Get an intracellular and membrane-bound chemicals that are present
	 *  in this PhysicalNode. */
	public IntracellularSubstance getIntracellularSubstance(String id) {

		return intracellularSubstances.get(id);

	}

	/** Add an intracellular or membrane-bound chemicals 
	 *  in this PhysicalNode. */
	public void addIntracellularSubstance(IntracellularSubstance is) {

		intracellularSubstances.put(is.getId(), is);

	}

	/** Remove an intracellular or membrane-bound chemicals that are present
	 *  in this PhysicalNode. */
	public void removeIntracellularSubstance(IntracellularSubstance is) {

		intracellularSubstances.remove(is);

	}

	/** All the intracellular and membrane-bound chemicals that are present
	 *  in this PhysicalNode. */
	public HashT<String, IntracellularSubstance> getIntracellularSubstances() {

		return intracellularSubstances;

	}


	/** All the intracellular and membrane-bound chemicals that are present
	 *  in this PhysicalNode. */
	public void setIntracellularSubstances(
			HashT<String, IntracellularSubstance> intracellularSubstances) {

		this.intracellularSubstances = (HashT<String, IntracellularSubstance>) intracellularSubstances.clone();

	}

	/** The class computing the inter object force.*/
	public static InterObjectForce getInterObjectForce() {
		return interObjectForce;
	}

	/** The class computing the inter object force.*/
	public static void setInterObjectForce(InterObjectForce interObjectForce) {
		PhysicalObject.interObjectForce = interObjectForce;
	}

	public double getInterObjectForceCoefficient() {

		return interObjectForceCoefficient;

	}
	
	public void setInterObjectForceCoefficient(double interObjectForceCoefficient) {
		this.interObjectForceCoefficient = interObjectForceCoefficient;
		setSpaceNodeRadius();
	}


	public void applyChanges(PhysicalNode n)
	{
		super.applyChanges(n);
		PhysicalObject node = (PhysicalObject) n;
		this.adherence = node.adherence;
		this.color = node.color;
		this.setDiameter(node.getDiameter());
		this.mass = node.mass;
		this.massLocation = node.massLocation.clone();
		this.onTheSchedulerListForPhysicalNodes = node.onTheSchedulerListForPhysicalNodes;

		//		this.totalForceLastTimeStep = node.totalForceLastTimeStep.clone();
		this.cordSys = node.cordSys.getCopy();

	}
	
	public boolean haveIBondedWith(PhysicalObject o)
	{
		for(int i=0;i<physicalBonds.size();i++)
		{
			PhysicalBond b=null;
			try{
				b = physicalBonds.get(i);
			}
			catch (Exception e) {}
			if(b == null ) continue;
			PhysicalObject temp = b.getOppositePhysicalObject(this);
			if(temp==null) continue;
			if(temp.equals(o)) return true;
		}
		return false;
	}
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof PhysicalObject)) return false;
		return soNode.getID()==((PhysicalObject)o).soNode.getID();
	}

	@Override
	public void setSoNode(SpaceNodeFacade son){
		super.setSoNode(son);
		setSpaceNodeRadius();
	}


	public double getConvolvedConcentration(String substanceA) {
		return getExtracellularConcentration(substanceA);
	}

	void updateSpatialOrganizationNodePosition() {
		soNode.setPosition(massLocation);
	}

	protected void applyChangesOnChemicals()
	{
		for (Substance  s : this.intracellularSubstances.values()) {
			s.applyCalculations();
		}
	}
	
	@Override
	public void deserialize(DataInputStream is) throws IOException {
		super.deserialize(is);
	
		this.massLocation = new double []{0.0, 0.0, 0.0};
		this.cordSys= new CoordinateSystem();
		cordSys.deserialize(is);
		
		this.adherence = is.readDouble();
		this.mass = is.readDouble(); 
		this.diam = is.readDouble();
		this.setSpaceNodeRadius();
		this.color = new Color(is.readInt());
		intracellularSubstances.clear();
		
		int to = is.readInt();
		for (int i=0;i<to;i++) {
			String key = is.readUTF();
			HashT<String, IntracellularSubstance> is2 = ECM.getInstance().getIntracelularSubstanceTemplates();
			IntracellularSubstance s = (IntracellularSubstance)is2.get(key).getCopy();
			s.quantity = is.readDouble();
			intracellularSubstances.put(key, s);
		}
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		super.serialize(os);	
		cordSys.serialize(os);
		os.writeDouble(this.adherence);
		os.writeDouble(this.mass); 
		os.writeDouble(this.diam);
		os.writeInt(this.color.getRGB());
		os.writeInt(intracellularSubstances.size());
		
		for (IntracellularSubstance o: intracellularSubstances.values()) {
			os.writeUTF(o.id);
			os.writeDouble(o.quantity);
		}
	}
	protected void setSpaceNodeRadius()
	{
		if(soNode==null) return;
		double radius = Math.max(diam/2,diam/2*getInterObjectForceCoefficient()); 
		soNode.setRadius(radius);
	}
	
	public abstract double [] getLastForce();
	
	public void checkHostDependence() {
		super.checkHostDependence();
	
		for (int i = 0;i<physicalBonds.size() ;i++) {
			if(physicalBonds.get(i)==null) continue;
			SpatialOrganizationNode n =physicalBonds.get(i).getOppositeSpatialOrganizationNode(soNode);
			ObjectReference k =  n.getObjectRef();
			//System.out.println(n.getObjectRef());
			PartitionAddress part = k.getPartitionId();		
			
			String host = part.getHost();
			if(!host.equals(this.getHost()))
			{
				if(!dependingHosts.contains(host))
					dependingHosts.add(host);
			}
		}

	}
	
	public ArrayList<PhysicalNode> getDependingNodes() {
		ArrayList<PhysicalNode> list =  super.getDependingNodes();
		for(int i=0;i<physicalBonds.size();i++)
		{
			PhysicalBond pb = physicalBonds.get(i);
			if(pb == null ) continue;
			SpatialOrganizationNode n = pb.getOppositeSpatialOrganizationNode(soNode);
			String host = n.getObjectRef().getPartitionId().getHost();
			if(!host.equals(this.getHost()))
			{
				if(!dependingHosts.contains(host))
					list.add(n.getUserObject());
			}
		}
		return list;
	}
	
}

class CoordinateSystem implements Serializable, Cloneable, CustomSerializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -789934642140851182L;
	/* First axis of the local coordinate system.*/
	private double[] xAxis = {1.0, 0.0, 0.0};
	/* Second axis of the local coordinate system.*/
	private double[] yAxis = {0.0, 1.0, 0.0};
	/* Third axis of the local coordinate system.*/
	private double[] zAxis = {0.0, 0.0, 1.0};
	/**
	 * Returns the position in the local coordinate system (xAxis, yXis, zAxis) 
	 * of a point expressed in global cartesian coordinates ([1,0,0],[0,1,0],[0,0,1]).
	 * @param positionInGlobalCoord
	 * @return
	 */



	public double[] transformCoordinatesGlobalToLocal(double[] positionInGlobalCoord){
		return new double[] { 
				dot(positionInGlobalCoord,getxAxis()), 
				dot(positionInGlobalCoord,getyAxis()),
				dot(positionInGlobalCoord,getzAxis())
		};
	}

	public CoordinateSystem getCopy() {
		// TODO Auto-generated method stub
		CoordinateSystem cord = new CoordinateSystem();
		cord.xAxis= xAxis.clone();
		cord.yAxis =  yAxis.clone();
		cord.zAxis = zAxis.clone();
		return cord;
	}

	/**
	 * Returns the position in in global cartesian coordinates ([1,0,0],[0,1,0],[0,0,1])
	 * of a point expressed in the local coordinate system (xAxis, yXis, zAxis).
	 * @param positionInLocalCoord
	 * @return
	 */
	public double[] transformCoordinatesLocalToGlobal(double[] positionInLocalCoord){
		return new double[] { 
				positionInLocalCoord[0]*getxAxis()[0] + positionInLocalCoord[1]*getyAxis()[0] + positionInLocalCoord[2]*getzAxis()[0], 
				positionInLocalCoord[0]*getxAxis()[1] + positionInLocalCoord[1]*getyAxis()[1] + positionInLocalCoord[2]*getzAxis()[1], 
				positionInLocalCoord[0]*getxAxis()[2] + positionInLocalCoord[1]*getyAxis()[2] + positionInLocalCoord[2]*getzAxis()[2] 
		};
	}


	public double[] getxAxis() {
		return xAxis.clone();
	}


	public double[] getyAxis() {
		return yAxis.clone();
	}


	public double[] getzAxis() {
		return zAxis.clone();
	}


	public void rotate(double rotationAngle,double [] rotationForce) {
		this.xAxis = rotAroundAxis(xAxis, rotationAngle, rotationForce);
		this.yAxis = rotAroundAxis(yAxis, rotationAngle, rotationForce);
		this.zAxis = rotAroundAxis(zAxis, rotationAngle, rotationForce);
	}

	public double[] transformToPolar(double[] vectorToPoint) {
		double [] localCartesian = this.transformCoordinatesGlobalToLocal(vectorToPoint);
		return new double[] {
				Math.sqrt(localCartesian[0]*localCartesian[0] + localCartesian[1]*localCartesian[1] + localCartesian[2]*localCartesian[2]),
				Math.atan2(Math.sqrt(localCartesian[0]*localCartesian[0] + localCartesian[1]*localCartesian[1]) , localCartesian[2]),
				Math.atan2(localCartesian[1],localCartesian[0])
		};
	}

	public void turnToDirection(double[] springAxis) {
		xAxis = Matrix.normalize(springAxis);
		zAxis = crossProduct(xAxis, yAxis);
		double normOfZ = norm(zAxis);
		if(normOfZ<1E-10){
			// If new xAxis and old yAxis are aligned, we cannot use this scheme;
			// we start by re-defining new perp vectors. Ok, we loose the previous info, but
			// this should almost never happen.... 
			zAxis = perp3(xAxis);
		}else{
			zAxis = scalarMult((1/normOfZ),zAxis);
		}
		yAxis = crossProduct(zAxis, xAxis);

	}
	
	@Override
	public void deserialize(DataInputStream is) throws IOException {
		this.xAxis[0] = is.readDouble();
		this.xAxis[1] = is.readDouble();
		this.xAxis[2] = is.readDouble();
		
		this.yAxis[0] = is.readDouble();
		this.yAxis[1] = is.readDouble();
		this.yAxis[2] = is.readDouble();
		
		this.zAxis[0] = is.readDouble();
		this.zAxis[1] = is.readDouble();
		this.zAxis[2] = is.readDouble();
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		os.writeDouble(this.xAxis[0]);
		os.writeDouble(this.xAxis[1]);
		os.writeDouble(this.xAxis[2]);
		
		os.writeDouble(this.yAxis[0]);
		os.writeDouble(this.yAxis[1]);
		os.writeDouble(this.yAxis[2]);
		
		os.writeDouble(this.zAxis[0]);
		os.writeDouble(this.zAxis[1]);
		os.writeDouble(this.zAxis[2]);

	}
	

	
	
}
