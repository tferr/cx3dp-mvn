package ini.cx3d.simulation;

import ini.cx3d.biology.Cell;
import ini.cx3d.biology.NeuriteElement;
import ini.cx3d.biology.SomaElement;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.parallelization.communication.SimulationStateManager;
import ini.cx3d.physics.IntracellularSubstance;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.physics.Substance;
import ini.cx3d.physics.diffusion.DiffReg;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ORR;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.PMNotFoundException;
import ini.cx3d.spacialOrganisation.PartitionManager;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.Matrix;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.RuntimeErrorException;

/**
 * Contains some lists with all the elements of the simulation, and methods to add 
 * or remove elements. Contains lists of  
 * @author fredericzubler
 *
 *
 */
public class ECM implements Serializable{

	// List of all the CX3DRunbable objects in the simulation ............................

	/**
	 * 
	 */
	private static final long serialVersionUID = -6624191372939496803L;
	/** List of all the PhysicalNode instances. */
	private ArrayAccessHashTable physicalNodeList = new ArrayAccessHashTable();
	/** List of all the PhysicalSphere instances. */
	public HashT<Long,PhysicalSphere> physicalSphereList = new HashT<Long,PhysicalSphere>();
	/** List of all the PhysicalCylinder instances. */
	public HashT<Long,PhysicalCylinder> physicalCylinderList = new HashT<Long,PhysicalCylinder>();
	/** List of all the SomaElement instances. */
	public HashT<Long,SomaElement> somaElementList  = new HashT<Long,SomaElement>();
	/** List of all the NeuriteElement instances. */
	public HashT<Long,NeuriteElement> neuriteElementList = new HashT<Long,NeuriteElement>();
	/** List of all the Cell instances. */
	public HashT<Long,Cell> cellList  = new HashT<Long,Cell>();

	/* An SON used to get new SON instances from*/
	private SpaceNodeFacade initialNode;

	/* In here we keep a template for each (extra-cellular) Substance in the simulation that have
	 * non-standard value for diffusion and degradation constant.*/
	private HashT<String, Substance> substancesLibrary = new HashT<String, Substance>();

	/* In here we keep a template for each (intra-cellular) Substance in the simulation that have
	 * non-standard value for diffusion and degradation constant.*/
	private HashT<String, IntracellularSubstance> intracellularSubstancesLibrary = new HashT<String, IntracellularSubstance>();

	/* In here we store a color attributed to specific cell types.*/
	private Hashtable<String, Color> cellTypeColorLibrary = new Hashtable<String, Color>();

	// Artificial walls ...................................................................




	// Artificial gradients ...............................................................

	
	//idcounters
	
	private AtomicInteger cellidcounter = new AtomicInteger(0);
	private AtomicInteger physicalBondcounter = new AtomicInteger(0);
	private AtomicLong diffusionNodeCounter = new AtomicLong(0);
	private AtomicLong commandCounter = new AtomicLong(0);

	private Lock nodeListLock = new ReentrantLock();
	
	// (in the next: all hash tables are public for View.paint)
	/* List of all the chemicals with a gaussian distribution along the Z-axis
	 * max value, mean (z-coord of the max value), sigma2 (thickness). */
	public Hashtable<Substance,double[]> gaussianArtificialConcentrationZ = new Hashtable<Substance,double[]>();

	/* List of all the chemicals with a linear distribution along the Z-axis
	 * max value, TOP (z-coord of the max value), DOWN (z-coord of the 0 value). */ 
	public Hashtable<Substance,double[]> linearArtificialConcentrationZ = new Hashtable<Substance,double[]>();

	/* List of all the chemicals with a gaussian distribution along the X-axis
	 * max value, mean (x-coord of the max value), sigma2 (thickness). */ 
	public Hashtable<Substance,double[]> gaussianArtificialConcentrationX = new Hashtable<Substance,double[]>();

	/* List of all the chemicals with a linear distribution along the X-axis
	 * max value, TOP (x-coord of the max value), DOWN (x-coord of the 0 value). */  
	public Hashtable<Substance,double[]> linearArtificialConcentrationX = new Hashtable<Substance,double[]>();

	/* to link the one instance of Substance we have used in the definition of the gradient, with the name of
	 * the chemical that can be given as argument in the methods to know the concentration/grad.. */
	public Hashtable<String, Substance> allArtificialSubstances = new Hashtable<String, Substance>();



	// **************************************************************************
	// Singleton pattern
	// **************************************************************************

	private static ECM instance = null;

	private ECM() {
	}

	/** 
	 * Gets a reference to the (unique) ECM
	 * @return the ECM
	 */
	public static ECM getInstance() {
		if (instance == null) {
			instance = new ECM();
		}
		return instance;
	}



	// **************************************************************************
	// Random Number
	// **************************************************************************
	static Random random = new Random();

	/**
	 * @return a random number between, from uniform probability 0 and 1;
	 */
	public static double getRandomDouble(){
		return random.nextDouble();
	}

	/**
	 * returns a random number from gaussian distribution
	 * @param mean
	 * @param standardDeviation
	 * @return
	 */
	public static double getGaussianDouble(double mean, double standardDeviation){
		return mean + standardDeviation*random.nextGaussian();
	}



	/**
	 * Initialises the random number generator. 
	 * @param seed
	 */
	public static void setRandomSeedTo(long seed){
		random = new Random(seed);
		Matrix.setRandomSeedTo(seed);
	}

	// **************************************************************************
	// Artificial Wall
	// **************************************************************************
	/**
	 * Set the boundaries of a pseudo wall, that maintains the PhysicalObjects in a closed volume.
	 * Automatically turns on this mechanism for spheres.
	 * @param Xmin
	 * @param Xmax
	 * @param Ymin
	 * @param Ymax
	 * @param Zmin
	 * @param Zmax
	 */
	public void setBoundaries(double Xmin, double Xmax, double Ymin, double Ymax, double Zmin, double Zmax){
		SimulationState.getLocal().minBoundary[0] = Xmin;
		SimulationState.getLocal().minBoundary[1] = Ymin;
		SimulationState.getLocal().minBoundary[2] = Zmin;
		SimulationState.getLocal().maxBoundary[0] = Xmax;
		SimulationState.getLocal().maxBoundary[1] = Ymax;
		SimulationState.getLocal().maxBoundary[2] = Zmax;
		setArtificialWallsForSpheres(true);
	}
	/** If set to true, the PhysicalSpheres tend to stay inside a box, 
	 * who's boundaries are set with setBoundaries().
	 * @param artificialWalls
	 */
	public void setArtificialWallsForSpheres(boolean artificialWallsForSpheres){
		SimulationState.getLocal().artificialWallsForSpheres = artificialWallsForSpheres;
	}

	/** If true, the PhysicalSpheres tend to stay inside a box, who's boundaries are set with
	 * setBoundaries().
	 * @param artificialWalls
	 */
	public boolean getArtificialWallForSpheres(){
		return SimulationState.getLocal().artificialWallsForSpheres;
	}
	/** If set to true, the PhysicalCyliners tend to stay inside a box, 
	 * who's boundaries are set with setBoundaries().
	 * @param artificialWalls
	 */
	public void setArtificialWallsForCylinders(boolean artificialWallsForCylinders){
		SimulationState.getLocal().artificialWallsForCylinders = artificialWallsForCylinders;
	}

	/** If true, the PhysicalCyliners tend to stay inside a box, who's boundaries are set with
	 * setBoundaries().
	 * @param artificialWalls
	 */
	public boolean getArtificialWallForCylinders(){
		boolean temp = SimulationState.getLocal().artificialWallsForCylinders;
		return temp;
	}
	
	public double[] getArtificialWalllMax()
	{
		return getMaxBounds();
	}
	
	
	public double[] getArtificialWalllMin()
	{
		return getMinBounds();
	}
	/**
	 * Returns a force that would be applied to a PhysicalSphere that left the boundaries
	 * of the artificial wall. 
	 * @param location the center of the PhysicalSphere
	 * @param radius the radius of the PhysicalSphere
	 * @return [Fx,Fy,Fz] the force applied to the cell
	 */
	public double[] correctForArtificalWall(double[] location, double radius){
		// TODO : take the radius into account
		double[] force = new double[3]; 
		double springThatPullsCellsBackIntoBoundaries = 2.0;
		double [] min = getMinBounds();
		double [] max = getMaxBounds();
		if(location[0] < min[0]) {
			force[0] += springThatPullsCellsBackIntoBoundaries*(min[0]-location[0]);
		}else if(location[0] > max[0]) {
			force[0] += springThatPullsCellsBackIntoBoundaries*(max[0]-location[0]);
		}

		if(location[1] < min[1]) {
			force[1] += springThatPullsCellsBackIntoBoundaries*(min[1]-location[1]);
		}else if(location[1] > min[2]) {
			force[1] += springThatPullsCellsBackIntoBoundaries*(min[2]-location[1]);
		}

		if(location[2] < min[2]) {
			force[2] += springThatPullsCellsBackIntoBoundaries*(min[2]-location[2]);
		}else if(location[2] > max[2]) {
			force[2] += springThatPullsCellsBackIntoBoundaries*(max[2]-location[2]);
		}
		return force;
	}

	// **************************************************************************
	// SOM and Interaction with PO & CellElements (add, remove, ..)
	// **************************************************************************


	/** 
	 * Returns an instance of a class implementing SpatialOrganizationNode. 
	 * If it is the first node of the simulation, it will fix the Class type of the SpatialOrganizationNode.
	 * CAUTION : NEVER call this method if there exist already SpatialOrganizationNodes in 
	 * the simulation, and initialNode in ECM has not been instatialized : there will then be
	 * two different unconnected Delaunay
	 * @param position
	 * @param userObject
	 * @return
	 */
	public SpaceNodeFacade getSpatialOrganizationNodeInstance(double[] position, PhysicalNode userObject){
		if(initialNode == null || !initialNode.isLocal()){
			SpaceNodeFacade sn1 = SpaceNodeFacade.getInitialNode(position.clone(), userObject);
			initialNode = sn1;
			return sn1;

		}
		SpaceNodeFacade son = (SpaceNodeFacade)initialNode.getNewInstance(position.clone(), userObject);

		return son;

	}

	/** 
	 * Returns an instance of a class implementing SpatialOrganizationNode. 
	 * If it is the first node of the simulation, it will fix the Class type of the SpatialOrganizationNode.
	 * CAUTION : NEVER call this method if there exist already SpatialOrganizationNodes in 
	 * the simulation, and initialNode in ECM has not been instantiated : there will then be
	 * two different unconnected Delaunay
	 * @param n an already existing SpaceNode close to the place where the new one should be
	 * @param position
	 * @param userObject
	 * @return
	 */
	public SpaceNodeFacade getSpatialOrganizationNodeInstance(
			SpaceNodeFacade n, double[] position, PhysicalNode userObject){


		if(initialNode == null){
			SpaceNodeFacade sn1 = SpaceNodeFacade.getInitialNode(position, userObject);
			initialNode = sn1;
			return sn1;

		}

		SpaceNodeFacade son =  n.getNewInstance(position.clone(), userObject);
		return son;

	}



	// Physical Objects-------------------------------------------
	// PhysicalCylinder and PhysicalSphere are also instances of PhysicalNode.
	// PhysicalNode contains a SpatialOrganizerNode.
	// So: add/remove PhysicalCylinder/PhysicalSphere makes a call to
	// add/remove-PhysicalNode.
	// the later also calls the remove() method of the associated SpatialOrganizationNode.

	public void addPhysicalCylinder(PhysicalCylinder newCylinder) {
		if(physicalCylinderList.containsKey(newCylinder.getSoNode().getID())) return;
		physicalCylinderList.put(newCylinder.getSoNode().getID(), newCylinder);
		addPhysicalNode(newCylinder);
	}

	public void removePhysicalCylinder(PhysicalCylinder oldCylinder) {
		physicalCylinderList.remove(oldCylinder.getSoNode().getID());
		removePhysicalNode(oldCylinder);
	}

	public void addPhysicalSphere(PhysicalSphere newSphere) {
		if(physicalSphereList.containsKey(newSphere.getSoNode().getID())) return;
		physicalSphereList.put(newSphere.getSoNode().getID(),newSphere);
		addPhysicalNode(newSphere);
	}

	public void removePhysicalSphere(PhysicalSphere oldSphere) {
		physicalSphereList.remove(oldSphere.getSoNode().getID());
		removePhysicalNode(oldSphere);
	}

	public void addPhysicalNode(PhysicalNode newPhysicalNode) {
		if(physicalNodeList.containsKey(newPhysicalNode.getSoNode().getID())) return;
		nodeListLock();
		if(!newPhysicalNode.getSoNode().isLocal())
		{
			System.out.println("not local!");
		}
		physicalNodeList.put(newPhysicalNode.getSoNode().getID(),newPhysicalNode);
		nodeListunLock();
	}

	public void removePhysicalNode(PhysicalNode oldPhysicalNode) {
		nodeListLock();
		physicalNodeList.remove(oldPhysicalNode.getSoNode().getID());
		if(physicalNodeList.containsKey(oldPhysicalNode.getSoNode().getID()))
		{
			System.out.println("the remove did not work");
		}
		nodeListunLock();
		//oldPhysicalNode.getSoNode().remove();
	}

	//	Cells

	public void addCell(Cell newCell) {
		cellList.put(newCell.getSomaElement().getPhysical().getSoNode().getID(),newCell);
	}

	public void removeCell(Cell oldCell) {
		cellList.remove(oldCell.getSomaElement().getPhysical().getSoNode().getID());

	}

	// Cell Elements--------------------------------------------------
	public void addSomaElement(SomaElement newSoma) {
		if(somaElementList.containsKey(newSoma.getPhysical().getSoNode().getID())) return;
		somaElementList.put(newSoma.getPhysical().getSoNode().getID(),newSoma);
	}

	public void removeSomaElement(SomaElement oldSoma) {
		somaElementList.remove(oldSoma.getPhysical().getSoNode().getID());
	}

	public void addNeuriteElement(NeuriteElement newNE) {
		if(neuriteElementList.containsKey(newNE.getPhysical().getSoNode().getID())) return;
		neuriteElementList.put(newNE.getPhysical().getSoNode().getID(),newNE);
	}

	public void removeNeuriteElement(NeuriteElement oldNE) {
		//		ShowConsoleOutput.println("size "+neuriteElementList.size());
		neuriteElementList.remove(oldNE.getPhysical().getSoNode().getID());
		//		ShowConsoleOutput.println("size "+neuriteElementList.size());
	}

	public void resetTime()
	{
		SimulationState.getLocal().simulationTime = 0.0;
	}

	/**
	 * Removes all the objects in the simulation, including SpaceNodes and the triangulation.
	 */
	public void clearAll(){
		// Layer 1 : Cells
		cellList  = new HashT<Long, Cell>();
		// Layer 2 : local biology
		somaElementList  = new HashT<Long,SomaElement>();
		neuriteElementList = new HashT<Long,NeuriteElement>();

		// Layer 3 : physics 
		physicalNodeList = new ArrayAccessHashTable();
		physicalSphereList = new HashT<Long, PhysicalSphere>();
		physicalCylinderList = new HashT<Long, PhysicalCylinder>();
		allArtificialSubstances.clear();
		gaussianArtificialConcentrationX.clear();
		gaussianArtificialConcentrationZ.clear();
		linearArtificialConcentrationX.clear();
		linearArtificialConcentrationZ.clear();
		this.intracellularSubstancesLibrary.clear();
		this.substancesLibrary.clear();
		// Layer 4 : triangulation
		initialNode = null;

	}


	// *********************************************************************
	// *** Substances (real chemicals)
	// *********************************************************************

	/** Define a template for each (extra-cellular) <code>Substance</code> in the simulation that has
	 * non-standard values for diffusion and degradation constant.*/
	public void addNewSubstanceTemplate(Substance s){
		if(s instanceof IntracellularSubstance)
		{
			intracellularSubstancesLibrary.put(s.getId(),(IntracellularSubstance) s);
		}
		else
		{
			substancesLibrary.put(s.getId(), s);
		}

	}

	/** Define a template for each <code>IntracellularSubstance</code> in the simulation that has
	 * non-standard values for diffusion and degradation constant, and outside visibility and volume dependency.*/
	public void addNewIntracellularSubstanceTemplate(IntracellularSubstance s){
		intracellularSubstancesLibrary.put(s.getId(), s);
	}

	/** Returns an instance of <code>Substance</code>. If a similar substance (with the same id)
	 * has already been declared as a template Substance, a copy of it is made (with
	 * same id, degradation and diffusion constant, but concentration and quantity 0).
	 * If it is the first time that this id is requested, a new template Substance is made
	 * (with by-default values) and stored, and a copy will be returned.
	 * @param id
	 * @return new Substance instance
	 */
	public Substance substanceInstance(String id){
		Substance s = substancesLibrary.get(id);
		if(s==null){
			s = new Substance();
			s.setId(id);
			// s will have the default color blue, diff const 1000 and degrad const 0.01
			substancesLibrary.put(id, s);
		}
		return s.getCopy();
	}

	/** Returns an instance of <code>IntracellularSubstance</code>. If a similar 
	 * IntracellularSubstance (with the same id) has already been declared as a template 
	 * IntracellularSubstance, a copy of it is made (with same id, degradation constant, 
	 * diffusion constant, outside visibility and volume dependency, but concentration 
	 * and quantity 0).
	 * If it is the first time that this id is requested, a new template IntracellularSubstance
	 *  is made (with by-default values) and stored, and a copy will be returned.
	 * @param id
	 * @return new IntracellularSubstance instance
	 */
	public IntracellularSubstance intracellularSubstanceInstance(String id){
		IntracellularSubstance s = intracellularSubstancesLibrary.get(id);
		if(s==null){
			s = new IntracellularSubstance();
			s.setId(id);
			// s will have the default color blue, diff const 1000, degrad const 0.01,
			// visibleFromOuside false and volumeDep false.
			this.addNewIntracellularSubstanceTemplate(s);
			intracellularSubstancesLibrary.put(id, s);
		}
		return (IntracellularSubstance) s.getCopy();
	}

	// *********************************************************************
	// *** Pre-defined cellType colors
	// *********************************************************************
	public void addNewCellTypeColor(String cellType, Color color){
		cellTypeColorLibrary.put(cellType, color);
	}

	public Color cellTypeColor(String cellType){
		//Select color from a list of cellsTypes
		Color c;
		if (cellType == null){						// if cell type is null				
			cellType = "null";
		}
		c = cellTypeColorLibrary.get(cellType);
		if(c==null){
			c = new Color((float) ECM.getRandomDouble(),(float) ECM.getRandomDouble(),(float) ECM.getRandomDouble(),0.7f);
			cellTypeColorLibrary.put(cellType, c);
		}
		return c;
	}



	/**
	 * Defines a bell-shaped artificial concentration in ECM, along the Z axis (ie uniform along X,Y axis). 
	 * It is a continuous value, and not instances of the class Substance!.
	 *   
	 * @param nameOfTheChemical 
	 * @param maxConcentration the value of the concentration at its peak
	 * @param zCoord the location of the peak
	 * @param sigma the thickness of the layer (= the variance)
	 */
	public void addArtificialGaussianLayerZ(Substance substance, double maxConcentration, double zCoord, double sigma){

		Substance sAlreadyPresent = allArtificialSubstances.get(substance.getId());
		if(sAlreadyPresent!=null){
			substance = sAlreadyPresent;
		}
		// define distribution values for the chemical, and store them together
		double[] value = new double[] {maxConcentration, zCoord, sigma}; 
		gaussianArtificialConcentrationZ.put(substance, value);
		allArtificialSubstances.put(substance.getId(), substance);

	}

	/**
	 * Defines a linear artificial concentration in ECM, between two points along the Z axis. Outside this interval
	 * the value will be 0. Between the interval the value is the linear interpolation between 
	 * the maximum value and 0.
	 * 
	 * It is a continuous value, and not instances of the class Substance!
	 * 
	 * @param nameOfTheChemical
	 * @param maxConcentration the value of the concentration at its peak
	 * @param zCoordMax the location of the peak 
	 * @param zCoordMin the location where the concentration reaches the value 0
	 */
	public void addArtificialLinearGradientZ(Substance substance, double maxConcentration, double zCoordMax, double zCoordMin){
		// look if we already have a substance with the same id
		Substance sAlreadyPresent = allArtificialSubstances.get(substance.getId());
		if(sAlreadyPresent!=null){
			substance = sAlreadyPresent;
		}
		// define distribution values for the chemical, and store them together
		double[] value = new double[] {maxConcentration, zCoordMax, zCoordMin}; 
		linearArtificialConcentrationZ.put(substance, value);
		allArtificialSubstances.put(substance.getId(), substance);
		// note that we have at least one artificial gradient

	}
	/**
	 * Defines a bell-shaped artificial concentration in ECM, along the X axis (ie uniform along Y,Z axis). 
	 * It is a continuous value, and not instances of the class Substance!.
	 *   
	 * @param nameOfTheChemical 
	 * @param maxConcentration the value of the concentration at its peak
	 * @param xCoord the location of the peak
	 * @param sigma the thickness of the layer (= the variance)
	 */
	public void addArtificialGaussianLayerX(Substance substance, double maxConcentration, double xCoord, double sigma){
		// look if we already have a substance with the same id
		Substance sAlreadyPresent = allArtificialSubstances.get(substance.getId());
		if(sAlreadyPresent!=null){
			substance = sAlreadyPresent;
		}
		// define distribution values for the chemical, and store them together
		double[] value = new double[] {maxConcentration, xCoord, sigma}; 
		gaussianArtificialConcentrationX.put(substance, value);
		allArtificialSubstances.put(substance.getId(), substance);


	}

	/**
	 * Defines a linear artificial concentration in ECM, between two points along the X axis. Outside this interval
	 * the value will be 0. Between the interval the value is the linear interpolation between 
	 * the maximum value and 0.
	 * 
	 * It is a continuous value, and not instances of the class Substance!
	 * 
	 * @param nameOfTheChemical
	 * @param maxConcentration the value of the concentration at its peak
	 * @param xCoordMax the location of the peak 
	 * @param xCoordMin the location where the concentration reaches the value 0
	 */
	public void addArtificialLinearGradientX(Substance substance, double maxConcentration, double xCoordMax, double xCoordMin){
		// look if we already have a substance with the same id
		Substance sAlreadyPresent = allArtificialSubstances.get(substance.getId());
		if(sAlreadyPresent!=null){
			substance = sAlreadyPresent;
		}
		// define distribution values for the chemical, and store them together
		double[] value = new double[] {maxConcentration, xCoordMax, xCoordMin}; 
		linearArtificialConcentrationX.put(substance, value);
		allArtificialSubstances.put(substance.getId(), substance);
	}

	/**
	 * Gets the value of a chemical, at a specific position in space
	 * @param nameOfTheChemical
	 * @param position the location [x,y,z]
	 * @return
	 */
	public double getValueArtificialConcentration(String nameOfTheChemical, double[] position){
		double x = position[0];
		double z = position[2];
		// does the substance exist at all ?
		Substance sub = null;
		if(allArtificialSubstances.containsKey(nameOfTheChemical)){
			sub = allArtificialSubstances.get(nameOfTheChemical);
		}else{
			return 0;
		}
		// if yes, we look for every type of gradient it might be implicated in,
		// and we add them up
		double concentration = 0;
		// X Gaussian
		if(gaussianArtificialConcentrationX.containsKey(sub)){
			double[] val = gaussianArtificialConcentrationX.get(sub);
			double exposant = (x-val[1])/val[2];
			exposant = exposant*exposant*0.5;
			concentration += val[0]*Math.exp(-exposant);
		}
		// Z Gaussian
		if(gaussianArtificialConcentrationZ.containsKey(sub)){
			double[] val = gaussianArtificialConcentrationZ.get(sub);
			double exposant = (z-val[1])/val[2];
			exposant = exposant*exposant*0.5;
			concentration += val[0]*Math.exp(-exposant);
		}
		// X linear
		if(linearArtificialConcentrationX.containsKey(sub)){
			double[] val = linearArtificialConcentrationX.get(sub);
			// only if between max and min
			if( (x<val[1] && x>val[2]) || (x>val[1] && x<val[2]) ){
				double slope = val[0]/(val[1]-val[2]);
				double result = (x-val[2])*slope;
				concentration += result;
			}
		}
		// Z linear
		if(linearArtificialConcentrationZ.containsKey(sub)){
			double[] val = linearArtificialConcentrationZ.get(sub);
			// only if between max and min
			if( (z<val[1] && z>val[2]) || (z>val[1] && z<val[2]) ){
				double slope = val[0]/(val[1]-val[2]);
				double result = (z-val[2])*slope;
				concentration += result;
			}
		}
		return concentration;
	}


	/**
	 * Gets the value of a chemical, at a specific position in space
	 * @param nameOfTheChemical
	 * @param position the location [x,y,z]
	 * @return
	 */
	public double getValueArtificialConcentration(Substance substance, double[] position){
		return getValueArtificialConcentration(substance.getId(), position);
	}
	///////// GRADIENT
	/**
	 * Gets the gradient of a chemical, at a specific altitude
	 * @param nameOfTheChemical
	 * @param position the location [x,y,z]
	 * @return the gradient [dc/dx , dc/dy , dc/dz]
	 */
	public double[] getGradientArtificialConcentration(String nameOfTheChemical, double[] position){
		// Do we have the substance in stock?
		Substance sub = null;
		if(allArtificialSubstances.containsKey(nameOfTheChemical)){
			sub = allArtificialSubstances.get(nameOfTheChemical);
		}else{
			return new double[] {0,0,0};
		}
		// if yes, we look for every type of gradient it might be implicated in
		double[] gradient = {0,0,0};
		double x = position[0];
		double z = position[2];
		// Gaussian X
		if(gaussianArtificialConcentrationX.containsKey(sub)){
			double[] val = gaussianArtificialConcentrationX.get(sub);
			double exposant = (x-val[1])/val[2];
			exposant = exposant*exposant*0.5;
			double xValOfGradient = -((x-val[1])/(val[2]*val[2])) *val[0]*Math.exp(-exposant);
			gradient[0] += xValOfGradient;
		}
		// Gaussian Z
		if(gaussianArtificialConcentrationZ.containsKey(sub)){
			double[] val = gaussianArtificialConcentrationZ.get(sub);
			double exposant = (z-val[1])/val[2];
			exposant = exposant*exposant*0.5;
			double zValOfGradient = -((z-val[1])/(val[2]*val[2])) *val[0]*Math.exp(-exposant);
			gradient[2] += zValOfGradient;
		}
		// Linear X
		if(linearArtificialConcentrationX.containsKey(sub)){
			double[] val = linearArtificialConcentrationX.get(sub);
			// only if x between max and min
			if (val[1]>x && x>val[2]){	// if up is higher, the gradient points up
				double slope = val[0]/(val[1]-val[2]);
				gradient[0] += slope;
			}
			if (val[1]<x && x<val[2]){
				double slope = val[0]/(val[1]-val[2]); // otherwise the gradient points down
				gradient[0] += slope;
			}
		}
		// Linear Z
		if(linearArtificialConcentrationZ.containsKey(sub)){
			double[] val = linearArtificialConcentrationZ.get(sub);
			// only if x between max and min
			if (val[1]>z && z>val[2]){	// if up is higher, the gradient points up
				double slope = val[0]/(val[1]-val[2]);
				gradient[2] += slope;
			}
			if (val[1]<z && z<val[2]){
				double slope = val[0]/(val[1]-val[2]); // otherwise the gradient points down
				gradient[2] += slope;
			}
		}
		return gradient;
	}


	public double getGradientArtificialConcentration(Substance s, double[] position){
		return getValueArtificialConcentration(s.getId(), position);
	}


	// **************************************************************************
	// Simulation Time
	// **************************************************************************
	public  String nicelyWrittenECMtime(){
		double hours = Math.floor(SimulationState.getLocal().simulationTime);
		double minutes = SimulationState.getLocal().simulationTime-hours;

		minutes*=60.0;

		minutes *=100;							// to avoid decimals
		minutes = Math.floor(minutes);
		minutes/= 100;

		if(hours<24){
			return ""+hours+" h "+minutes+" min.";
		}
		double hdivided = hours/24.0; 
		double days = Math.floor(hdivided); 
		hours = hours-days*24;
		return ""+days+" days "+hours+" hours "+minutes+" min.";
	}




	public static void pause(int time){
		try{
			Thread.sleep(time);
		}catch(Exception e){

		}
	}




	/**
	 * @return the physicalNodeList
	 */
	public  ArrayAccessHashTable getPhysicalNodes() {
		return physicalNodeList;
	}

	public void nodeListLock()
	{
	//	nodeListLock.lock();
	}
	
	public void nodeListunLock()
	{
	//	nodeListLock.unlock();
	}
	

	/**
	 * @return the cellList
	 */
	public Collection<Cell> getCellList(){
		return cellList.values();
	}

	public Collection<PhysicalSphere> getPhysicalSphereList() {
		return physicalSphereList.values();
	}

	public Collection<PhysicalCylinder> getPhysicalCylinderList() {
		return physicalCylinderList.values();
	}

	public Collection<NeuriteElement> getNeuriteElementList() {
		return neuriteElementList.values();
	}


	public Hashtable<Substance, double[]> getGaussianArtificialConcentrationZ() {
		return gaussianArtificialConcentrationZ;
	}

	public Hashtable<Substance, double[]> getLinearArtificialConcentrationZ() {
		return linearArtificialConcentrationZ;
	}

	public Hashtable<Substance, double[]> getGaussianArtificialConcentrationX() {
		return gaussianArtificialConcentrationX;
	}

	public Hashtable<Substance, double[]> getLinearArtificialConcentrationX() {
		return linearArtificialConcentrationX;
	}

	public double getECMtime() {
		return SimulationState.getLocal().simulationTime;
	}

	public void setECMtime(double ECMtime) {
		SimulationState.getLocal().simulationTime = ECMtime;
	}

	public void increaseECMtime(double deltaT){
		SimulationState.getLocal().simulationTime += deltaT;
	}




	public HashT<String, IntracellularSubstance> getIntracelularSubstanceTemplates() {
		// TODO Auto-generated method stub
		return this.intracellularSubstancesLibrary;
	}

	public HashT<String, Substance> getSubstanceTemplates() {
		// TODO Auto-generated method stub
		return this.substancesLibrary;
	}

	public double[] getMinBounds() {
		// TODO Auto-generated method stub
		return SimulationState.getLocal().minBoundary.clone();
	}

	public double[] getMaxBounds() {
		// TODO Auto-generated method stub
		return SimulationState.getLocal().maxBoundary.clone();
	}



	public static void saveToFile(String file)
	{
		try {
			ObjectOutputStream  outputStream = new ObjectOutputStream(new FileOutputStream(file));
			Hosts.write(outputStream);
			outputStream.writeObject(ECM.getInstance());
			SimulationStateManager.write(outputStream);
			outputStream.writeObject(ManagerResolver.I());
			outputStream.writeObject(ORR.I());
			outputStream.writeObject(DiffusionNodeManager.I());
			outputStream.writeObject(DiffReg.I());
			outputStream.close();
			OutD.println("saved state to "+file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private static String tobeloaded = null;
	public static String toBeLoaded()
	{
			return tobeloaded;
	}
	
	public static void setToBeLoaded(String name)
	{
		tobeloaded = name;
	}
	
	
	public static void readFromFile(String file)
	{
		try {
			if(!(new File(file).exists())) throw new RuntimeException("file "+file+" inexistent");
			ObjectInputStream  in = new ObjectInputStream(new FileInputStream(file));
			Hosts.read(in);
			ECM.instance = (ECM)in.readObject();
			SimulationStateManager.read(in);
			ManagerResolver.SetI((ManagerResolver) in.readObject());
			ORR.SetI((ORR)in.readObject());
			DiffusionNodeManager.SetI((DiffusionNodeManager)in.readObject());
			DiffReg.SetI((DiffReg)in.readObject());
			Hosts.executeAdditionalCommands();
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public AtomicInteger getCellidCounter() {
		return cellidcounter;
	}


	public AtomicInteger getPhysicalBondcounter() {
		return physicalBondcounter;
	}



	public AtomicLong getDiffusionNodeCounter() {
		return diffusionNodeCounter;
	}



	public AtomicLong getCommandCounter() {
		return commandCounter;
	}

	public boolean isAllowedPostitonInArtificialWall(double[] pos) {
		
		double [] max = getMaxBounds();
		double [] min = getMinBounds();
		
		if(max[0]<pos[0]) return false;
		if(max[1]<pos[1]) return false;
		if(max[2]<pos[2]) return false;
		if(min[0]>pos[0]) return false;
		if(min[0]>pos[1]) return false;
		if(min[0]>pos[2]) return false;
		return true;
		
	}

	public double[] forceFromArtificialWall(double[] pos, double d) {
		double [] max = getMaxBounds();
		double [] min = getMinBounds();
		double [] okpos = new double[3];
		
		okpos[0] = max[0]-d < pos[0] ? max[0]-d : min[0]+d > pos[0] ? min[0]+d : pos[0];
		okpos[1] = max[1]-d < pos[1] ? max[1]-d : min[1]+d > pos[1] ? min[1]+d : pos[1];
		okpos[2] = max[2]-d < pos[2] ? max[2]-d : min[2]+d > pos[2] ? min[2]+d : pos[2];
		
		pos[0] = okpos[0] + 0.25 * (okpos[0] - pos[0]);
		pos[1] = okpos[1] + 0.25 * (okpos[1] - pos[1]);
		pos[2] = okpos[2] + 0.25 * (okpos[2] - pos[2]);
		/*
		if(max[0]<pos[0]+d)
			pos[0] = max[0]-d;
		if(max[1]<pos[1]+d)
			pos[1] = max[1]-d;
		if(max[2]<pos[2]+d) 
			pos[2] = max[2]-d;
		if(min[0]>pos[0]-d)
			pos[0] = min[0]+d;
		if(min[1]>pos[1]-d)
			pos[1] = min[1]+d;
		if(min[2]>pos[2]-d) 
			pos[2] = min[2]+d;
		*/
		return pos;
	}
	
	
	public void clean()
	{
		ArrayAccessHashTable temp = new ArrayAccessHashTable();
		for(int i=0;i<this.physicalNodeList.size(); i++)
		{
			PhysicalNode n = this.physicalNodeList.get(i);
			if(n==null) continue;
			temp.put(n.getSoNode().getID() , n);
		}
		this.physicalNodeList = temp;
	}
	
	public void removeFromSimulation(Cell c)
	{
		SomaElement e = c.getSomaElement();
		this.removeCell(c);
		this.removePhysicalNode(e.getPhysical());
		this.removePhysicalSphere(e.getPhysicalSphere());
		this.removeSomaElement(e);
		ObjectReference r = e.getPhysicalSphere().getSoNode().getObjectRef();
		PartitionManager m = (PartitionManager) ManagerResolver.I().resolve(r.partitionId);
		m.remove(r);
	}
	
	public void removeFromSimulation(NeuriteElement e)
	{
		this.removePhysicalNode(e.getPhysical());
		this.removePhysicalCylinder(e.getPhysicalCylinder());
		this.removeNeuriteElement(e);
		e.getCell().getSomaElement().getPhysicalSphere().removeDaugther(e.getPhysicalCylinder());
		ObjectReference r = e.getPhysicalCylinder().getSoNode().getObjectRef();
		PartitionManager m = (PartitionManager) ManagerResolver.I().resolve(r.partitionId);
		m.remove(r);
	}
	
	public void removeFromSimulation(SomaElement e)
	{
		removeFromSimulation(e.getCell());
		
	}

	public static boolean drawExtendedDaughterTracker = false;
	public static boolean drawDaughterTracker = false;
	public static float maxLength = 1;
	public static int selectionType;
	public static int selectionType1;
	public static int selectionType2;
	public static int searchInt;
	public static String searchRegex = "";
	public static String searchRegex1  = "";
	public static String searchRegex2  = "";
	public static int searchIntLength;
	public static int searchIntLength1;
	public static int searchIntLength2;
	public static int Count;
	public static int daughterSearchInt; 
	public static int daughterSearchIntLength;
	public static int SearchIntLength1;
	public static int daughterCount;
	public static int daughterCount1;
	public static int daughterCount2;
	public static int generationOCounter = 1;
	public static int generation1Counter = 1;
	public static int generation2Counter = 1;
}
