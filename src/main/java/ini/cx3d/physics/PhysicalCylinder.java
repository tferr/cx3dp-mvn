package ini.cx3d.physics;



import static ini.cx3d.utilities.Matrix.add;
import static ini.cx3d.utilities.Matrix.angleRadian;
import static ini.cx3d.utilities.Matrix.crossProduct;
import static ini.cx3d.utilities.Matrix.dot;
import static ini.cx3d.utilities.Matrix.norm;
import static ini.cx3d.utilities.Matrix.normalize;
import static ini.cx3d.utilities.Matrix.perp3;
import static ini.cx3d.utilities.Matrix.printlnLine;
import static ini.cx3d.utilities.Matrix.projectionOnto;
import static ini.cx3d.utilities.Matrix.randomNoise;
import static ini.cx3d.utilities.Matrix.scalarMult;
import static ini.cx3d.utilities.Matrix.subtract;
import ini.cx3d.Param;
import ini.cx3d.biology.CellElement;
import ini.cx3d.biology.LocalBiologyModule;
import ini.cx3d.biology.NeuriteElement;
import ini.cx3d.electrophysiology.ElectroPhysiolgyNeurite;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.simulation.ECM;
import ini.cx3d.spacialOrganisation.ISpacialRepesentation;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;
import ini.cx3d.utilities.Matrix;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;




/**
 * A cylinder can be seen as a normal cylinder, with two end points and a diameter. It is oriented; 
 * the two points are called proximal and distal. The PhysicalCylinder is be part of a tree-like 
 * structure with (one and only) one Physical object at its proximal point and (up to) two physical Objects at
 * its distal end. If there is only one daughter,
 * it is the left one. If <code>daughterLeft == null</code>, there is no distal cylinder (this
 * is a terminal cylinder). The presence of a <code>daugtherRight</code> means that this branch has a bifurcation
 * at its distal end.
 * <p>
 *
 * All the mass of this cylinder is concentrated at the distal point. Only the distal end is moved
 * by a PhysicalCylinder. All the forces in a cylinder that are applied to the proximal node (belonging to the 
 * mother PhysicalNode) are transmitted to the mother element
 * 
 * @author fredericzubler
 *
 */
public class PhysicalCylinder extends PhysicalObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9149947761077228424L;

	/* Local biology object associated with this PhysicalCylinder.*/
	private NeuriteElement neuriteElement = null;
	private ElectroPhysiolgyNeurite elneurite = null;

	/* Parent node in the neuron tree structure (can be PhysicalSphere or PhysicalCylinder)*/  
	private SpaceNodeFacade motherNode = null;
	/* First child node in the neuron tree structure (can only be PhysicalCylinder)*/
	private SpaceNodeFacade daughterLeftNode = null;   
	/* Second child node in the neuron tree structure. (only PhysicalCylinder) */
	private SpaceNodeFacade daughterRightNode = null; 


	/* The part of the inter-object force transmitted to the mother (parent node) -- c.f. runPhysics() */
	private double[] forceToTransmitToProximalMass = {0.0, 0.0, 0.0}; 

	/* Spring constant per distance unit (springConstant * restingLength  = "real" spring constant).*/
	private double springConstant = Param.NEURITE_DEFAULT_SPRING_CONSTANT;
	/* The length of the internal spring where tension would be zero. */
	private double tension= 0;



	/** No argument constructor, initializing fields of <code>PhysicalObject</code> 
	 * with <code>Param</code> values.*/ 
	public PhysicalCylinder() {
		super();

		super.adherence = 	Param.NEURITE_DEFAULT_ADHERENCE; 
		super.mass = 		Param.NEURITE_DEFAULT_MASS; 
		super.setDiameter(Param.NEURITE_DEFAULT_DIAMETER); 

	}

	/** Returns a <code>PhysicalCylinder</code> with all fields similar than in this 
	 * <code>PhysicalCylinder</code>. Note that the relatives in the tree structure, the
	 * tension, the volume, the  
	 * <code>CellElement</code>, as well as<code>Excrescences</code> and the 
	 * <code>IntracellularSubstances</code> are not copied. */
	public PhysicalCylinder getCopy(){

		PhysicalCylinder newCylinder = new PhysicalCylinder();
		// PhysicalObject variables

		newCylinder.adherence =  adherence;
		newCylinder.mass = mass;
		newCylinder.diam = diam;  // re - computes also volumes
		newCylinder.color = color;

		newCylinder.cordSys = cordSys.getCopy(); 
		// this class variable	
		//		newCylinder.setSpringAxis(getSpringAxis().clone());
		newCylinder.setInterObjectForceCoefficient(this.getInterObjectForceCoefficient());
		newCylinder.springConstant = this.springConstant;

		return newCylinder;
	}



	// *************************************************************************************
	// *      METHODS FOR NEURON TREE STRUCTURE                                            *
	// *************************************************************************************



	/**
	 * Returns true if the <code>PhysicalObject</code> given as argument is a mother, daughter
	 * or sister branch.*/
	@Override
	public boolean isRelative(PhysicalObject po) {

		// mother-daughter
		if( po.soNode.getID()==motherNode.getID())
			return true;
		if(daughterLeftNode!=null && po.soNode.getID()==daughterLeftNode.getID())
			return true;
		if(daughterRightNode!=null && po.soNode.getID()==daughterRightNode.getID())
			return true;
		// sister-sister
		if(po.isAPhysicalCylinder()){
			if(((PhysicalCylinder)po).motherNode.getID() == this.motherNode.getID())
				return true;
		}
		return false;

	}

	/**
	 * Returns the location in absolute coordinates of where the <code>PhysicalObject</code> 
	 * given as argument is attached on this where the <code>PhysicalCylinder</code>  
	 * If the argument is one of our daughter <code>PhysicalCylinder</code>, the point mass location
	 * is returned. Otherwise, the return is <code>null</code>.
	 * 
	 * @param daughterWhoAsks the PhysicalObject requesting it's origin.
	 * 
	 */
	@Override
	public double[] originOf(PhysicalObject daughterWhoAsks) {
		// TODO : consider remove the check
		double [] result=null;

		if(daughterWhoAsks.soNode.equals(daughterLeftNode) || daughterWhoAsks.soNode.equals(daughterRightNode)){
			result =  getSoNode().getPosition();
		}
		else
		{
			OutD.println(this +" PhysicalCylinder.getOrigin() says : this is not one of my relatives !!! +Thread.currentThread().getId()"+Thread.currentThread().getId());
			//	ShowConsoleOutput.println(" this id:"+ this.getID()+" daughterleft:"+this.daughterLeftNode+" "+daughterLeftNode.getID());
			//			ShowConsoleOutput.println(" this daughterwho asks:"+daughterWhoAsks.getID()+" ");
			//ShowConsoleOutput.println(" mother"+this.motherNode.getID());
			throw new RuntimeException("not my daughter!");
			//result= null;
		}

		return result;
	}


	@Override
	protected void removeDaugther(PhysicalObject daughterToRemove) {
		// If there is another daughter than the one we want to remove,
		// we have to be sure that it will be the daughterLeft.

		SpaceNodeFacade s = daughterToRemove.soNode;


		if(daughterLeftNode !=null && s.getID() == daughterLeftNode.getID()){

			daughterLeftNode = daughterRightNode;
			daughterRightNode = null;

			return;
		}

		if(daughterRightNode !=null && s.getID() == daughterRightNode.getID()){

			daughterRightNode = null;

			return;
		}

		OutD.println("PhysicalCylinder.daughterToRemove() says : this is not one of my relatives !!!");
		(new Throwable()).printStackTrace();
	}

	@Override
	protected void updateRelative(PhysicalObject oldRelative, PhysicalObject newRelative) {

		if(oldRelative.soNode.getID() == motherNode.getID()){
			setMother(newRelative);
		}
		else if(daughterLeftNode!=null && oldRelative.soNode.getID() == daughterLeftNode.getID()){
			setDaughterLeft((PhysicalCylinder) newRelative);
		}
		else if(daughterRightNode!=null && oldRelative.soNode.getID() == daughterRightNode.getID()){
			setDaughterRight((PhysicalCylinder) newRelative);
		}
		else
		{
			throw new RuntimeException("this is not one of my relatives");
		}

	}

	/**
	 * returns the total force that this <code>PhysicalCylinder</code> exerts on it's mother.
	 * It is the sum of the spring force an the part of the inter-object force computed earlier in
	 * <code>runPhysics()</code>.
	 */
	double[] forceTransmittedFromDaugtherToMother(PhysicalObject motherWhoAsks) {

		if(motherNode == null)
		{
			setMother(motherWhoAsks);
		}


		if(motherWhoAsks.soNode.getID() != motherNode.getID()){
			OutD.println("PhysicalCylinder.forceTransmittedFromDaugtherToMother() says : this is not my mother !!! thread id:"+Thread.currentThread().getId()+ " mother id:"+this.motherNode.getID()+" this id:"+soNode.getID()+" daughterleft:"+this.daughterLeftNode);
			return new double[] {0,0,0}; // relative has changed or is not yet initialized therefore 0,0,0 is all right it's just force

		}
		// The inner tension is added to the external force that was computed earlier.
		// (The reason for dividing by the actualLength is to normalize the direction : T = T * axis/ (axis length)
		if(getTension() >0)
		{
			System.out.println("asdfasdf");
		}
		double tension = getTension();
		double actualLength = getActualLength();
		double factor =  tension/actualLength;
		if(factor<0){
			factor = 0;
		}

		double [] result =  new double[] { 	factor*getSpringAxis()[0] + forceToTransmitToProximalMass[0], 
				factor*getSpringAxis()[1] + forceToTransmitToProximalMass[1],
				factor*getSpringAxis()[2] + forceToTransmitToProximalMass[2]  } ;
		if(Double.isNaN(result[1]))
		{
			OutD.println("nana!!!!!");
		}

		return result;
	}




	// *************************************************************************************
	//   DISCRETIZATION , SPATIAL NODE, CELL ELEMENT
	// *************************************************************************************


	/** 
	 * Checks if this <code>PhysicalCylinder</code> is either too long (and in this case it will insert 
	 * another <code>PhysicalCylinder</code>), or too short (and in this second case fuse it with the
	 * proximal element or even delete it).
	 * */
	public boolean runDiscretization() {
		if(this.daughterLeftNode!=null) return false;
		double max_length = 0;
		if(this.getMother() ==null)
		{
			prefetchDependencies();
			System.out.println("test");
			this.setColor(Color.green);
			return false;
		}
		
		double forcedist =  0.5*(this.getMother().getDiameter()*getMother().getInterObjectForceCoefficient()+this.getDiameter()*this.getInterObjectForceCoefficient());
		
		max_length = Math.max(forcedist, Param.NEURITE_MAX_LENGTH);
		double min_length = max_length*0.01;
		if(getActualLength()>max_length){
			if(daughterLeftNode== null){   // if terminal branch : 
				Object element = insertProximalCylinder(0.5);
				if(element ==null) return false;
			}else if(getMother().isAPhysicalSphere()){ //if initial branch :
				Object element = insertProximalCylinder(0.9);
				if(element ==null) return false;
			}else{
				Object element = insertProximalCylinder(0.5);
				if(element ==null) return false;
			}
			return true;
		}

		return false;
		
	}

	
	public boolean checkRetraction()
	{
		if(true)return false; 
		double max_length = 0;
		double forcedist =  0.5*(this.getMother().getDiameter()*getMother().getInterObjectForceCoefficient()+this.getDiameter()*this.getInterObjectForceCoefficient());
		max_length = Math.max(forcedist, Param.NEURITE_MAX_LENGTH);
		double min_length = max_length*0.01;
		
		if(this.getDaughterLeft() != null && this.getDaughterLeft().getDaughterLeft() == null)
		{	
			if(!this.daughterLeftNode.isLocal()) return false;
			double [] daughterleft = getDaughterLeft().massLocation;
			double [] thismass = this.massLocation;
			if(Matrix.distance(daughterleft,thismass)>min_length) return false;
			if(this.getDaughterRight() ==null)
			{
		
				for (IntracellularSubstance s : this.getIntracellularSubstances().values() ) {
					this.getDaughterLeft().modifyIntracellularQuantity(s.getId(), s.getQuantity()/Param.SIMULATION_TIME_STEP);
				}
				PhysicalCylinder s = this.getAsPhysicalCylinder();
				for(LocalBiologyModule a:this.getDaughterLeft().getNeuriteElement().getLocalBiologyModulesList())
				{
					s.getNeuriteElement().addLocalBiologyModule(a);
		
				}
				this.setMassLocation(daughterLeftNode.getPosition());
				getDaughterLeft().removeCauseOfRetraction();
				this.daughterLeftNode = null;
				this.fetchedDaughterLeft = null;
				return true;
			}
			else
			{
				this.setMassLocation(daughterLeftNode.getPosition());
				getDaughterLeft().removeCauseOfRetraction();
				this.daughterLeftNode = this.daughterRightNode;
				this.fetchedDaughterRight = this.fetchedDaughterLeft;
				this.daughterRightNode = null;
				this.fetchedDaughterRight = null;
				return true;
			}
			
		}
		else if(this.getDaughterRight() != null && this.getDaughterRight().getDaughterLeft() == null)
		{
			if(!this.daughterRightNode.isLocal()) return false;
			double [] daughterright = getDaughterRight().massLocation;
			double [] thismass = this.massLocation;
			if(Matrix.distance(daughterright,thismass)>min_length) return false;
			getDaughterRight().removeCauseOfRetraction();
			this.daughterLeftNode = null;
			this.fetchedDaughterLeft = null;
			return true;
			
		}
		return false;
	}
	
	private boolean removed = false;
	
	public void removeCauseOfRetraction()
	{
		removed = true;
	}
	

	/**
	 * Divides the PhysicalCylinder into two PhysicalCylinders of equal length. The one in which the method is called becomes the distal half.
	 * A new PhysicalCylinder is instantiated and becomes the proximal part. All characteristics are transmitted.
	 * A new Neurite element is also instantiated, and assigned to the new proximal PhysicalCylinder
	 */
	private NeuriteElement insertProximalCylinder(){
		NeuriteElement temp;
		while((temp=insertProximalCylinder(0.5)) == null)
		{
			Thread.yield();
		}
		return temp;
	}

	//private SpatialOrganizationNode<PhysicalNode> newSONinpreparation = null;
	/**
	 * Divides the PhysicalCylinder into two PhysicalCylinders (in fact, into two instances of the derived class). 
	 * The one in which the method is called becomes the distal half, and it's length is reduced.
	 * A new PhysicalCylinder is instantiated and becomes the proximal part (=the mother). All characteristics are transmitted
	 * 
	 *@param distalPortion the fraction of the total old length devoted to the distal half (should be between 0 and 1).
	 */
	private NeuriteElement insertProximalCylinder(double distalPortion){ 

		// creating a new PhysicalCylinder & a new NeuriteElement, linking them together
		NeuriteElement ne = createExactCopy();
		//soNode.getPosition()
		double [] dir = Matrix.subtract(massLocation,motherNode.getPosition());
		double [] currentpos = massLocation;
		NeuriteElement thisne = this.neuriteElement;
		PhysicalCylinder newCylinder = ne.getPhysicalCylinder();
		PhysicalCylinder thisCylinder = thisne.getPhysicalCylinder();
		// familly relations
		thisCylinder.setDaughterLeft(newCylinder);
		newCylinder.setMother(thisCylinder);

		//remove wrong modules
		ArrayList<LocalBiologyModule> lbs = (ArrayList<LocalBiologyModule>) thisne.getLocalBiologyModulesList().clone();
		for(int i=0;i<lbs.size();i++)
		{
			ne.addLocalBiologyModule(lbs.get(i));	
		}
		for(int i=0;i<lbs.size();i++)
		{
			LocalBiologyModule m = lbs.get(i);
			if(!m.isCopiedWhenNeuriteElongates())
			{
				
				thisne.removeLocalBiologyModule(lbs.get(i));
			}
		}





		double [] newpos =  Matrix.scalarMult(0.5,dir);

		double [] pMassLocation = subtract(currentpos,newpos);
		double [] dMassLocation = currentpos;

		thisCylinder.setMassLocation(pMassLocation);
		thisCylinder.setTension(this.getTension());
		newCylinder.setMassLocation(dMassLocation);
		newCylinder.setTension(this.getTension());
	
		




		// registering the new cylinder with ecm


		//	ShowConsoleOutput.println("new cyilinder id:="+newProximalCylinder.soNode.getID());
		//	ShowConsoleOutput.println("its mother cyilinder id:="+newProximalCylinder.motherNode.getID());

		for (String key : thisCylinder.intracellularSubstances.keySet()) {

			Substance subofNew = newCylinder.intracellularSubstances.get(key); 
			Substance thissub =  thisCylinder.intracellularSubstances.get(key);

			double quantityBeforeDistribution = thissub.getQuantity();

			subofNew.setQuantity(quantityBeforeDistribution*distalPortion);
			thissub.setQuantity(quantityBeforeDistribution*(1-distalPortion));
		}



		ECM.getInstance().addPhysicalCylinder(newCylinder);

		thisCylinder.prefetchDependencies();
		if(thisCylinder.fetchedMother==null) 
		{
			System.out.println("did not work");
		}
		newCylinder.prefetchDependencies();
		if(newCylinder.fetchedMother==null) 
		{
			System.out.println("did not work");
		}


		return ne;
	}



	private NeuriteElement createExactCopy() {
		PhysicalCylinder newProximalCylinder = getCopy();

		SpaceNodeFacade newSONinpreparation=null;

		newSONinpreparation = soNode.getNewInstance(this.soNodePosition().clone(), newProximalCylinder);

		newProximalCylinder.setSoNode(newSONinpreparation);

		NeuriteElement thisneurite = this.neuriteElement;

		NeuriteElement ne = thisneurite.getCopy();
		ne.setPhysicalAndInstall(newProximalCylinder);




		for (IntracellularSubstance s : intracellularSubstances.values()) {
			IntracellularSubstance s2 = (IntracellularSubstance)s.getCopy();
			s2.setQuantity(s.getQuantity());
			newProximalCylinder.addNewIntracellularSubstance(s2);
		}



		return ne;
	}



	//	/**
	//	 * Repositioning of the SpatialNode location (usually a Delaunay vertex) at the barycenter of the cylinder. 
	//	 * If it is already closer than a quarter of the diameter of the cylinder, it is not displaced.
	//	 */
	//	void updateSpatialOrganizationNodePosition() {
	//		
	//		
	//		
	//	
	//		double[] currentSpatialNodePosition = soNode.getPosition();
	//		
	//		double displacementOfTheCenter[] = new double[] {	massLocation[0] - 0.5*springAxis[0] - currentSpatialNodePosition[0],
	//				massLocation[1] - 0.5*springAxis[1] - currentSpatialNodePosition[1],
	//				massLocation[2] - 0.5*springAxis[2] - currentSpatialNodePosition[2]  };
	//		double diameter = this.getDiam();
	//		
	//		// to save time in SOM operation, if the displacement is very small, we don't do it
	//		if(norm(displacementOfTheCenter)<diameter/4.0)
	//			return;
	//		// To avoid perfect alignment (pathologic position for Delaunay, eg), we add a small jitter
	//		// TODO remove next line when we have Dennis'stable Delaunay
	//		displacementOfTheCenter = add(displacementOfTheCenter,randomNoise(diameter/4.0, 3));
	//		
	//			// Tell the node to move
	//		try{
	//			soNode.moveFrom(displacementOfTheCenter);	
	//		
	//		} catch (PositionNotAllowedException e) {
	//			e.printStackTrace();
	//		}
	//	
	//
	//	}


	// *************************************************************************************
	//   ELONGATION, RETRACTION, BRANCHING
	// *************************************************************************************

	public void setTension(double d) {

		// T = k*(A-R)/R --> R = k*A/(T+K)
		this.tension = d;
		if(this.tension-10>0.01)
		{
			OutD.println("tension");
		}
		//		if(this.tension<0)
		//		{
		//			OutD.println("whoot");
		//		}
		//		if(this.tension < 0)
		//		{
		//			this.tension = 0;
		//		}

	}

	/** Method used for active extension of a terminal branch, representing the steering of a 
	 * growth cone. The movement should always be forward, otherwise no movement is performed.
	 * 
	 * @param speed of the growth rate (microns/hours).
	 * @direction the 3D direction of movement.
	 */
	public void extendCylinder(double speed, double[] direction){

		double temp = dot(direction, getSpringAxis());

		if(  temp> 0 )  {	
			movePointMass(speed, direction);
		}
	}

	/** Method used for active extension of a terminal branch, representing the steering of a 
	 * growth cone. There is no check for real extension (unlike in extendCylinder() ).
	 * 
	 * @param speed of the growth rate (microns/hours).
	 * @direction the 3D direction of movement.
	 */
	public void movePointMass(double speed, double[] direction){
		// check if is a terminal branch
		if(daughterLeftNode != null){      
			return;
		}
		// scaling for integration step
		double length = speed*Param.SIMULATION_TIME_STEP;
		direction = normalize(direction);
		double displacement[] = Matrix.scalarMult(length, direction);

		setMassLocation(add(displacement, getInternalMassLocation()));


		// process of elongation : setting tension to 0 increases the restingLength :
		setRestingLengthToSetTension(0.0);

		// Make sure I'll be updated when I run my physics
		// but since I am actually moving, I have to update the neighbors 
		// (the relative would probably not be needed...).
		//scheduleMeAndAllMyFriends();
	}

	public double[] getMyOrigin()
	{
		return motherNode.getPosition();
	}

	/**
	 * Branch retraction by moving the distal end (i.e. the massLocation) toward the
	 * proximal end (the mother), maintaining the same tension in the PhysicalCylinder. The method
	 * shortens the actual and the resting length so that the result is a shorter
	 * cylinder with the same tension.
	 * - If this PhysicalCylinder is longer than the required shortening, it simply retracts. 
	 * - If it is shorter and its mother has no other daughter, it merges with it's mother and 
	 * the method is recursively called (this time the cylinder length is bigger because we have 
	 * a new PhysicalCylinder that resulted from the fusion of two).
	 * - If it is shorter and either the previous PhysicalCylinder has another daughter or the 
	 * mother is not a PhysicalCylinder, it disappears.
	 * 
	 * @param speed of the retraction (microns/hours).
	 * @return false if the neurite doesn't exist anymore (complete retraction)
	 */
	public boolean retractCylinder(double speed) {

		double[] dir = Matrix.subtract(this.getMother().getMassLocation(),this.getMassLocation());
		double length = Matrix.norm(dir);
		dir = Matrix.normalize(dir);

		if(daughterLeftNode != null){      
			return true;
		}
		speed = speed*Param.SIMULATION_TIME_STEP;

		speed = Math.min(speed,length);
		double pos[] = Matrix.add(Matrix.scalarMult(speed, dir),getMassLocation());
		setMassLocation(pos);
		return true;
	}


	/** 
	 * Bifurcation of the growth cone creating : adds the 2 <code>PhysicalCylinder</code> that become
	 * daughter left and daughter right
	 * @param length the length of the new branches 
	 * @param direction_1 of the first branch (if 
	 * @param newBranchL
	 * @param newBranchR
	 */

	public PhysicalCylinder[] bifurcateCylinder(double length, double[] direction_1, double[] direction_2) {
		// check it is a terminal branch
		if (daughterLeftNode != null){
			OutD.println("Not a terminal Branch");
			return null;
		}
		// create the cylinders
		PhysicalCylinder newBranchL = this.getCopy();
		PhysicalCylinder newBranchR = this.getCopy();


		// check that the directions are not pointing backwards
		if( angleRadian(getSpringAxis(), direction_1)>Math.PI*0.5){
			double[] proj = projectionOnto(direction_1, getSpringAxis());
			proj = scalarMult(-1,proj);
			direction_1 = add(direction_1, proj);
		}
		if( angleRadian(getSpringAxis(), direction_2)>Math.PI*0.5){
			double[] proj = projectionOnto(direction_2, getSpringAxis());
			proj = scalarMult(-1,proj);
			direction_2 = add(direction_2, proj);
		}






		// spatial organization node
		//		double[] newBranchCenterLocation = add(this.massLocation, scalarMult(0.5,newBranchL.springAxis));
		SpaceNodeFacade newSON = null;

		newSON = this.soNode.getNewInstance(this.soNode.getPosition().clone(), newBranchL);
		
		newBranchL.setSoNode(newSON);
		newBranchL.cordSys.turnToDirection(direction_1);
		//		newBranchCenterLocation = add(this.massLocation, scalarMult(0.5,newBranchR.springAxis));

		newSON = this.soNode.getNewInstance(this.soNode.getPosition().clone(), newBranchR);

		newBranchR.setSoNode(newSON);
		newBranchR.cordSys.turnToDirection(direction_2);
		// set family relations

		this.setDaughterLeft(newBranchL);
		newBranchL.setMother(this);
		this.setDaughterRight(newBranchR);
		newBranchR.setMother(this);



		// mass location and spring axis
		//		newBranchL.setSpringAxis(scalarMult(length,normalize(direction_1)));
		newBranchL.setMassLocation( add(this.getInternalMassLocation(), newBranchL.getAxis()) );

		//		newBranchR.setSpringAxis(scalarMult(length,normalize(direction_2)));
		newBranchR.setMassLocation( add(this.getInternalMassLocation(), newBranchR.getAxis()) );

		// physics of tension : 
		newBranchL.setActualLength(length);
		newBranchR.setActualLength(length);
		newBranchR.setRestingLengthToSetTension(Param.NEURITE_DEFAULT_TENSION);
		newBranchL.setRestingLengthToSetTension(Param.NEURITE_DEFAULT_TENSION);




		// i'm scheduled to run physics next time :
		// (the daughters automatically are too, because they are new PhysicalObjects)
		super.setOnTheSchedulerListForPhysicalObjects(true);



		//newBranchL.updateDependentPhysicalVariables();
		//newBranchR.updateDependentPhysicalVariables();
		PhysicalCylinder newBranch = newBranchL;
		//		newBranch.setSpringAxis(Matrix.subtract(newBranch.getInternalMassLocation(),this.getInternalMassLocation()));
		newBranch.setActualLength(Math.sqrt(newBranch.getSpringAxis()[0]*newBranch.getSpringAxis()[0] + newBranch.getSpringAxis()[1]*newBranch.getSpringAxis()[1] +newBranch. getSpringAxis()[2]*newBranch.getSpringAxis()[2]));
		newBranch.setInterObjectForceCoefficient(this.getInterObjectForceCoefficient());

		newBranch = newBranchR;

		//		newBranch.setSpringAxis(Matrix.subtract(newBranch.getInternalMassLocation(),this.getInternalMassLocation()));
		newBranch.setActualLength(Math.sqrt(newBranch.getSpringAxis()[0]*newBranch.getSpringAxis()[0] + newBranch.getSpringAxis()[1]*newBranch.getSpringAxis()[1] +newBranch. getSpringAxis()[2]*newBranch.getSpringAxis()[2]));
		newBranch.setInterObjectForceCoefficient(this.getInterObjectForceCoefficient());


		// register the new branches with ecm
		ECM.getInstance().addPhysicalCylinder(newBranchL);
		ECM.getInstance().addPhysicalCylinder(newBranchR);

		//		ShowConsoleOutput.println("PhysicalCylinder.bifurcateCylinder() "+newBranchL.soNode.getID());
		//newBranchL.updateSpatialOrganizationNodePosition(); //move to right pos
		//		ShowConsoleOutput.println("PhysicalCylinder.bifurcateCylinder() "+newBranchR.soNode.getID());
		//newBranchR.updateSpatialOrganizationNodePosition();

		PhysicalCylinder[] t=new PhysicalCylinder[0];

		t = new PhysicalCylinder[] {newBranchL, newBranchR};
		return t;
	}



	/**
	 * Makes a side branching by adding a second daughter to a non terminal <code>PhysicalCylinder</code>.
	 * The new <code>PhysicalCylinder</code> is perpendicular to the mother branch.
	 * @param direction the direction of the new neuriteLement (But will be automatically corrected if
	 * not al least 45 degrees from the cylinder's axis).
	 * @return the newly added <code>NeuriteSegment</code>
	 */
	public PhysicalCylinder branchCylinder(double length, double[] direction) {
		// we first split this cylinder into two pieces
		NeuriteElement ne = insertProximalCylinder(0.1);
		// then append a "daughter right" between the two
		return this.extendSideCylinder(length, direction);
		// ne.getPhysicalCylinder().extendSideCylinder(length, direction);
	}


	private PhysicalCylinder extendSideCylinder(double length, double[] direction) {
		PhysicalCylinder newBranch = this.getCopy();
		SpaceNodeFacade newSON = null;

		newSON = soNode.getNewInstance(soNode.getPosition().clone(), newBranch);

		newBranch.setSoNode(newSON);
		newBranch.setMother(this);
		newBranch.cordSys.turnToDirection(direction);
		if(direction == null){
			direction = add( perp3(getSpringAxis()), randomNoise(0.1, 3) );    
		}else{
			// check that the direction is at least 45 degrees from the branch axis
			// TODO : better method ! 
			double angleWithSideBranch = angleRadian(getSpringAxis(), direction);
			if(angleWithSideBranch<0.78 || angleWithSideBranch > 2.35){  // 45-135 degrees
				double[] p = crossProduct(getSpringAxis(), direction);
				p = crossProduct(p, getSpringAxis());
				direction = add(normalize(direction), normalize(p));
			}
		}
		// location of mass and computation center
		double[] newBranchSpringAxis = scalarMult(length, normalize(direction)); 
		double[] newBranchMassLocation = add(this.getInternalMassLocation(), newBranchSpringAxis);
		newBranch.setMassLocation(newBranchMassLocation);
		newBranch.setRestingLengthToSetTension( Param.NEURITE_DEFAULT_TENSION); 
		newBranch.setDiameter(Param.NEURITE_DEFAULT_DIAMETER, true);
		// new CentralNode
		//		double[] newBranchCenterLocation = add(this.massLocation, scalarMult(0.5,newBranchSpringAxis));


		// family relations


		this.setDaughterRight(newBranch);


		// correct physical values (has to be after family relations and SON assignement).
		//		newBranch.updateDependentPhysicalVariables();



		// register to ecm
		ECM.getInstance().addPhysicalCylinder(newBranch);
		//		ShowConsoleOutput.println("PhysicalCylinder.extendSideCylinder() "+newBranch.soNode.getID());

		// i'm scheduled to run physics next time :
		// (the side branch automatically is too, because it's a new PhysicalObject)
		setOnTheSchedulerListForPhysicalObjects(true);
		return newBranch;
	}


	public void setRestingLengthToSetTension(double tensionWeWant){
		setTension(tensionWeWant);
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

	}

	/**
	 * Progressive modification of the diameter. Updates the volume, the intracellular concentration 
	 * @param speed micron/ h
	 */
	public void changeDiameter(double speed) {
		//scaling for integration step

		double dD = speed*(Param.SIMULATION_TIME_STEP);
		setDiameter(getDiameter() + dD);
		if(getDiameter() < 0.01 ){
			System.err.println("PhysicalCylinder.changeDiameter() : diameter is "+getDiameter());
		}

	}



	// *************************************************************************************
	//   Physics
	// *************************************************************************************




	/**
	 * 
	 */
	public boolean runPhysics() {

		prefetchDependencies();

		
		if(runDiscretization())
		{
			return false;
		}
		
		if(checkRetraction())
		{
			return false;
		}
		
	


		for(int i=0;i<physicalBonds.size();i++)
		{
			PhysicalBond pb = physicalBonds.get(i);
			if(pb == null ) continue;
			if(pb.checkForBreak())
			{
				this.physicalBonds.remove(i);
			}
		}

		double [] thisspringAxis = this.getSpringAxis().clone();
		double thistensioin =  this.getTension();
		double actualLength =  this.getActualLength();
		final SpaceNodeFacade thisdaughterLeftNode = this.daughterLeftNode;
		final SpaceNodeFacade thisdaughterRightNode = this.daughterRightNode;
		SpaceNodeFacade thismotherNode = this.motherNode;
		ArrayList<PhysicalBond> thisphysicalBonds =  this.physicalBonds!=null? (ArrayList<PhysicalBond>) this.physicalBonds.clone():null;
		PhysicalCylinder getDaughterLeft;
		PhysicalCylinder getDaughterRight;
		getDaughterLeft = getDaughterLeft();
		getDaughterRight = getDaughterRight();



		double h = Param.SIMULATION_TIME_STEP;
		double[] forceOnMyPointMass = {0,0,0};
		double[] forceOnMyMothersPointMass  = {0,0,0};

		// 1) Spring force -------------------------------------------------------------------
		// 		Only the spring of this cylinder. The daughters spring also act on this mass,
		// 		but they are treated in point (2)


		double factor =  -thistensioin/actualLength;  // the minus sign is important because the spring axis goes in the opposite direction

		forceOnMyPointMass= Matrix.add(forceOnMyPointMass,Matrix.scalarMult(factor,thisspringAxis));

		// 2) Force transmitted by daugthers (if they exist) ----------------------------------
		if(getDaughterLeft!=null) {
			double[] forceFromDaughter = getDaughterLeft.forceTransmittedFromDaugtherToMother(this);
			forceOnMyPointMass= Matrix.add(forceOnMyPointMass,forceFromDaughter);
		}



		if(getDaughterRight!=null) {
			double[] forceFromDaughter=getDaughterRight.forceTransmittedFromDaugtherToMother(this);	
			forceOnMyPointMass= Matrix.add(forceOnMyPointMass,forceFromDaughter);	
		} 



		// 1) "artificial force" to maintain the sphere in the ecm simulation boundaries--------
		//		if(ECM.getInstance().getArtificialWallForCylinders()){
		//			double[] forceFromArtificialWall = ECM.getInstance().forceFromArtificialWall(massLocation, getDiameter()*0.5);
		//			forceOnMyPointMass= Matrix.add(forceOnMyPointMass,forceFromArtificialWall);
		//		}

		// 3) Object avoidance force -----------------------------------------------------------
		//	(We check for every neighbor object if they touch us, i.e. push us away)

		for (PhysicalNode neighbor : getNeighboringPhysicalNodes()) {
			// of course, only if it is an instance of PhysicalObject
			if(neighbor.isAPhysicalObject()){
				PhysicalObject n = (PhysicalObject)neighbor;
				// if it is a direct relative, we don't take it into account
				boolean isrelative=  (thismotherNode != null && neighbor.soNode.getID() == thismotherNode.getID());
				isrelative|=(thisdaughterLeftNode != null && neighbor.soNode.getID() == thisdaughterLeftNode.getID());
				isrelative|= (thisdaughterRightNode != null &&  neighbor.soNode.getID() == thisdaughterRightNode.getID());
				if(isrelative) 
					continue;
				// if sister branch, we also don't take into account
				if (neighbor instanceof PhysicalCylinder) {
					PhysicalCylinder nCyl = (PhysicalCylinder) neighbor;
					if(nCyl.motherNode.getID() == thismotherNode.getID()){
						continue;
					}
				}
				// if we have a PhysicalBond with him, we also don't take it into account
				if(thisphysicalBonds != null){
					for (PhysicalBond pb : thisphysicalBonds) {
						if(pb.getOppositePhysicalObject(this) == neighbor){
							continue;
						}
					}
				}
				double [] temp = n.getForceOn(this);
				double[] forceFromThisNeighbor = n.getForceOn(this);

				//ajust for mass relation
				forceFromThisNeighbor = Matrix.scalarMult(n.mass/(this.mass+n.mass), forceFromThisNeighbor);

				forceOnMyPointMass= Matrix.add(forceOnMyPointMass,forceFromThisNeighbor);	


			}
		}

		// 4) PhysicalBond -----------------------------------------------------------
		for (int i=0; i< thisphysicalBonds.size(); i++) {
			PhysicalBond pb = thisphysicalBonds.get(i);
			double[] forceFromThisPhysicalBond = pb.getForceOn(this);

			if(forceFromThisPhysicalBond.length == 3){
				// (if all the force is transmitted to the (distal end) point mass : )
				forceOnMyPointMass= Matrix.add(forceOnMyPointMass,forceFromThisPhysicalBond);	
			}else{												
				// (if there is a part transmitted to the proximal end : )
				double partForMyPointMass = 1.0-forceFromThisPhysicalBond[3];
				double partForMotherPoint = forceFromThisPhysicalBond[3];
				forceOnMyPointMass= Matrix.add(forceOnMyPointMass,Matrix.scalarMult( partForMyPointMass, forceFromThisPhysicalBond));		
				forceOnMyMothersPointMass = Matrix.add(forceOnMyMothersPointMass,Matrix.scalarMult(partForMotherPoint,forceFromThisPhysicalBond));	
			}		
		}

		//		Timer.stop("cyliner_calc");
		//		Timer.start("cyliner_writelock");

		// 6) Compute the movement of this neurite elicited by the resultant force----------------
		// 	6.0) In case we display the force

		//	6.1) Define movement scale 
		double hOverM = h/mass;
		double normOfTheForce = norm(forceOnMyPointMass);
		// 	6.2) If is F not strong enough -> no movements

		// So, what follows is only executed if we do actually move :

		// 	6.3) Since there's going be a move, we calculate it

		double[] displacement = scalarMult(hOverM, forceOnMyPointMass);
		double normOfDisplacement = normOfTheForce*hOverM;

		// 	6.4) There is an upper bound for the movement.
		if(normOfDisplacement > Param.SIMULATION_MAXIMAL_DISPLACEMENT){
			displacement = scalarMult(Param.SIMULATION_MAXIMAL_DISPLACEMENT/normOfDisplacement, displacement);
		}

		double[] actualDisplacement = displacement;

		// 8) Eventually, we do perform the move--------------------------------------------------
		// 8.1) The move of our mass

		futureMovement  = actualDisplacement;

		// 5) define the force that will be transmitted to the mother
		futureForceToTransmitToProximalMass = forceOnMyMothersPointMass;

		// 8.2) Recompute length, tension and re-center the computation node, and redefine axis

		//		Timer.stop("cyliner_shedule");
		return true;
	}


	private transient  double [] futureMovement;
	private transient double [] futureForceToTransmitToProximalMass;
	public void applyPhysicsCalculations()
	{

		if(futureMovement !=null)
		{

			setMassLocation(Matrix.add(this.getInternalMassLocation(),futureMovement));
			this.forceToTransmitToProximalMass = futureForceToTransmitToProximalMass;
			futureForceToTransmitToProximalMass = null;


		}

		if(Matrix.distanceSquare(this.getSoNode().getPosition(),this.getInternalMassLocation())>0)
		{
			if(ECM.getInstance().getArtificialWallForCylinders() && !ignoreboundaries)
			{
				massLocation = ECM.getInstance().forceFromArtificialWall(massLocation, getDiameter()/2);
			}

			updateSpatialOrganizationNodePosition();
			setMassLocation(soNodePosition());
		}
		cordSys.turnToDirection(getSpringAxis());
		applyChangesOnChemicals();
		scheduleMeAndAllMyFriends();
		if(removed)
		{
			this.getNeuriteElement().removeLocally();
			this.removeLocally();
			this.soNode.remove();
		}
	}



	/**
	 * Sets the scheduling flag onTheSchedulerListForPhysicalObjects to true
	 * for me and for all my neighbors, relative, things I share a physicalBond with
	 */
	private void scheduleMeAndAllMyFriends(){
		// me
		//		setOnTheSchedulerListForPhysicalObjects(true);
		//		// relatives :
		//		scheduleCheck(motherNode);
		//		if(daughterLeftNode != null){
		//			scheduleCheck(daughterLeftNode);  
		//			
		//	
		//			if(daughterRightNode != null){
		//				scheduleCheck(daughterRightNode);  
		//			}
		//		}
		//		
		//		// neighbors in the triangulation :
		//		
		//		for (PhysicalNode neighbor : getNeighboringPhysicalNodes()) {
		//			if(neighbor.isAPhysicalObject()){
		//				scheduleCheck(neighbor.soNode);
		//			}
		//		}
		//		for (int i=0; i< this.physicalBonds.size(); i++) {
		//			scheduleCheck(this.physicalBonds.get(i).getOppositeSpatialOrganizationNode(soNode));
		//		}

	}



	@Override
	public double[] getForceOn(PhysicalSphere s) {

		// force from cylinder on sphere
		//		double[] f = Force.forceOnASphereFromACylinder(
		//				s.getMassLocation(),
		//				s.getDiameter()*0.5,
		//				subtract(this.massLocation,this.springAxis),
		//				this.massLocation,
		//				this.springAxis,
		//				this.actualLength,
		//				this.getDiameter() );
		//		return f;
		return interObjectForce.forceOnASphereFromACylinder(s, this);
	}

	@Override
	public double[] getForceOn(PhysicalCylinder c) {

		if(c.motherNode.getID() == this.motherNode.getID()){
			// extremely important to avoid that two sister branches start to
			// interact physically.
			return new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		}
		//		//		double[] f = Force.forceOnACylinderFromACylinder2(
		//		//		subtract(c.massLocation,c.springAxis), c.massLocation, c.diameter,
		//		//		subtract(this.massLocation,this.springAxis), this.massLocation, this.diameter);
		//		double[] f = Force.forceOnACylinderFromACylinder2(
		//				c.proximalEnd(), c.getMassLocation(), c.diameter,
		//				this.proximalEnd(), this.massLocation, this.diameter);
		//		return f;
		return interObjectForce.forceOnACylinderFromACylinder(c, this);

	}

	@Override
	public boolean isInContactWithSphere(PhysicalSphere s) {

		double[] force = PhysicalObject.interObjectForce.forceOnACylinderFromASphere(this,s);

		if(norm(force)>1E-15){
			return true;
		}else{
			return false;
		}

	}

	@Override
	public boolean isInContactWithCylinder(PhysicalCylinder c) {

		double[] force = PhysicalObject.interObjectForce.forceOnACylinderFromACylinder(this,c);
		if(norm(force)>1E-15){
			return true;
		}else{
			return false;
		}

	}

	/** Returns the point on this cylinder's spring axis that is the closest to the point p.*/
	public double[] closestPointTo(double[] p){
		double[] massToP = subtract(p, this.getInternalMassLocation());

		printlnLine("massToP",massToP);

		double massToPDotMinusAxis = -massToP[0]*getSpringAxis()[0] - massToP[1]*getSpringAxis()[1] - massToP[2]*getSpringAxis()[2];
		OutD.println("massToPDotMinusAxis = "+massToPDotMinusAxis);

		double K = massToPDotMinusAxis/(getActualLength()*getActualLength());

		OutD.println("K = "+K);

		double[] cc; // the closest point
		if(K<=1.0 && K>=0.0){
			cc  = new double[] {this.getInternalMassLocation()[0]-K*getSpringAxis()[0], this.getInternalMassLocation()[1]-K*getSpringAxis()[1], this.getInternalMassLocation()[2]-K*getSpringAxis()[2]};
		}else if(K<0){
			cc = this.getInternalMassLocation();
		}else {   	
			cc = proximalEnd();
		}

		printlnLine("cc",cc);

		return cc;
	}


	@Override
	public void runIntracellularDiffusion() {

		// 1) Degradation according to the degradation constant for each chemical

		for (Substance s : intracellularSubstances.values()) {
			s.degrade(this);
		}		

		diffuseWithPhysicalObject(motherNode, this.getActualLength());


		if(daughterRightNode != null){
			diffuseWithPhysicalObject(daughterRightNode, getDaughterRight().getActualLength());

		}

		if(daughterLeftNode != null){
			diffuseWithPhysicalObject(daughterLeftNode, getDaughterLeft().getActualLength());

		}

	}






	@Override
	public double[] getUnitNormalVector(double[] positionInPolarCoordinates) {
		return add(	scalarMult(Math.cos(positionInPolarCoordinates[1]), cordSys.getyAxis()),
				scalarMult(Math.sin(positionInPolarCoordinates[1]) ,cordSys.getzAxis())
		);
	}







	// *************************************************************************************
	//   Coordinates transform
	// *************************************************************************************

	/*
	 * 3 systems of coordinates :
	 * 
	 * Global :		cartesian coord, defined by orthogonal axis (1,0,0), (0,1,0) and (0,0,1)
	 * 				with origin at (0,0,0).
	 * Local :		defined by orthogonal axis xAxis (=vect proximal to distal end), yAxis and zAxis,
	 * 				with origin at proximal end
	 * Polar :		cylindrical coordinates [h,theta,r] with 	
	 * 				h = first local coord (along xAxis), 
	 * 				theta = angle from yAxis,
	 * 				r euclidian distance from xAxis;
	 * 				with origin at proximal end
	 * 
	 *  Note: The methods below transform POSITIONS and not DIRECTIONS !!!
	 *  
	 * G -> L
	 * L -> G
	 * 
	 * L -> P
	 * P -> L
	 * 
	 * G -> P = G -> L, then L -> P
	 * P -> P = P -> L, then L -> G 
	 */

	// G -> L
	/**
	 * Returns the position in the local coordinate system (xAxis, yXis, zAxis) 
	 * of a point expressed in global cartesian coordinates ([1,0,0],[0,1,0],[0,0,1]).
	 * @param positionInGlobalCoord
	 * @return
	 */
	public double[] transformCoordinatesGlobalToLocal(double[] positionInGlobalCoord){

		positionInGlobalCoord = subtract(positionInGlobalCoord, proximalEnd());
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
		return add(glob,proximalEnd());

	}
	// L -> P
	/**
	 * Returns the position in cylindrical coordinates (h,theta,r)
	 * of a point expressed in the local coordinate system (xAxis, yXis, zAxis).
	 * @param positionInLocalCoord
	 * @return
	 */
	public double[] transformCoordinatesLocalToPolar(double[] positionInLocalCoordinates){

		return new double[] {
				positionInLocalCoordinates[0],
				Math.atan2(positionInLocalCoordinates[2], positionInLocalCoordinates[1]),
				Math.sqrt(positionInLocalCoordinates[1]*positionInLocalCoordinates[1] + positionInLocalCoordinates[2]*positionInLocalCoordinates[2])
		};

	}
	// P -> L
	/**
	 * Returns the position in the local coordinate system (xAxis, yXis, zAxis) 
	 * of a point expressed in cylindrical coordinates (h,theta,r).
	 * @param positionInLocalCoord
	 * @return
	 */
	public double[] transformCoordinatesPolarToLocal(double[] positionInPolarCoordinates){

		return new double[] {
				positionInPolarCoordinates[0],
				positionInPolarCoordinates[2]*Math.cos(positionInPolarCoordinates[1]),
				positionInPolarCoordinates[2]*Math.sin(positionInPolarCoordinates[1])
		};

	}
	// P -> G :    P -> L, then L -> G 
	@Override
	public double[] transformCoordinatesPolarToGlobal(double[] positionInPolarCoordinates) {

		if(positionInPolarCoordinates.length==2){
			// the positionInLocalCoordinate is in cylindrical coord (h,theta,r)
			// with r being implicit (half the diameter)
			// We thus have h (along xAxis) and theta (the angle from the yAxis).
			double r = 0.5*getDiameter();
			positionInPolarCoordinates = new double[] {
					positionInPolarCoordinates[0],
					positionInPolarCoordinates[1],
					r
			};
		}
		double[] local = transformCoordinatesPolarToLocal(positionInPolarCoordinates);
		return transformCoordinatesLocalToGlobal(local);

	}
	// G -> L :    G -> L, then L -> P 
	@Override
	public double[] transformCoordinatesGlobalToPolar(double[] positionInGlobalCoordinates) {

		double[] local = transformCoordinatesGlobalToLocal(positionInGlobalCoordinates);
		return(transformCoordinatesLocalToPolar(local));

	}



	// *************************************************************************************
	//   GETTERS & SETTERS
	// *************************************************************************************


	/** Well, there is no field cellElement. We return neuriteElement.*/
	@Override
	public CellElement getCellularElement() {

		return neuriteElement;

	}

	/**
	 * @return the neuriteElement
	 */
	public NeuriteElement getNeuriteElement() {

		return neuriteElement;

	}


	/**
	 * @param neuriteElement the neuriteElement to set
	 */
	public void setNeuriteElement(NeuriteElement neuriteElement) {
		this.neuriteElement = neuriteElement;

	}


	/**
	 * @return the daughterLeft
	 */
	public PhysicalCylinder getDaughterLeft()
	{

		return fetchedDaughterLeft;
	}

	private PhysicalCylinder fetchDaughterLeft(){

		SpaceNodeFacade current = daughterLeftNode;

		if(current==null)
		{
			return null;
		}
		//		long id = current.getID();
		//		PhysicalNode cyl = LockManager.getUserObject(current,id);
		return (PhysicalCylinder)current.getUserObject();
	}

	/**
	 * @return the daughterRight
	 */
	public PhysicalCylinder getDaughterRight()
	{
		return fetchedDaughterRight;
	}



	private PhysicalCylinder fetchDaughterRight() {

		SpaceNodeFacade current = daughterRightNode;

		if(current==null) return null;
		return (PhysicalCylinder)current.getUserObject();// (PhysicalCylinder)LockManager.getUserObject(current,current.getID());
	}

	/**
	 * @return the mother
	 * @throws UserObjectNotFetchableYetException 
	 */
	private  PhysicalObject fetchMother()
	{


		SpaceNodeFacade thismothernode = motherNode;
		Color c = this.color;
		String tmp = c.getRed()+" "+c.getGreen()+" "+c.getBlue();
		//	if(thismothernode==null) throw new RuntimeException("it should have a mother!");
		return (PhysicalObject)thismothernode.getUserObject();// (PhysicalObject)LockManager.getUserObject(thismothernode,thismothernode.getID());

	}

	public PhysicalObject getMother() 
	{

		return fetchedMother;
	}





	public double getActualLength() {
		try{
			//addd a tini bit of length it can not be null
			double dist = Matrix.distance(motherNode.getPosition(),getInternalMassLocation())+0.000001;
			return dist;
		}
		finally
		{

		}
	}

	/**
	 * Should not be used, since the actual length depends on the geometry.
	 * @param actualLength
	 */
	public void setActualLength(double actualLength) {

		double [] dir = getSpringAxis();
		double [] vec = Matrix.scalarMult(actualLength,dir);
		setMassLocation(Matrix.add(motherNode.getPosition(),vec));

	}

	public double getRestingLength() {
		try{

			return springConstant*getActualLength()/(getTension()+springConstant);
		}
		finally
		{

		}
	}

	public void setRestingLength(double restingLength) {

		double actualLength= getActualLength();
		double ac_rest = Math.abs(actualLength - restingLength);
		setTension(springConstant * ( ac_rest ) / restingLength);
		if(Double.isNaN(tension))
		{
			OutD.println("tension nan");
		}

	}

	public double[] getSpringAxis() {
		return getAxis();
	}
	
	
	public double[] getAxis() {
		if(Matrix.norm(cordSys.getxAxis())==0)
		{
			if(neuriteElement.getLocalBiologyModulesList().size()>0)
			{
				double[] s = Matrix.subtract(soNode.getPosition(),motherNode.getPosition());
				System.out.println(s[0]);
			}
			
		}
		return cordSys.getxAxis();		

	}


	/** Returns the third axis of the local coordinate system.*/
	public double[] getZAxis() {

		return cordSys.getzAxis();

	}



	//	public void setSpringAxis(double[] springAxis) {
	//
	//		cordSys.turnToDirection(springAxis);
	//
	//	}

	public double getSpringConstant() {
		try{

			return springConstant;
		}
		finally
		{

		}	
	}

	public void setSpringConstant(double springConstant) {


		this.springConstant = springConstant;

	}

	public double getTension() {
		try{

			return tension;
		}
		finally
		{

		}	
	}

	//	public void setTension(double tension) {
	//		this.tension = tension;
	//		setRestingLengthToSetTension(tension);
	//	}

	/**
	 * Gets a vector of length 1, with the same direction as the SpringAxis.
	 * @return a normalized springAxis
	 */
	// NOT A "REAL" GETTER
	public double[] getUnitaryAxisDirectionVector() {
		try{

			double factor = 1.0/getActualLength();
			return new double[] {factor*getSpringAxis()[0], factor*getSpringAxis()[1], factor*getSpringAxis()[2]};
		}
		finally
		{

		}	
	}

	/** Should return yes if the PhysicalCylinder is considered a terminal branch.
	 * @return is it a terminal branch
	 */
	public boolean isTerminal(){
		try{

			return (daughterLeftNode==null);
		}
		finally
		{

		}	
	}

	/**
	 * Returns true if a bifurcation is physicaly possible. That is if the PhysicalCylinder
	 * has no daughter and the actual length is bigger than the minimum required.
	 * @return
	 */
	public boolean bifurcationPermitted(){
		try{

			if(daughterLeftNode == null && getActualLength() > Param.NEURITE_MINIMAL_LENGTH_FOR_BIFURCATION){
				//			if(daughterLeft == null && getLengthToProximalBranchingPoint() > 40){
				return true;
			}
			return false;
		}
		finally
		{

		}	
	}
	/**
	 * Returns true if a side branch is physically possible. That is if this is not a terminal
	 * branch and if there is not already a second daughter.
	 * @return
	 */
	public boolean branchPermitted(){
		try{

			if(this.daughterLeftNode!=null && this.daughterRightNode==null){
				return true;
			}else{
				return false;
			}
		}
		finally
		{

		}	
	}

	/**
	 * retuns the position of the proximal end
	 * @return
	 */
	public double[] proximalEnd(){
		try{
			double [] proxend = this.motherNode.getPosition();
			return proxend;
		}
		finally
		{

		}	
	}


	/**
	 * retuns the position of the distal end, ie the massLocation coordinates (but not the 
	 * actual massLocation array).
	 * Is mainly used for paint
	 * @return
	 */
	public double[] distalEnd(){
		try{

			return this.soNode.getPosition();
		}
		finally
		{

		}	
	}

	/**
	 * Returns the total (actual) length of all the cylinders (including the one in which this method is 
	 * called) before the previous branching point. Used to decide if long enough to bifurcate or branch,
	 * independently of the discretization.
	 * @return 
	 */
	public double lengthToProximalBranchingPoint(){


		double length = getActualLength();
		if(getMother() == null)
		{
			fetchedMother = fetchMother();
		}
		if (getMother().isAPhysicalCylinder()) {
			PhysicalCylinder previousCylinder = (PhysicalCylinder) getMother();
			if(previousCylinder.daughterRightNode == null){
				length += previousCylinder.lengthToProximalBranchingPoint();
			}
		}
		return length;
	}

	/** returns true because this object is a PhysicalCylinder */
	public boolean isAPhysicalCylinder(){
		return true;
	}

	@Override
	public double getLength() {
		try{

			return getActualLength();
		}
		finally
		{

		}
	}



	public void setDaughterLeft(PhysicalCylinder daughterLeft) {

		if(daughterLeft.soNode.getID() == this.soNode.getID()) throw new RuntimeException("can not be my own daughter!");
		if(daughterLeft!=null){this.daughterLeftNode = daughterLeft.soNode;
		fetchedDaughterLeft = null;}

	}

	public void setDaughterRight(PhysicalCylinder daughterRight) {

		if(daughterRight.soNode==null) throw new RuntimeException("this was null why the daughter rights so node was null");
		if(daughterRight.soNode.getID() == this.soNode.getID()) throw new RuntimeException("can not be my own daughter!");
		if(daughterRight!=null){ this.daughterRightNode = daughterRight.soNode;
		fetchedDaughterRight = null;
		}

	}

	public void setMother(PhysicalObject mother) {

		if(mother.soNode.getID() == this.soNode.getID()) throw new RuntimeException("can not be my own mother!");
		if(mother!=null){ this.motherNode = mother.soNode;
		fetchedMother = null;
		}

	}

	public void removeLocally() {
		ECM.getInstance().removePhysicalCylinder(this);
		if(this.neuriteElement!=null) neuriteElement.removeLocally();
		if(this.elneurite!=null)elneurite.removeLocally();
	}

	public void installLocally() {
		ECM.getInstance().addPhysicalCylinder(this);
		if(this.neuriteElement!=null) neuriteElement.installLocally();
		if(this.elneurite!=null)elneurite.installLocally();

	}


	@Override
	public PhysicalCylinder getAsPhysicalCylinder() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public PhysicalSphere getAsPhysicalSphere() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Collection<PhysicalNode> getNeighboringPhysicalNodes()
	{
		if(this.getNeuriteElement().getLocalBiologyModulesList().size()>0)
		{
			if(this.daughterLeftNode ==null)
			{
				//System.out.println("asdf");
				soNode.setRadius(this.getDiameter()/2+10);
			}
		}
 		Collection<PhysicalNode> temp = super.getNeighboringPhysicalNodes();
		if(this.getNeuriteElement().getLocalBiologyModulesList().size()>0)
		{
			if(this.daughterLeftNode ==null)
			{
				soNode.setRadius(this.getDiameter()/2);
			}
		}
		return temp;
	}

	private transient PhysicalObject fetchedMother; 
	private transient PhysicalCylinder fetchedDaughterLeft;
	private transient PhysicalCylinder fetchedDaughterRight;

	public void prefetchDependencies_debug()
	{
		fetchedMother = fetchMother();
		
		if(this.daughterLeftNode !=null)
		{
			fetchedDaughterLeft = fetchDaughterLeft();
			if(fetchedDaughterLeft==null) 
			{
				System.out.println("ahhhh");
				ObjectReference o = this.soNode.getObjectRef();
				fetchedDaughterLeft = fetchDaughterLeft();
			}
		}


		if(this.daughterRightNode !=null)
		{
			fetchedDaughterRight = fetchDaughterRight();
			if(fetchedDaughterRight==null) 
			{
				System.out.println("ahhhh");
				fetchDaughterRight();
			}
		}
	}
	
	
	public void prefetchDependencies()
	{
		fetchedMother = fetchMother();
		
		if(this.daughterLeftNode !=null)
		{
			fetchedDaughterLeft = fetchDaughterLeft();
			if(fetchedDaughterLeft==null) 
			{
				System.out.println("ahhhh");
				ObjectReference o = this.soNode.getObjectRef();
				fetchedDaughterLeft = fetchDaughterLeft();
			}
		}


		if(this.daughterRightNode !=null)
		{
			fetchedDaughterRight = fetchDaughterRight();
			if(fetchedDaughterRight==null) 
			{
				System.out.println("ahhhh");
				fetchDaughterRight();
			}
		}

		super.prefetchDependencies();
	}


	@Override
	public double getVolume() {
		// TODO Auto-generated method stub
		return Math.PI * getDiameter()/2 * getDiameter()/2 * getActualLength();
	}

	@Override
	public void setVolume(double volume) {
		setDiameter(Math.sqrt(volume * 4/Math.PI / getActualLength()));  	// 1.27323 = 4/pi
	}



	@Override
	public void deserialize(DataInputStream is) throws IOException {
		super.deserialize(is);

		forceToTransmitToProximalMass = new double[]{0,0,0};
		forceToTransmitToProximalMass[0]  = is.readDouble();
		forceToTransmitToProximalMass[1]  = is.readDouble();
		forceToTransmitToProximalMass[2]  = is.readDouble();

		motherNode = new SpaceNodeFacade(this.soNode.getObjectRef());
		motherNode.deserialize(is);
		int b = is.readInt();
		switch (b) {
		case -1:

			break;
		case 1:
			daughterLeftNode = new SpaceNodeFacade(this.soNode.getObjectRef().getCopy());
			daughterLeftNode.deserialize(is);		
			break;
		case 2:
			daughterLeftNode = new SpaceNodeFacade(this.soNode.getObjectRef().getCopy());
			daughterLeftNode.deserialize(is);
			daughterRightNode = new SpaceNodeFacade(this.soNode.getObjectRef().getCopy());
			daughterRightNode.deserialize(is);
			break;
		default:
			System.out.println("wrroooonnngngg");
		}

		springConstant = is.readDouble();
		setTension(is.readDouble());

		neuriteElement = new NeuriteElement();
		neuriteElement.deserialize(is);
		neuriteElement.setPhysicalCylinder(this);
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		super.serialize(os);

		if(forceToTransmitToProximalMass==null) forceToTransmitToProximalMass = new double[]{0,0,0};
		os.writeDouble(forceToTransmitToProximalMass[0]);
		os.writeDouble(forceToTransmitToProximalMass[1]);
		os.writeDouble(forceToTransmitToProximalMass[2]);


		motherNode.serialize(os);

		int b=-1;
		if(daughterLeftNode!=null) b = 1;
		if(daughterRightNode!=null) b = 2;

		os.writeInt(b);
		if(daughterLeftNode!=null) daughterLeftNode.serialize(os);
		if(daughterRightNode!=null) daughterRightNode.serialize(os);
		os.writeDouble(springConstant);
		os.writeDouble(tension);
		neuriteElement.serialize(os);

	}

	protected void setSpaceNodeRadius()
	{
		if(soNode==null) return;
		if(this.daughterLeftNode!=null)
		{
			double radius = Math.max(diam/2,diam/2+10); 
			soNode.setRadius(radius);
		}
		else
		{
			double radius = diam/2*2; 
			soNode.setRadius(radius);
		}
	}

	public void setInterObjectForceCoefficient(double interObjectForceCoefficient) {

		this.interObjectForceCoefficient = interObjectForceCoefficient;
		setSpaceNodeRadius();
	}

	@Override
	public double[] getLastForce() {
		// TODO Auto-generated method stub
		if(futureMovement==null) return new double[]{Double.NaN,Double.NaN,Double.NaN};
		return futureMovement;
	}

	public void checkHostDependence() {
		super.checkHostDependence();

		SpaceNodeFacade n = motherNode;
		String host = n.getObjectRef().getPartitionId().getHost();
		if(!host.equals(this.getHost()))
		{
			if(!dependingHosts.contains(host))
				dependingHosts.add(host);
		}

		n = daughterLeftNode;

		if(n==null) return;
		host = n.getObjectRef().getPartitionId().getHost();
		if(!host.equals(this.getHost()))
		{
			if(!dependingHosts.contains(host))
				dependingHosts.add(host);
		}

		n = daughterRightNode;
		if(n==null) return;
		host = n.getObjectRef().getPartitionId().getHost();
		if(!host.equals(this.getHost()))
		{
			if(!dependingHosts.contains(host))
				dependingHosts.add(host);
		}
	}

	public ArrayList<PhysicalNode> getDependingNodes() {
		ArrayList<PhysicalNode> list =  super.getDependingNodes();
		SpaceNodeFacade n = motherNode;
		String host = n.getObjectRef().getPartitionId().getHost();
		if(!host.equals(this.getHost()))
		{
			if(!dependingHosts.contains(host))
				list.add(getMother());
		}

		n = daughterLeftNode;

		if(n==null) return list;
		host = n.getObjectRef().getPartitionId().getHost();
		if(!host.equals(this.getHost()))
		{
			if(!dependingHosts.contains(host))

				list.add(getDaughterLeft());
		}

		n = daughterRightNode;
		if(n==null) return list;
		host = n.getObjectRef().getPartitionId().getHost();
		if(!host.equals(this.getHost()))
		{
			if(!dependingHosts.contains(host))
				list.add(getDaughterRight());
		}
		return list;
	}

	public ElectroPhysiolgyNeurite getElectroPhysiolgy() {
		// TODO Auto-generated method stub
		return elneurite;
	}

	public void setElectroPhysiolgy(
			ElectroPhysiolgyNeurite electroPhysiolgyNeurite) {
		elneurite = electroPhysiolgyNeurite;
		elneurite.installLocally();
	}
}
