package ini.cx3d.biology;

import static ini.cx3d.utilities.Matrix.add;
import static ini.cx3d.utilities.Matrix.perp3;
import static ini.cx3d.utilities.Matrix.randomNoise;
import static ini.cx3d.utilities.Matrix.rotAroundAxis;
import ini.cx3d.Param;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.simulation.ECM;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;
import ini.cx3d.utilities.Matrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
//
public class NeuriteElement extends CellElement {

	/* The PhysicalObject this NeuriteElement is associated with.*/
	private PhysicalCylinder physicalCylinder = null;

	/* true if part of an axon, false if dendrite.*/
	private boolean isAnAxon = false;


	private SpaceNodeFacade somaSpacenode = null;


	// *************************************************************************************
	//   Constructor & stuff
	// *************************************************************************************

	public NeuriteElement() {
		super();
	
	}

	/** Note : doesn't copy the <code>LocalBiologyModule</code> list 
	 * (this is done in <code>branch()</code> etc.).*/
	public NeuriteElement getCopy() {
		NeuriteElement ne = new NeuriteElement();
		ne.isAnAxon = isAnAxon;
		ne.somaSpacenode = this.somaSpacenode;
		ne.properties = new HashMap<String, String>(properties);
		return ne;
	}



	// *************************************************************************************
	//   Run
	// *************************************************************************************


	public void run() {
		// run local biological modules...
		runLocalBiologyModules();
		
	}

	// *************************************************************************************
	//   Movements
	// *************************************************************************************


	/** Retracts the Cylinder associated with this NeuriteElement, if it is a terminal one.
	 * @param speed the retraction speed in micron/h
	 */
	public void retractTerminalEnd(double speed) {
		physicalCylinder.retractCylinder(speed);
	}

	/** Moves the point mass of the Cylinder associated with this NeuriteElement, if it is a terminal one.
	 *  BUT : if "direction" points in an opposite direction than the cylinder axis, i.e.
	 *  if the dot product is negative, there is no movement (only elongation is possible).
	 * @param speed
	 * @param direction
	 */
	public void elongateTerminalEnd(double speed, double[] direction){
		physicalCylinder.extendCylinder(speed, direction);
	}


	// *************************************************************************************
	//   Branching & Bifurcating
	// *************************************************************************************

	/**
	 * Makes a side branch, i.e. splits this cylinder into two and puts a daughter right at the proximal half.
	 * @param newBranchDiameter
	 * @param growthDirection (But will be automatically corrected if not at least 45 degrees from the cylinder's axis).
	 * @return
	 */
	public NeuriteElement branch(double newBranchDiameter, double[] growthDirection) {
		// create a new NeuriteElement for side branch
		NeuriteElement ne = getCopy();
		
		ArrayList<LocalBiologyModule> list = new ArrayList<LocalBiologyModule>();
		for (LocalBiologyModule m : super.localBiologyModulesList) {
			if(m.isCopiedWhenNeuriteBranches()){
				LocalBiologyModule m2 = m.getCopy();
				list.add(m2);
			}
		}
		// define direction if not defined
		if(growthDirection==null){
			growthDirection = perp3(add(physicalCylinder.getUnitaryAxisDirectionVector(), randomNoise(0.1, 3)));

		}
		// making the branching at physicalObject level
		PhysicalCylinder pc1 = physicalCylinder.branchCylinder(Param.NEURITE_DEFAULT_ACTUAL_LENGTH, growthDirection);
		// linking biology and phyics
		ne.setPhysicalAndInstall(pc1);  // (this also sets the call back)
		// specifying the diameter we wanted
		pc1.setDiameter(newBranchDiameter);

		// Copy of the local biological modules:
		for (LocalBiologyModule m : list) {
			ne.addLocalBiologyModule(m);
		}
		
		return ne;
	}

	/**
	 * Makes a side branch, i.e. splits this cylinder into two and puts a daughter right at the proximal half.
	 * @param growthDirection (But will be automatically corrected if not at least 45 degrees from the cylinder's axis).
	 * @return
	 */
	public NeuriteElement branch(double[] growthDirection) {
		return branch(physicalCylinder.getDiameter(),growthDirection);
	}

	/**
	 * Makes a side branch, i.e. splits this cylinder into two and puts a daughter right at the proximal half.
	 * @param diameter of the side branch
	 * @return
	 */
	public NeuriteElement branch(double diameter) {
		double[] growthDirection = perp3(add(physicalCylinder.getAxis(), randomNoise(0.1, 3)));
		growthDirection = Matrix.normalize(growthDirection);
		//		growthDirection = add(
		//				physicalCylinder.getUnitaryAxisDirectionVector(),
		//				scalarMult(1, growthDirection));
		return branch(diameter,growthDirection);
	}

	/**
	 * Makes a side branch, i.e. splits this cylinder into two and puts a daughter right at the proximal half.
	 * @param diam of the side branch
	 * @return
	 */
	public NeuriteElement branch() {
		double newBranchDiameter = physicalCylinder.getDiameter();
		double[] growthDirection = perp3(add(physicalCylinder.getUnitaryAxisDirectionVector(), randomNoise(0.1, 3)));
		return branch(newBranchDiameter,growthDirection);
	}

	/**
	Returns <code>true</code> if it is a terminal cylinder with length of at least 1micron.
	 * @return
	 */
	public boolean bifurcationPermitted(){
		return physicalCylinder.bifurcationPermitted();
	}

	/**
	 * Bifurcation of a growth come (only works for terminal segments). 
	 * Note : angles are corrected if they are pointing backward.
	 * @param length of new branches
	 * @param diameter_1  of new daughterLeft
	 * @param diameter_2 of new daughterRight
	 * @param direction_1
	 * @param direction_2
	 * @return
	 */
	public NeuriteElement[] bifurcate(
			double length, 
			double diameter_1, 
			double diameter_2,
			double[] direction_1,
			double[] direction_2) {

		// 1) physical bifurcation
		PhysicalCylinder[] pc = physicalCylinder.bifurcateCylinder(length, direction_1, direction_2);
		// if bifurcation is not allowed...
		if(pc == null){
			(new RuntimeException("Bifurcation not allowed!")).printStackTrace();
			return null;
		}

		// 2) creating the first daughter branch
		NeuriteElement ne1 = getCopy();
		PhysicalCylinder pc1 = pc[0];
		ne1.setPhysicalAndInstall(pc1);
		pc1.setDiameter(diameter_1);

		// 3) the second one
		NeuriteElement ne2 = getCopy();
		PhysicalCylinder pc2 = pc[1];
		ne2.setPhysicalAndInstall(pc2);
		pc2.setDiameter(diameter_2);

		// 4) the local biological modules :
		for (int i = 0; i<super.localBiologyModulesList.size(); i++) {
			LocalBiologyModule m = super.localBiologyModulesList.get(i);
			// copy...
			if(m.isCopiedWhenNeuriteBranches()){
				// ...for the first neurite
				LocalBiologyModule m2 = m.getCopy();
				ne1.addLocalBiologyModule(m2);
				// ...for the second neurite
				m2 = m.getCopy();
				ne2.addLocalBiologyModule(m2);
			}
			// and remove
			if(m.isDeletedAfterNeuriteHasBifurcated()){
				super.localBiologyModulesList.remove(m);
			}
		}
		return new NeuriteElement[] {ne1, ne2};
	}

	/**
	 * 
	 * @param direction_1
	 * @param direction_2
	 * @return
	 */
	public NeuriteElement[] bifurcate(double [] direction_1, double [] direction_2) {
		// length :
		double l = Param.NEURITE_DEFAULT_ACTUAL_LENGTH;
		// diameters :
		double d = physicalCylinder.getDiameter();

		return bifurcate(l, d, d, direction_1, direction_2);
	}

	/**
	 * 
	 * @return
	 */
	public NeuriteElement[] bifurcate() {
		// length :
		double l = Param.NEURITE_DEFAULT_ACTUAL_LENGTH;
		// diameters :
		double d = physicalCylinder.getDiameter();
		// direction : (60 degrees between branches)
		double[] perpPlane = perp3(physicalCylinder.getSpringAxis());
		double angleBetweenTheBranches = Math.PI/3.0;
		double[] direction_1 = rotAroundAxis(physicalCylinder.getSpringAxis(), angleBetweenTheBranches*0.5, perpPlane);
		double[] direction_2 = rotAroundAxis(physicalCylinder.getSpringAxis(), -angleBetweenTheBranches*0.5, perpPlane);

		return bifurcate(l, d, d, direction_1, direction_2);

	}


	// *************************************************************************************
	//   Synapses
	// *************************************************************************************


	/**
	 * Links the free boutons of this neurite element to adjacents free spines
	 * @param probabilityToSynapse probability to make the link.
	 * @return
	 */
	public Synapse synapse(){
		int synapseMade = 0;
		if(this.getPhysical().getExcrescence()==null) return null;
		for (PhysicalNode pn : physicalCylinder.getNeighboringPhysicalNodes()) {
			if(this.getPhysical().isRelative((PhysicalObject)pn)) continue;
			PhysicalObject po = (PhysicalObject)pn;
			if(po.getExcrescence()==null) continue;
			
			Synapse s = this.getPhysical().getExcrescence().synapseWith(po.getExcrescence());
			if(s!=null) 
				return s;
		}
		return null;
	}

	// *************************************************************************************
	//   Getters & Setters
	// *************************************************************************************


	public PhysicalObject getPhysical() {
		return physicalCylinder;
	}

	public void setPhysicalAndInstall(PhysicalObject physical) {
		this.physicalCylinder = (PhysicalCylinder) physical;
		this.physicalCylinder.setNeuriteElement(this); // callback
		physical.installLocally();
	}

	public PhysicalCylinder getPhysicalCylinder(){
		return (PhysicalCylinder)physicalCylinder;
	}

	public void setPhysicalCylinder(PhysicalCylinder physicalcylinder){
		physicalCylinder = physicalcylinder;
		physicalCylinder.setNeuriteElement(this);
	}

	/** Returns true if this NeuriteElement is an axon. Hence false if it is a dendrite*/
	public boolean isAnAxon(){
		return isAnAxon;
	}

	/** True means that this NeuriteElement is an axon. Hence false means it is a dendrite*/
	public void setIsAnAxon(boolean isAnAxon){
		this.isAnAxon = isAnAxon;
	}

	@Override
	public boolean isASomaElement() {
		return false;
	}
	@Override
	public boolean isANeuriteElement(){
		return true;
	}

	/**
	 * @return the (first) distal <code>NeuriteElement</code>, if it exists,
	 * i.e. if this is not the terminal segment (otherwise returns <code>null</code>).  
	 */
	public NeuriteElement getDaughterLeft() {
		if (physicalCylinder.getDaughterLeft() == null) { 
			return null;
		} else {
			return physicalCylinder.getDaughterLeft().getNeuriteElement();
		}
	}

	/**
	 * @return the second distal <code>NeuriteElement</code>, if it exists 
	 * i.e. if there is a branching point just after this element (otherwise returns <code>null</code>).  
	 */
	public NeuriteElement getDaughterRight() {
		if (physicalCylinder.getDaughterRight() == null) { return null;
		} else {
			NeuriteElement ne = physicalCylinder.getDaughterRight().getNeuriteElement();
			return ne; 
		}
	}

	// *************************************************************************************
	//   Traverse Tree
	// *************************************************************************************

	/**
	 * Adds to a Vector of NeuriteElements (NE) all the NE distal to this particular NE (including it). 
	 * @param elements the vector where it should be added.
	 * @return
	 */
	public ArrayList<NeuriteElement> AddYourselfAndDistalNeuriteElements(ArrayList<NeuriteElement> elements){
		elements.add(this);
		NeuriteElement dL = getDaughterLeft();
		if(dL!=null){
			dL.AddYourselfAndDistalNeuriteElements(elements);
			NeuriteElement dR = getDaughterRight();
			if(dR!=null){
				dR.AddYourselfAndDistalNeuriteElements(elements);
			}
		}
		return elements;
	}

	@Override
	public Cell getCell() {

		return ((PhysicalSphere) somaSpacenode.getUserObject()).getSomaElement().getCell();


	}

	@Override
	public void setCell(Cell cell) {
		somaSpacenode = cell.getSomaElement().getPhysical().getSoNode();
	}

	@Override
	public void installLocally() {
		ECM.getInstance().addNeuriteElement(this);
	}
	@Override
	public void removeLocally() {

		//		ShowConsoleOutput.println("removing localy neurite element"+getPhysical().getSoNode().getID());
		ECM.getInstance().removeNeuriteElement(this);

	}

	@Override
	public void deserialize(DataInputStream is) throws IOException {
		super.deserialize(is);
		this.isAnAxon =  is.readBoolean();
		
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		super.serialize(os);
		os.writeBoolean(isAnAxon);
		
	}

}
