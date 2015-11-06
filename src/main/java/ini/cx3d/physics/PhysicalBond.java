/**
 * ###################UNFINISHED COMPLETELY NOT WORKING ATM############################
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.physics;

import static ini.cx3d.utilities.Matrix.norm;
import static ini.cx3d.utilities.Matrix.subtract;
import ini.cx3d.biology.Synapse;
import ini.cx3d.electrophysiology.ElectroPhysiolgySynapse;
import ini.cx3d.simulation.ECM;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;

import java.io.Serializable;


/**
 * This class represents an elastic bond between two physical objects.
 * It can be used (1) to represent a cell adhesion mechanism - zip/anchor- and
 * in this case is permanent, or (2) to force two cylinders that crossed
 * each other to come back on the right side, and in this case it vanishes
 * when the right conformation is re-established.
 * 
 * It works as a spring, with
 * a resting length and a spring constant, used to compute a force along the vector
 * joining the two ends, depending on the actual length. (Note that it is considered as a
 * real unique spring and not a linear spring constant as in PhysicalCylinder)
 *  
 * @author fredericzubler
 *
 */

public class PhysicalBond implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5330981642671468481L;

	private SpaceNodeFacade a;
	private SpaceNodeFacade b;
	private double restingLength;
	private double springConstant = 10;
	private double dumpingConstant = 0;
	
	private double breakingPointInPercent=150;
	private double movingPointInPercent=30;

	/** If false, there is no force transmitted on the first PhysicalObject (a).*/
	private boolean hasEffectOnA = true;
	/** If false, there is no force transmitted on the second PhysicalObject (b).*/
	private boolean hasEffectOnB = true;

	/* Unique identification for this CellElement instance. Used for marshalling/demarshalling*/
	private int ID = 0;
	
	public ElectroPhysiolgySynapse elSynapse;
	private Synapse synapse;

	public PhysicalBond(){
		ID = ECM.getInstance().getPhysicalBondcounter().incrementAndGet();
	}

	/** Creates a PhysicalBond between the point masses of the two PhysicalObjects
	 * given as argument, with resting length their actual distance from oneanother.
	 * @param a
	 * @param b
	 */
	public PhysicalBond(PhysicalObject a, PhysicalObject b){
		this();
		this.a = a.getSoNode();
		a.addPhysicalBond(this);
		this.b = b.getSoNode();
		b.addPhysicalBond(this);
		this.restingLength = norm(subtract(a.getMassLocation(), b.getMassLocation()));
		this.springConstant = 10;
		this.dumpingConstant = 0;
		
	}


	public PhysicalBond(PhysicalObject a, double[] positionOnA, PhysicalObject b , double[] positionOnB, double restingLength, double springConstant) {
		this();
		this.a = a.getSoNode();
		a.addPhysicalBond(this);
		this.b = b.getSoNode();
		b.addPhysicalBond(this);
		this.restingLength = restingLength;
		this.springConstant = springConstant;
	}

	
	public synchronized SpaceNodeFacade getFirstSoNode() {
		return this.a;
	}
	
	public synchronized SpaceNodeFacade getSecondSoNode() {
		return this.b;
	}
	
	public synchronized PhysicalObject getFirstPhysicalObject() {
		return getPhysicalA();
	}

	public synchronized PhysicalObject getSecondPhysicalObject() {
		return getPhysiaclB();
	}

	public synchronized void setFirstPhysicalObject(PhysicalObject a) {
		this.a = a.soNode;
	}

	public synchronized void setSecondPhysicalObject(PhysicalObject b) {
		this.b = b.soNode;
	}

	/** If false, the first PhysicalObject doesn't feel the influence of this PhysicalBond.*/
	public synchronized boolean isHasEffectOnA() {
		return hasEffectOnA;
	}
	/** If false, the first PhysicalObject doesn't feel the influence of this PhysicalBond.*/
	public synchronized void setHasEffectOnA(boolean hasEffectOnA) {
		this.hasEffectOnA = hasEffectOnA;
	}
	/** If false, the second PhysicalObject doesn't feel the influence of this PhysicalBond.*/
	public synchronized  boolean isHasEffectOnB() {
		return hasEffectOnB;
	}
	/** If false, the second PhysicalObject doesn't feel the influence of this PhysicalBond.*/
	public synchronized void setHasEffectOnB(boolean hasEffectOnB) {
		this.hasEffectOnB = hasEffectOnB;
	}


	public void exchangePhysicalObject(PhysicalObject oldPo, PhysicalObject newPo){
				if(oldPo.soNode.getID() == a.getID()){
					a = newPo.soNode;
				}else{
					b = newPo.soNode;
				}
				oldPo.removePhysicalBond(this);
				newPo.addPhysicalBond(this);				
	}

	public synchronized PhysicalObject getOppositePhysicalObject(PhysicalObject po) {
		if(po.soNode.getID() == a.getID()){
			return getPhysiaclB();
		}else{
			return getPhysicalA();
		}
	}

	public synchronized SpaceNodeFacade getOppositeSpatialOrganizationNode(SpaceNodeFacade soNode) {
		if(soNode.getID() == a.getID()){
			return b;
		}else{
			return a;
		}
	}


	/**
	 * Returns the force that this PhysicalBond is applying to a PhsicalObject.
	 * The function also returns the proportion of the mass that is applied to the 
	 * proximal end (mother's point mass) in case of PhysicalCylinder.
	 * (For PhysicalSpheres, the value p is meaningless).
	 * 
	 * @param po the PhysicalObject to which the force is being applied.
	 * @return [Fx,Fy,Fz,p] an array with the 3 force components and the proportion
	 * applied to the proximal end - in case of a PhysicalCylinder.
	 */
	public double[] getForceOn(PhysicalObject po){
		// 0. Find if this physicalBound has an effect at all on the object
		if( (po.getSoNode()==a && hasEffectOnA==false) || (po.getSoNode()==b && hasEffectOnB==false) )
			return new double[] {0,0,0};
		double movinglength = restingLength/100.0*(100.0+movingPointInPercent);
		double actual = getActualLength();
		// TODO: check that removing this is fine!
//		if(movinglength>actual)return new double[] {0,0,0};
			
		return PhysicalObject.interObjectForce.forceFromPhysicalBond(this,po);
	
		
	}

	/**
	 * Gets the location in absolute cartesian coord of the first insertion point (on a).
	 * (Only for graphical display).Raises a NullPointerException if a == null.
	 * @return x,y,z coord of the insertion point of one end
	 */
	public double[] getFirstEndLocation(){
		return a.getPosition();
	}

	/**
	 * Gets the location in absolute cartesian coord of the first insertion point (on a).
	 * (Only for graphical display). Raises a NullPointerException if b == null.
	 * @return x,y,z coord of the insertion point of one end
	 */
	public double[] getSecondEndLocation(){
		return b.getPosition();
	}


	public String toString(){
		return "My name is Bond, PhysicalBond ("+hashCode()+")";
	}

	/**
	 * @return the restingLength
	 */
	public synchronized double getRestingLength() {
		return restingLength;
	}

	/**
	 * @param restingLength the restingLength to set
	 */
	public synchronized void setRestingLength(double restingLength) {
		this.restingLength = restingLength;
	}

	/**
	 * @return the springConstant
	 */
	public synchronized double getSpringConstant() {
		return springConstant;
	}

	/**
	 * @param springConstant the springConstant to set
	 */
	public synchronized void setSpringConstant(double springConstant) {
		this.springConstant = springConstant;
	}


	/**
	 * @return the dumpingConstant
	 */
	public synchronized double getDumpingConstant() {
		return dumpingConstant;
	}

	/**
	 * @param dumpingConstant the dumpingConstant to set
	 */
	public synchronized void setDumpingConstant(double dumpingConstant) {
		this.dumpingConstant = dumpingConstant;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getID() {
		return ID;
	}


	public boolean equals(Object o)
	{
		if(o instanceof PhysicalBond)
		{
			return ((PhysicalBond)o).ID == this.ID;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return ID+10;
	}

	private PhysicalObject getPhysicalA()
	{
		if(a ==null) return null;
		return (PhysicalObject) a.getUserObject();

	}

	private PhysicalObject getPhysiaclB()
	{
		if(b ==null) return null;
		return (PhysicalObject) b.getUserObject();

	}

	public void vanish() {
		try {
			getFirstPhysicalObject().removePhysicalBond(this);
			getSecondPhysicalObject().removePhysicalBond(this);
			if(this.elSynapse!=null)elSynapse.removeLocally();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean broken= false;
	public boolean checkForBreak()
	{
		if(broken)
			return true;
		double actualLength = getActualLength();
		double breakinglength = restingLength/100.0*(100.0+getBreakingPointInPercent());
		broken =  breakinglength < actualLength;
		return broken;
	}

	public void setBreakingPointInPercent(double breakingPointInPercent) {
		this.breakingPointInPercent = breakingPointInPercent;
	}

	public double getBreakingPointInPercent() {
		return breakingPointInPercent;
	}
	
	
	public double getActualLength()
	{
		return norm(subtract(getFirstEndLocation(),getSecondEndLocation()));
	}

	private void setMovingPointInPercent(double movingPointInPercent) {
		this.movingPointInPercent = movingPointInPercent;
	}

	private double getMovingPointInPercent() {
		return movingPointInPercent;
	}
	
	public ElectroPhysiolgySynapse getElectroPhysiolgy() {
		// TODO Auto-generated method stub
		return elSynapse;
	}

	public void setElectroPhysiolgy( ElectroPhysiolgySynapse electroPhysiologySynapse) {
		elSynapse = electroPhysiologySynapse;
		electroPhysiologySynapse.installLocally();
	}
	
	
	public void installLocally() {
		if(this.elSynapse!=null)elSynapse.installLocally();
		if(this.synapse!=null)synapse.installLocally();
	}
	
	public void removeLocally() {
		if(this.elSynapse!=null)elSynapse.removeLocally();
		if(this.synapse!=null)synapse.removeLocally();
	}

	public void setSynapse(Synapse synapse) {
		this.synapse = synapse;
		synapse.installLocally();
	}

	public Synapse getSynapse() {
		return synapse;
	}
	
}
