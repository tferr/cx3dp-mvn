package ini.cx3d.physics;

import static ini.cx3d.utilities.Matrix.add;
import static ini.cx3d.utilities.Matrix.crossProduct;
import static ini.cx3d.utilities.Matrix.norm;
import static ini.cx3d.utilities.Matrix.normalize;
import static ini.cx3d.utilities.Matrix.printlnLine;
import static ini.cx3d.utilities.Matrix.scalarMult;
import static ini.cx3d.utilities.Matrix.subtract;
import ini.cx3d.Param;
import ini.cx3d.biology.CellElement;
import ini.cx3d.biology.SomaElement;
import ini.cx3d.electrophysiology.ElectroPhysiolgySoma;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.simulation.ECM;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.Matrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;



/**
 * The embodiment of SomaElement. Contains 
 * 
 *
 * The spherical coordinates (r, phi, theta) are defined as:
 * r >= 0 is the distance from the origin to a given point P.
 * 0 <= phi <= pi is the angle between the positive z-axis and the line formed between the origin and P.
 * 0 <= theta < 2pi is the angle between the positive x-axis and the line from the origin 
 * to the P projected onto the xy-plane.
 * 
 * @author fredericzubler
 *
 */

public class PhysicalSphere extends PhysicalObject{


	/* Local biology object associated with this PhysicalSphere.*/
	private SomaElement somaElement = null;
	private ElectroPhysiolgySoma elsoma;

	/* Position in local coordinates (PhysicalObject's xAxis,yAxis,zAxis) of 
	 * the attachment point of my daughters.*/
	HashT<SpaceNodeFacade, double[]> daughtersCoord = new HashT<SpaceNodeFacade, double[]>();

	/* Plays the same role than mass and adherence, for rotation around center of mass. */
	private double rotationalInertia = 0.5; // estaba en 0.5 y 5.0



	/* Force applied by the biology. Is taken into account during runPhysics(), and the set to 0.*/
	protected double[] tractorForce = {0,0,0};




	public PhysicalSphere() {
		super();

		mass = 1; 
		adherence = Param.SPHERE_DEFAULT_ADHERENCE;
		setDiameter(Param.SPHERE_DEFAULT_DIAMETER);

	}





	public double getRotationalInertia() {

		return rotationalInertia;

	}

	public void setRotationalInertia(double rotationalInertia) {

		this.rotationalInertia = rotationalInertia;


	}

	/** returns true because this object is a PhysicalSphere */
	public boolean isAPhysicalSphere(){
		return true;
	}


	// *************************************************************************************
	//   RELATIONS WITH MOTHER & DAUGHTERS
	// *************************************************************************************




	public void movePointMass(double speed, double[] direction){
		// NOTE : 
		// a) division by norm(direction), so we get a pure direction
		// b) multiplication by the mass, because the total force is divide 
		//  	by the mass in runPhysics().
		// c) multiplication by speed for obvious reasons...
		// d) the scaling for simulation time step occurs in the runPhysics() method

		//		double factor = speed*mass/norm(direction);
		double length = speed*Param.SIMULATION_TIME_STEP;
		direction = normalize(direction);
		double displacement[] = Matrix.scalarMult(length, direction);

		setMassLocation(add(displacement, getInternalMassLocation()));


		// if we are told to move (by our SomaElement for instance), we will update
		// our physics.
		if(speed > 1E-10){
			setOnTheSchedulerListForPhysicalObjects(true);
		}

	}


	/**
	 * 
	 * 
	 * @param daughterWhoAsks .
	 * 
	 */
	public double[] originOf(PhysicalObject daughterWhoAsks) {

		double[] xyz = daughtersCoord.get(daughterWhoAsks.soNode);
		double radius = getDiameter()*.5;
		xyz = cordSys.transformCoordinatesLocalToGlobal(scalarMult(radius, xyz));
		double[] origin =  Matrix.add(massLocation,xyz);
		return origin;
	}

	/**
	 * A PhysicalSphere has no mother that could call, so this method is not used.
	 */
	protected double[] forceTransmittedFromDaugtherToMother(PhysicalObject motherWhoAsks) {
		return null;
	}

	public void removeAllDaughter()
	{
		daughtersCoord = new HashT<SpaceNodeFacade, double[]>();
	}
	
	public void removeDaugther(PhysicalObject daughterToRemove) {


		daughtersCoord.remove(daughterToRemove.soNode);

	}

	protected void updateRelative(PhysicalObject oldRelative, PhysicalObject newRelative) {

		double[] coordOfTheNeuriteThatChanges = daughtersCoord.get(oldRelative.soNode);
		daughtersCoord.put(newRelative.soNode, coordOfTheNeuriteThatChanges);

	}

	// *************************************************************************************
	//   DISCRETIZATION , SPATIAL NODE, CELL ELEMENT
	// *************************************************************************************

	/* Move the SpatialOrganizationNode in the center of this PhysicalSphere. If it is
	 * only a small distance off (half of the radius), there is no movement.
	 */
	//	private void updateSpatialOrganizationNodePosition() {
	//		
	//		double[] currentCenterPosition = soNode.getPosition();
	//		double displacementOfTheCenter[] = new double[] {	massLocation[0] - currentCenterPosition[0],
	//				massLocation[1] - currentCenterPosition[1],
	//				massLocation[2] - currentCenterPosition[2]  };
	//		double offset = norm(displacementOfTheCenter);
	//		if(offset>getDiam()*0.25 || offset > 5){
	//
	//			// TODO : do we need this ?
	//			displacementOfTheCenter = add(displacementOfTheCenter,randomNoise(getDiam()*0.025, 3));
	//			try {
	//				soNode.moveFrom(displacementOfTheCenter);
	//			} catch (PositionNotAllowedException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//		
	//	}




	// *************************************************************************************
	//   BIOLOGY (growth, division, new neurite... )
	// *************************************************************************************


	/**
	 * @return the somaElement
	 */
	public SomaElement getSomaElement() {

		return somaElement;

	}

	/**
	 * @param somaElement the somaElement to set
	 */
	public void setSomaElement(SomaElement somaElement) {

		if (somaElement != null) {

			this.somaElement = somaElement;

		} else {
			OutD.println("ERROR  PhysicalSphere: somaElement already exists");
		};
	}


	/**
	 * Progressive modification of the volume. Updates the diameter, the intracellular concentration 
	 * @param speed cubic micron/ h
	 */
	public void changeVolume(double speed) {

		//scaling for integration step
		double dV = speed*(Param.SIMULATION_TIME_STEP);
		setVolume(getVolume()+ dV);
		if(getVolume() < 5.2359877E-7 ){	// minimum volume, corresponds to minimal diameter
			System.err.println("PhysicalSphere.changeVolume() : volume is "+getVolume());
			setVolume(5.2359877E-7); 
		}


		scheduleMeAndAllMyFriends();

	}

	/**
	 * Progressive modification of the diameter. Updates the volume, the intracellular concentration 
	 * @param speed micron/ h
	 */
	public void changeDiameter(double speed) {
		//scaling for integration step

		double dD = speed*(Param.SIMULATION_TIME_STEP);
		setDiameter(getDiameter()+dD);
		if(getDiameter() < 0.01 ){
			System.err.println("PhysicalSphere.changeDiameter() : diameter is "+getDiameter());
			setDiameter(0.01); // minimum diameter
		}


		// no call to updateIntracellularConcentrations() cause it's done by updateVolume().
		scheduleMeAndAllMyFriends();

	}






	/**
	 * Extension of a PhysicalCylinder as a daughter of this PhysicalSphere. The position on the sphere where
	 * the cylinder will be attached is specified in spherical coordinates with respect to the
	 * cx3d.cells Axis with two angles. The cylinder that is produced is specified by the object (usually
	 * a SomaElement) that calls this method. Indeed, unlike PhysicalCylinder.insertProximalCylinder() for instance, 
	 * this method is called for biological reasons, and not for discretization purposes.
	 * 
	 * @param cyl the PhysicalCylinder instance (or a class derived from it) that will be extended.
	 * @param phi the angle from the zAxis
	 * @param theta the angle from the xAxis around the zAxis 
	 */
	public PhysicalCylinder addNewPhysicalCylinder(double newLength, double phi, double theta){
		double radius = 0.5*this.getDiameter();
		// position in cx3d.cells coord
		double x = Math.cos(theta)*Math.sin(phi);
		double y = Math.sin(theta)*Math.sin(phi);
		double z = Math.cos(phi);
		double[] axisDirection = cordSys.transformCoordinatesLocalToGlobal(new double[]{x,y,z});
		printlnLine("axisDirection", axisDirection);
		// positions & axis in cartesian coord
		double[] newCylinderBeginingLocation = add(massLocation, scalarMult(radius,axisDirection));
		double[] newCylinderSpringAxis = scalarMult(newLength,axisDirection);

		double[] newCylinderMassLocation = add(newCylinderBeginingLocation, newCylinderSpringAxis);
		//	double[] newCylinderCentralNodeLocation = add(newCylinderBeginingLocation, scalarMult(0.5,newCylinderSpringAxis));
		// new PhysicalCylinder
		PhysicalCylinder cyl = new PhysicalCylinder();
		cyl.cordSys.turnToDirection(axisDirection);
		// familly relations	


		// SpaceNode
		SpaceNodeFacade newSON = null;

		newSON = soNode.getNewInstance(soNode.getPosition().clone(), cyl);

		cyl.setSoNode(newSON);
		cyl.setMother(this);




		cyl.setMassLocation(newCylinderMassLocation);
		cyl.setRestingLengthToSetTension(Param.NEURITE_DEFAULT_TENSION);
		cyl.setDiameter(Param.NEURITE_DEFAULT_DIAMETER, true);
		// Color 
		cyl.setColor(this.color);




		daughtersCoord.put(cyl.soNode, new double[] {x, y, z});

		ECM.getInstance().addPhysicalCylinder(cyl);

		return cyl;
	}



	/**
	 * Division of the sphere into two spheres. The one in which the method is called becomes 
	 * one the 1st daughter sphere (it keeps its Soma); a new PhysicalSphere is instantiated
	 * and becomes the 2nd daughter (and the Soma given as argument is attributed to it
	 * as CellElement). One can specify the relative size of the daughters (2nd/1st). 
	 * In asymmetrical division the cx3d.cells that divides stays the progenitor, so the ratio is 
	 * smaller than 1.  
	 * @param somaElement the PhysicalSphere for daughter 2
	 * @param vr ratio of the two volumes (vr = v2/v1)
	 * @param phi the angle from the zAxis (for the division axis)
	 * @param theta the angle from the xAxis around the zAxis (for the division axis)
	 * @return the other daughter (new sphere)
	 */
	public PhysicalSphere divide(double vr, double[] dir){
		// A) Defining some values ..................................................................
		// defining the two radii s.t total volume is conserved ( R^3 = r1^3 + r2^3 ; vr = r2^3 / r1^3 ) 
		double radius = getDiameter()*0.5;
		vr = Math.max(Math.min(1, vr),0);
		double v1 = getVolume()*vr;
		double v2 = getVolume()*(1-vr);

		double r1 = Math.pow(v1/(4.0/3.0)/Math.PI,1.0/3.0);
		double r2 = Math.pow(v2/(4.0/3.0)/Math.PI,1.0/3.0);
		// define an axis for division (along which the nuclei will move) in cx3d.cells Coord
		double TOTAL_LENGTH_OF_DISPLACEMENT = radius/2; 
		//		TOTAL_LENGTH_OF_DISPLACEMENT = 5;
		double[] axisOfDivision = Matrix.scalarMult(TOTAL_LENGTH_OF_DISPLACEMENT, dir); 
		axisOfDivision = normalize(axisOfDivision);
		// two equations for the center displacement :
		//  1) d2/d1= v2/v1 = vr (each sphere is shifted inver. proportionally to its volume) 
		// 	2) d1 + d2 = TOTAL_LENGTH_OF_DISPLACEMENT
		//fred version
		//		double d2 = TOTAL_LENGTH_OF_DISPLACEMENT/(vr+1)); 
		//		double d1 = TOTAL_LENGTH_OF_DISPLACEMENT-d2;
		double d2 = TOTAL_LENGTH_OF_DISPLACEMENT; 
		double d1 = 0;
		double[] newSphereMassLocation = new double[] { 	
				massLocation[0] + d2*axisOfDivision[0],
				massLocation[1] + d2*axisOfDivision[1],
				massLocation[2] + d2*axisOfDivision[2]  };



		// B) Instantiating a new sphere = 2nd daughter................................................
		// getting a new sphere
		PhysicalSphere newSphere = new PhysicalSphere();
		SpaceNodeFacade newSON = null;

		newSON = soNode.getNewInstance(newSphereMassLocation, newSphere);

		newSphere.setSoNode(newSON);
		// super class variables (except masLocation, filled below)
		newSphere.cordSys = cordSys.getCopy();
		newSphere.color = color;
		newSphere.adherence = adherence;
		newSphere.mass = mass;

		// this class variables (except radius/diameter)
		newSphere.rotationalInertia = rotationalInertia;
		newSphere.adherence = this.adherence;
		newSphere.setInterObjectForceCoefficient(this.getInterObjectForceCoefficient());
		newSphere.diam = r2*2;

		// Mass Location


		// C) Request a SpaceNode
		//		double[] newSphereSpaceNodeLocation = ;//ManagerResolver.I().getAllLocalPartitionManagers().get(0).getDiffusionnode().getMiddle();


		newSphere.setMassLocation(newSphereMassLocation);


		massLocation[0] -= d1*axisOfDivision[0];
		massLocation[1] -= d1*axisOfDivision[1];
		massLocation[2] -= d1*axisOfDivision[2];



		Volumen oldv = new Volumen() {

			double temp = this.getVolume(); 
			@Override
			public double getVolume() {
				return temp;

			}
		};
		// F) change properties of this cell 
		this.setDiameter(r1*2, true);	

		// G) Copy the intracellular and membrane bound Substances
		for (IntracellularSubstance sub : intracellularSubstances.values()) {
			IntracellularSubstance subCopy = (IntracellularSubstance)sub.getCopy(); 	//copy substance
//			sub.distributeConcentrationOnDivision(oldv,this,subCopy,newSphere);
			sub.distributeQuantityOnDivision(subCopy);
			newSphere.intracellularSubstances.put(subCopy.getId(), subCopy);
		}
		//		this.setColor(Color.red);

		return newSphere;
	}


	// *************************************************************************************
	//   PHYSICS
	// *************************************************************************************

	/**
	 * Tells if a sphere is in the detection range of an other sphere.
	 */
	public boolean isInContactWithSphere(PhysicalSphere s){

		double[] force = PhysicalObject.interObjectForce.forceOnASphereFromASphere(this,s);
		if(norm(force)>1E-15){
			return true;
		}else{
			return false;
		}

		//		double sumOfTheRadius = 0.5*(super.diameter + s.getDiameter() );
		//		// if larger force-field, we increase the detection range
		//		if(this.forceFieldType == Param.TISSUE_CELL_FORCE_FIELD && s.getForceFieldType() == Param.TISSUE_CELL_FORCE_FIELD){
		////			sumOfTheRadius += 3.5;
		//		}
		//		double additionalRadius = 10.0*Math.min(s.getInterObjectForceCoefficient(), this.getInterObjectForceCoefficient());
		//		sumOfTheRadius += 2*additionalRadius; // 2 times: one for each sphere
		//		sumOfTheRadius *= 1; // some extra range
		//
		//		double distanceBetweenSphereCenters = distance(massLocation, s.getMassLocation());
		//		if(distanceBetweenSphereCenters -sumOfTheRadius < Param.SPHERE_SPHERE_DETECTION_RANGE ){
		//			return true;
		//		}
		//		return false;
	}

	@Override
	public boolean isInContactWithCylinder(PhysicalCylinder c) {

		double[] force = PhysicalObject.interObjectForce.forceOnACylinderFromASphere(c,this);
		if(norm(force)>1E-15){
			return true;
		}else{
			return false;
		}


		//		// detailed comments on the method used to find the closest point on a line from a point
		//		// in Force.forceOnACylinderFromASphere().
		//		double[] pP = c.proximalEnd();
		//		double[] pD = c.getMassLocation();
		//		double[] axis = c.getSpringAxis();
		//		double actualLength = c.getActualLength();
		//		double[] pPc = subtract(massLocation,pP);
		//
		//		// 		projection of pPc onto axis = (pPc.axis)/norm(axis)^2  * axis
		//		// 		length of the projection = (pPc.axis)/norm(axis)
		//		double pPcDotAxis = pPc[0]*axis[0] + pPc[1]*axis[1] + pPc[2]*axis[2];
		//		double K = pPcDotAxis/(actualLength*actualLength);
		//		//		cc = pP + K* axis 
		//		double[] cc  = new double[] {pP[0]+K*axis[0], pP[1]+K*axis[1], pP[2]+K*axis[2]}; 
		//
		//		if(K<0){ 	// if the closest point to c on the line pPpD is before pP
		//			cc = pP;
		//		}else {   	// if cc is after pD, the force is only on the distal end (the segment's point mass).	 
		//			cc = pD;
		//		}
		//
		//		double penetration = 0.5*( c.getDiameter() + this.diameter ) -distance(massLocation,cc) ;		 
		//		if(penetration > -Param.SPHERE_CYLINDER_DETECTION_RANGE) {
		//			return true;
		//		}
		//		return false;
	}


	@Override
	public double[] getForceOn(PhysicalCylinder c) {

		return interObjectForce.forceOnACylinderFromASphere(c, this);
	}


	@Override
	public double[] getForceOn(PhysicalSphere s) {
		//		double[] f;
		//		if(this.forceFieldType == Param.TISSUE_CELL_FORCE_FIELD && s.getForceFieldType() == Param.TISSUE_CELL_FORCE_FIELD){	
		//			f = Force.tissueInteractionForceOnASphereFromASphere(
		//					s.getMassLocation(), s.getDiameter()*0.5, massLocation, diameter*0.5);
		//		}else{	
		//			f = Force.forceOnASphereFromASphere(
		//					s.getMassLocation(), s.getDiameter()*0.5, massLocation, diameter*0.5);
		//		}
		//		return f;
		return interObjectForce.forceOnASphereFromASphere(s, this);
	}




	public boolean runPhysics() {


		// Basically, the idea is to make the sum of all the forces acting
		// on the Point mass. It is stored in translationForceOnPointMass.
		// There is also a computation of the torque (only applied
		// by the daughter neurites), stored in rotationForce.



		prefetchDependencies();

		setOnTheSchedulerListForPhysicalObjects(true);

		for (int i = 0;i<physicalBonds.size() ;i++) {
			PhysicalBond pb = this.physicalBonds.get(i);
			if(pb==null) continue;
			if(pb.checkForBreak())
			{
				this.physicalBonds.remove(i);
				OutD.println("removed "+pb.getID());
			}
		}

		// the physics force to move the point mass
		double[] translationForceOnPointMass = {0,0,0};


		// 1) "artificial force" to maintain the sphere in the ecm simulation boundaries--------

		//		if(ECM.getInstance().getArtificialWallForSpheres()){
		//			double[] forceFromArtificialWall = ECM.getInstance().forceFromArtificialWall(massLocation, getDiameter()*0.5);
		//			forceFromArtificialWall = Matrix.scalarMult(10, forceFromArtificialWall);
		//			translationForceOnPointMass= Matrix.add(translationForceOnPointMass,forceFromArtificialWall);	
		//		}

		// 2) Spring force from my neurites (translation and rotation)--------------------------
		//
		// the physics force to rotate the cell
		double[] rotationForce = {0,0,0};
		for (SpaceNodeFacade s : daughtersCoord.keySet()) {
			double[] forceFromDaughter = new double[]{0,0,0};

			PhysicalCylinder c = (PhysicalCylinder)s.getUserObject();
			forceFromDaughter = c.forceTransmittedFromDaugtherToMother(this);
			forceFromDaughter= Matrix.scalarMult(c.mass/(this.mass+c.mass), forceFromDaughter);
			translationForceOnPointMass= Matrix.add(translationForceOnPointMass,forceFromDaughter);	

			double[] xyz = daughtersCoord.get(s);
			double[] r = this.cordSys.transformCoordinatesLocalToGlobal(xyz);
			rotationForce = add(rotationForce, crossProduct(r, forceFromDaughter));
		}
		//
		// 3) Object avoidance force -----------------------------------------------------------

		//	(We check for every neighbor object if they touch us, i.e. push us away)
		double mindist = 20;
		for (PhysicalNode neighbor : getNeighboringPhysicalNodes()) {

			PhysicalObject n = (PhysicalObject)neighbor;
			// if it is a direct relative, we don't take it into account
			if(daughtersCoord.containsKey(n.getSoNode())){  // no physical effect of a member of the family...
				continue;
			}
			// if we have a PhysicalBond with him, we also don't take it into account
			if(physicalyboundTo(n)) continue;

			double[] forceFromThisNeighbor = n.getForceOn(this);
			forceFromThisNeighbor = Matrix.scalarMult(n.mass/(this.mass+n.mass), forceFromThisNeighbor);
			translationForceOnPointMass= Matrix.add(translationForceOnPointMass,forceFromThisNeighbor);	

			double dist = Matrix.distance(this.getSoNode().getPosition(), neighbor.getSoNode().getPosition());
			if(dist<mindist)
			{
				mindist = dist; 
			}

		}



		// 4) PhysicalBonds--------------------------------------------------------------------
		for(int i=0;i<physicalBonds.size();i++)
		{
			PhysicalBond pb = physicalBonds.get(i);
			if(pb == null ) continue;
			PhysicalObject n = pb.getOppositePhysicalObject(this);
			if(n==null)
			{

				OutD.println("n is zero damn");
				OutD.println("physicalOposite:-()");
				continue;
			}
			double[] forceFromThisPhysicalBond = pb.getForceOn(this);
			forceFromThisPhysicalBond = Matrix.scalarMult(n.mass/(this.mass+n.mass), forceFromThisPhysicalBond);
			translationForceOnPointMass= Matrix.add(translationForceOnPointMass,forceFromThisPhysicalBond);	
		}




		// adding the physics translation (scale by weight) if important enough

		//ajust fortimestep

		translationForceOnPointMass = Matrix.scalarMult(Param.SIMULATION_TIME_STEP,translationForceOnPointMass);

		//		if(translationForceOnPointMass[0]!=0 ||translationForceOnPointMass[2]!=0 )
		//		{
		//			System.out.println("nope");
		//		}

		double normOfTheForce = Matrix.norm(translationForceOnPointMass);

		// but we want to avoid huge jumps in the simulation, so there are maximum distances possible
		if(normOfTheForce > mindist*0.45){
			translationForceOnPointMass = scalarMult(mindist*0.45, normalize(translationForceOnPointMass));
		}


		this.tractorForce = new double[]{0,0,0};
		futureRotationForce = rotationForce;

		futureMovement =  translationForceOnPointMass;

		return true;
	}

	private boolean physicalyboundTo(PhysicalObject n) {
		
		for(int i=0;i<physicalBonds.size();i++)
		{
			PhysicalBond pb = physicalBonds.get(i);
			if(pb == null ) continue;
			if(pb.getOppositePhysicalObject(this) == n){
				return true;
			}
		}
		return false;
	}

	private double [] futureMovement;
	private double [] futureRotationForce;
	public void applyPhysicsCalculations()
	{
		if(futureMovement !=null)
		{

			massLocation = Matrix.add(massLocation,futureMovement);

			if(futureRotationForce!=null)
			{
				if(norm(futureRotationForce) !=0){
					double rotationAngle = 3.14*Param.SIMULATION_TIME_STEP;
					this.cordSys.rotate(rotationAngle,futureRotationForce);
				}
				futureRotationForce = null;
			}

			futureMovement = new double[]{0,0,0};

		}
		//		if(Matrix.distanceSquare(this.getSoNode().getPosition(),massLocation)>0)
		//		{

		if(ECM.getInstance().getArtificialWallForSpheres() && !ignoreboundaries)
		{
			massLocation = ECM.getInstance().forceFromArtificialWall(massLocation, getDiameter()/2);
		}


		updateSpatialOrganizationNodePosition();
		scheduleMeAndAllMyFriends();
		massLocation = soNodePosition();
		//		}
		applyChangesOnChemicals();
	}


	private void scheduleMeAndAllMyFriends(){

		// Re-schedule me and every one that has something to do with me :
		setOnTheSchedulerListForPhysicalObjects(true);
		// daughters : 
		//		for (int i = 0; i < daughters.size(); i++) {
		//	 		scheduleCheck(daughters.get(i));
		//		}
		//		
		//		for (PhysicalNode neighbor : getNeighboringPhysicalNodes()) {
		//			if(neighbor.isAPhysicalObject()){
		//				scheduleCheck(neighbor.soNode);
		//			}
		//		}
		//		
		//		for (PhysicalBond pb : this.physicalBonds) {
		//			scheduleCheck(pb.getOppositeSpatialOrganizationNode(soNode));
		//		}

	}



	public double[] getAxis() {		
		return cordSys.getxAxis();

	}

	/**
	 * @return the daughters
	 */
	public ArrayList<PhysicalCylinder> getDaughters() {
		ArrayList<PhysicalCylinder> c = new ArrayList<PhysicalCylinder>();
		for (SpaceNodeFacade d : daughtersCoord.keySet()) {
			c.add((PhysicalCylinder)d.getUserObject());
		}
		return c;
	}


	@Override
	public void runIntracellularDiffusion() {		


		// 1) Degradation according to the degradation constant for each chemical
		for (IntracellularSubstance is: intracellularSubstances.values())
		{
			is.degrade(this);
		}


		// TODO: check if does not change to much in the simulations!!!

		//	for (Substance s : intracellularSubstances.values()) {
		//	double decay = Math.exp(-s.getDegradationConstant()*Param.SIMULATION_TIME_STEP);
		//	s.multiplyQuantityAndConcentrationBy(decay);
		//	s.setConcentration(s.getConcentration()-s.getDegradationConstant()*s.getConcentration());
		//	s.updateQuantityBasedOnConcentration(this.getVolume());
		//	}

		//	2) Diffusion in Physical cylinders
		// TODO : scramble daughters so that we don't always go in same order.
		ArrayList<SpaceNodeFacade> daugters = new ArrayList<SpaceNodeFacade>(daughtersCoord.keySet());
		for (SpaceNodeFacade cyl : daugters) {
			diffuseWithPhysicalObject(cyl, ((PhysicalCylinder)cyl.getUserObject()).getActualLength());
		}

	}




	// G -> L
	/**
	 * Returns the position in the local coordinate system (xAxis, yXis, zAxis) 
	 * of a point expressed in global cartesian coordinates ([1,0,0],[0,1,0],[0,0,1]).
	 * @param positionInGlobalCoord
	 * @return
	 */
	public double[] transformCoordinatesGlobalToLocal(double[] positionInGlobalCoord){
		positionInGlobalCoord = subtract(positionInGlobalCoord, massLocation);
		return cordSys.transformCoordinatesGlobalToLocal(positionInGlobalCoord);
	}
	// L -> G
	/**
	 * Returns the position in global cartesian coordinates ([1,0,0],[0,1,0],[0,0,1])
	 * of a point expressed in the local coordinate system (xAxis, yXis, zAxis).
	 * @param positionInLocalCoord
	 * @return
	 */
	public double[] transformCoordinatesLocalToGlobal(double[] positionInLocalCoord){
		double[] glob = cordSys.transformCoordinatesLocalToGlobal(positionInLocalCoord);
		return add(glob, massLocation);
	}

	// L -> P
	/**
	 * Returns the position in spherical coordinates (r,phi,theta)
	 * of a point expressed in the local coordinate system (xAxis, yXis, zAxis).
	 * @param positionInLocalCoord
	 * @return
	 */
	public double[] transformCoordinatesLocalToPolar(double[] positionInLocalCoordinates){
		return new double[] {
				Math.sqrt(positionInLocalCoordinates[0]*positionInLocalCoordinates[0] + positionInLocalCoordinates[1]*positionInLocalCoordinates[1] + positionInLocalCoordinates[2]*positionInLocalCoordinates[2]),
				Math.atan2(Math.sqrt(positionInLocalCoordinates[0]*positionInLocalCoordinates[0] + positionInLocalCoordinates[1]*positionInLocalCoordinates[1]) , positionInLocalCoordinates[2]),
				Math.atan2(positionInLocalCoordinates[1],positionInLocalCoordinates[0])
		};

	}
	// P -> L
	/**
	 * Returns the position in the local coordinate system (xAxis, yXis, zAxis) 
	 * of a point expressed in spherical coordinates (r,phi,theta).
	 * @param positionInLocalCoord
	 * @return
	 */
	public double[] transformCoordinatesPolarToLocal(double[] positionInPolarCoordinates){

		return new double[] {
				positionInPolarCoordinates[0]*Math.cos(positionInPolarCoordinates[2])*Math.sin(positionInPolarCoordinates[1]),
				positionInPolarCoordinates[0]*Math.sin(positionInPolarCoordinates[2])*Math.sin(positionInPolarCoordinates[1]),
				positionInPolarCoordinates[0]*Math.cos(positionInPolarCoordinates[1])
		};

	}

	@Override
	public double[] transformCoordinatesPolarToGlobal(double[] positionInPolarCoordinates) {

		double x = positionInPolarCoordinates[0]*Math.cos(positionInPolarCoordinates[2])*Math.sin(positionInPolarCoordinates[1]);
		double y = positionInPolarCoordinates[0]*Math.sin(positionInPolarCoordinates[2])*Math.sin(positionInPolarCoordinates[1]);
		double z = positionInPolarCoordinates[0]*Math.cos(positionInPolarCoordinates[1]);

		return Matrix.add(massLocation,cordSys.transformCoordinatesLocalToGlobal(new double[]{x,y,z}));
	}


	@Override
	public double[] transformCoordinatesGlobalToPolar(double[] positionInGlobalCoordinates) {
		double[] vectorToPoint = subtract(positionInGlobalCoordinates,massLocation);
		return cordSys.transformToPolar(vectorToPoint);
	}


	@Override
	public double[] getUnitNormalVector(double[] positionInPolarCoordinates) {
		double[] positionInLocalCoordinates = transformCoordinatesPolarToLocal(positionInPolarCoordinates);
		return normalize(cordSys.transformCoordinatesLocalToGlobal(positionInLocalCoordinates)); 
	}


	@Override
	public CellElement getCellularElement() {
		return getSomaElement();
	}


	@Override
	public boolean isRelative(PhysicalObject po) {
		if(daughtersCoord.containsKey(po))
			return true;		
		return false;
	}


	@Override
	public double getLength() {

		return getDiameter();
	}

	public void applyChanges(PhysicalNode n)
	{
		super.applyChanges(n);
		PhysicalSphere node = (PhysicalSphere) n;
		this.rotationalInertia = node.rotationalInertia;
		this.interObjectForceCoefficient = node.getInterObjectForceCoefficient();
	}

	public void removeLocally() {

		ECM.getInstance().removePhysicalSphere(this);
		if(this.somaElement!=null) somaElement.removeLocally();
		if(this.elsoma!=null)elsoma.removeLocally();
	}

	public void installLocally() {
		ECM.getInstance().addPhysicalSphere(this);
		if(this.somaElement!=null) somaElement.installLocally();
		if(this.elsoma!=null)elsoma.removeLocally();
	}


	@Override
	public PhysicalCylinder getAsPhysicalCylinder() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public PhysicalSphere getAsPhysicalSphere() {
		// TODO Auto-generated method stub
		return this;
	}




	@Override
	public double getVolume() {
		// TODO Auto-generated method stub
		return (4.0/3.0) * Math.PI * diam*diam*diam/(2*2*2);
	}


	@Override
	public void setVolume(double volume) {
		setDiameter(Math.cbrt(volume * 1.90985932));   		// 1.90985932 = 6/pi
	}


	@Override
	public void deserialize(DataInputStream is) throws IOException {
		super.deserialize(is);
		int to = is.readInt();
		for (int i=0;i<to;i++) {
			SpaceNodeFacade s = new SpaceNodeFacade(this.soNode.getObjectRef());
			s.deserialize(is);
			double d [] = new double[3];
			d[0] = is.readDouble();
			d[1] = is.readDouble();
			d[2] = is.readDouble();
			daughtersCoord.put(s, d);
		}
		this.rotationalInertia = is.readDouble();
		this.interObjectForceCoefficient = is.readDouble();
		somaElement = new SomaElement();
		somaElement.deserialize(is);
		somaElement.setPhysicalSphere(this);
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		super.serialize(os);

		os.writeInt(daughtersCoord.size());
		for (SpaceNodeFacade o: daughtersCoord.keySet()) {
			o.serialize(os);
			double d [] = daughtersCoord.get(o);
			os.writeDouble(d[0]);
			os.writeDouble(d[1]);
			os.writeDouble(d[2]);
		}
		os.writeDouble(rotationalInertia);
		os.writeDouble(interObjectForceCoefficient);
		somaElement.serialize(os);
		
	}





	@Override
	public double[] getLastForce() {
		// TODO Auto-generated method stub
		if(futureMovement==null) return new double[]{Double.NaN,Double.NaN,Double.NaN};
		return futureMovement;
	}

	public void checkHostDependence() {
		super.checkHostDependence();

		for(SpaceNodeFacade n: this.daughtersCoord.keySet())
		{
			String host = n.getObjectRef().getPartitionId().getHost();
			if(!host.equals(this.getHost()))
			{
				if(!dependingHosts.contains(host))
					dependingHosts.add(host);
			}
		}

	}

	public ArrayList<PhysicalNode> getDependingNodes() {
		ArrayList<PhysicalNode> list =  super.getDependingNodes();
		for(SpaceNodeFacade n: this.daughtersCoord.keySet())
		{
			String host = n.getObjectRef().getPartitionId().getHost();
			if(!host.equals(this.getHost()))
			{
				if(!dependingHosts.contains(host))
					list.add(n.getUserObject());
			}
		}
		return list;
	}





	
	
	public ElectroPhysiolgySoma getElectroPhysiolgy() {
		// TODO Auto-generated method stub
		return elsoma;
	}

	public void setElectroPhysiolgy(
			ElectroPhysiolgySoma electroPhysiolgyNeurite) {
		elsoma = electroPhysiolgyNeurite;
		elsoma.installLocally();
		
	}


}
