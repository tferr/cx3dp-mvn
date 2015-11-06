package ini.cx3d.biology;

import static ini.cx3d.utilities.Matrix.add;
import ini.cx3d.Param;
import ini.cx3d.biology.synapse2.SomaticSpine;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.simulation.ECM;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class SomaElement extends CellElement{

	/* The PhysicalSphere associated with this SomaElement.*/
	private PhysicalSphere physical = null ;

	/* The cells.Cell this CellElement belongs to.*/
	protected Cell cell;
	
	// *************************************************************************************
	//   Constructor and divide
	// *************************************************************************************
	public SomaElement(){
		super();
	}

	public SomaElement divide(double volumeRatio, double [] dir){
		SomaElement newSoma = new SomaElement();
		PhysicalSphere pc = physical.divide(volumeRatio, dir); 
		newSoma.setPhysicalAndInstall(pc);   // this method also sets the callback
		newSoma.properties = new HashMap<String, String>(properties);
		// Copy of the local biological modules:
		for (LocalBiologyModule m : super.localBiologyModulesList) {
			if(m.isCopiedWhenSomaDivides()){
				LocalBiologyModule m2 = m.getCopy();
				newSoma.addLocalBiologyModule(m2);
			}
		}
		return newSoma;
	}


	// *************************************************************************************
	//   Run
	// *************************************************************************************

	public void run(){	 
		runLocalBiologyModules();
		
	}
	

	// *************************************************************************************
	//   Extend neurites
	// *************************************************************************************

	
	public NeuriteElement extendNewNeurite(){
		return extendNewNeurite(Param.NEURITE_DEFAULT_DIAMETER);
	}
	
	/**
	 * Extends a new neurite at a random place on the sphere
	 * @param diameter the diameter of the new neurite
	 * @param phi the angle from the zAxis
	 * @param theta the angle from the xAxis around the zAxis
	 * @return
	 */
	public NeuriteElement extendNewNeurite(double diameter) {
		// find random point on sphere (based on : http://www.cs.cmu.edu/~mws/rpos.html)
//		
		
		
//		double R = physical.getDiameter()*0.5;
//		double z = - R + R*ecm.getRandomDouble();
//		double phi = Math.asin(z/R);
//		double theta = 6.28318531*ecm.getRandomDouble();
		
		//andreas thinks this gives a better distribution based on some friends of mine.
		double phi =(ECM.getRandomDouble()-0.5f)*2*Math.PI;
		double theta =Math.asin(ECM.getRandomDouble()*2-1) + Math.PI/2;

		return extendNewNeurite(diameter ,phi, theta);
	}

	public NeuriteElement extendNewNeurite(double[] directionInGlobalCoordinates){
		// we do this cause transform is for 2 points in space and not for a direction:
		double[] dir = add(directionInGlobalCoordinates, physical.getMassLocation());
		double[] angles = physical.transformCoordinatesGlobalToPolar(dir);
		return extendNewNeurite(Param.NEURITE_DEFAULT_DIAMETER, angles[1], angles[2]);
	}
	
	public NeuriteElement extendNewNeurite(double diameter, double[] directionInGlobalCoordinates){
		// we do this cause transform is for 2 points in space and not for a direction:
		double[] dir = add(directionInGlobalCoordinates, physical.getMassLocation());
		double[] angles = physical.transformCoordinatesGlobalToPolar(dir);
		return extendNewNeurite(diameter, angles[1], angles[2]);
	}
	
	/**
	 * Extends a new neurites
	 * @param diameter the diameter of the new neurite
	 * @param phi the angle from the zAxis
	 * @param theta the angle from the xAxis around the zAxis
	 * @return
	 */
	public NeuriteElement extendNewNeurite(double diameter, double phi, double theta) {
		// creating the new NeuriteElement and PhysicalCylinder, linking them
		double lengthOfNewCylinder = Param.NEURITE_DEFAULT_ACTUAL_LENGTH;
		NeuriteElement ne = new NeuriteElement();
		ne.properties = new HashMap<String, String>(properties);
		PhysicalCylinder pc = physical.addNewPhysicalCylinder(lengthOfNewCylinder, phi, theta);
		
		// setting ref for Cell
		ne.setCell(this.cell);
		
		ne.setPhysicalAndInstall(pc);
		// setting diameter for new branch
		pc.setDiameter(diameter, true);
		
		// copy of the biological modules
		for (LocalBiologyModule module : localBiologyModulesList) {
			if(module.isCopiedWhenNeuriteExtendsFromSoma())
				ne.addLocalBiologyModule(module.getCopy());
		}
		// return the new neurite	
		return ne;
	}
	
	

	
	
	// Roman: Begin
	

	// *************************************************************************************
	//   Synapses
	// **
	
	/**
	 * Makes somatic spines (the physical and the biological part) dependent on some parameter (e.g. substance concentration) on this NeuriteElement.
	 * @param probability to make a spine and maximal nr of spines allowed on soma.
	 */
	public void MakeSomaticSpine(){
		
		
		double radius = physical.getDiameter()/2;
		physical.setExcrescence(new SomaticSpine());
			
	}
	// Roman

	// *************************************************************************************
	//   Getters & Setters
	// *************************************************************************************

	public PhysicalObject getPhysical() {
		return this.physical;
	}

	public void setPhysicalAndInstall(PhysicalObject physical) {
		this.physical = (PhysicalSphere) physical;
		this.physical.setSomaElement(this); // callback
		physical.installLocally();
	}
	
	

	public PhysicalSphere getPhysicalSphere(){
		return physical;
	}

	public void setPhysicalSphere(PhysicalSphere physicalsphere){
		physical = physicalsphere;
		physical.setSomaElement(this);
	}

	public ArrayList<NeuriteElement>  getNeuriteList() {
		ArrayList<NeuriteElement> neuriteList = new ArrayList<NeuriteElement>();        
		ArrayList<PhysicalCylinder> pcList = physical.getDaughters();        
		for (Iterator<PhysicalCylinder> element = pcList.iterator(); element.hasNext();) {
			PhysicalCylinder pc = (PhysicalCylinder)element.next();
			neuriteList.add(pc.getNeuriteElement());
		}        
		return neuriteList ;
	}
	
	public void removeDaughter(NeuriteElement e)
	{
	
		this.getPhysicalSphere().removeDaugther(e.getPhysical());
	}

	public boolean isANeuriteElement() {
		return false;
	}
	
	/** Returns true, because this <code>CellElement</code> is a <code>SomaElement</code>.*/ 
	public boolean isASomaElement(){
		return true;
	}

	@Override
	public Cell getCell() {
		// TODO Auto-generated method stub
		return cell;
	}

	@Override
	public void setCell(Cell cell) {
		this.cell = cell;
		
		ECM.getInstance().addCell(this.cell);
	}

	
	@Override
	public void installLocally() {
	
		ECM.getInstance().addSomaElement(this);
		if(cell!=null) ECM.getInstance().addCell(this.cell);
	}

	@Override
	public void removeLocally() {
		ECM.getInstance().removeSomaElement(this);
		ECM.getInstance().removeCell(this.cell);
	}

	@Override
	public void deserialize(DataInputStream is) throws IOException {
		super.deserialize(is);
		cell = new Cell();
		cell.somaElement = this;
		cell.deserialize(is);
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		super.serialize(os);
		cell.serialize(os);
	}
}
