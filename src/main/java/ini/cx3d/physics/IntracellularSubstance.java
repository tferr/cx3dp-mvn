package ini.cx3d.physics;

import ini.cx3d.Param;

import org.w3c.dom.Node;

/** 
 * Instances of this class represent the intracellular and surface (membrane bound) 
 * Substances. The visibility from outside (fact that they are expressed on the surface)
 * is specified by the appropriate getter and setter. 
 * 
 * @author fredericzubler
 *
 */
public class IntracellularSubstance extends Substance{

	/* If true, the Substance can be detected from outside of the PhysicalObject
	 * (equivalent to an membrane bound substance).*/
	private boolean visibleFromOutside = false;
	/* If true, the volume is taken into account for computing the concentration,
	 * otherwise quantity and volume are considered as equivalent (effective volume = 1).*/
	private boolean volumeDependant = false;

	/* For degradation we need to know the concentration that was present at the previous time step! */

	/* Degree of asymmetric distribution during cell division. 
	 * 0 represents complete symmetrical division, 1 represents complete asymmetric division. */
	protected double asymmetryConstant = 0;
	
	/* Probabilty of asymmetric distribution during cell division. */
	protected double asymmetryProbabilty = 1;

	public IntracellularSubstance(){
	}

	public IntracellularSubstance(String id, double diffusionConstant, double degradationConstant){
		super(id, diffusionConstant, degradationConstant);
	}

	public IntracellularSubstance(String id, double diffusionConstant, double degradationConstant, double asymmetryConstant){
		super(id, diffusionConstant, degradationConstant);
		this.asymmetryConstant = asymmetryConstant;
	}
	
	public IntracellularSubstance(String id, double diffusionConstant, double degradationConstant, double asymmetryConstant, double asymmetryProbabilty){
		super(id, diffusionConstant, degradationConstant);
		this.asymmetryConstant = asymmetryConstant;
		this.asymmetryProbabilty = asymmetryProbabilty;
	}
	
	/**
	 * Distribute IntracellularSubstance quantity at division.
	 * @param physicalSphere 
	 * @param newIS
	 */
	public void distributeQuantityOnDivision(Substance newIS){
		double recalc = 1/2.0*(1+asymmetryConstant);
		newIS.setQuantity(this.getQuantity() * (recalc));
		this.setQuantity(this.getQuantity() * (1-recalc));
		newIS.applyCalculations();
		this.applyCalculations();
	}


	/**
	 * Degradation of the <code>IntracellularSubstance</code>.
	 * @param newIS
	 */
	public void degrade(Volumen o){
		addQuantity(-this.quantity *this.degradationConstant*Param.SIMULATION_TIME_STEP);		
	}


	/** Returns the degree of asymmetric distribution during cell division. 
	 * 0 represents complete symmetrical division, 1 represents complete asymmetric division. */
	public double getAsymmetryConstant(){

		return asymmetryConstant;

	}

	/** Sets the degree of asymmetric distribution during cell division. 
	 * 0 represents complete symmetrical division, 1 represents complete asymmetric division.
	 * The sign + or - is used to distinguish between one daughter (mother cell) and the other
	 * (new cell). */
	public void setAsymmetryConstant(double asymmetryConstant){
		this.asymmetryConstant = asymmetryConstant;
	}

	public double getAsymmetryProbabilty() {
		return asymmetryProbabilty;
	}

	public void setAsymmetryProbabilty(double asymmetryProbabilty) {
		this.asymmetryProbabilty = asymmetryProbabilty;
	}

	/** If true, the Substance can be detected from outside of the PhysicalObject
	 * (equivalent to an membrane bound substance).*/
	public boolean isVisibleFromOutside() {

		return visibleFromOutside;

	}

	/** If true, the Substance can be detected from outside of the PhysicalObject
	 * (equivalent to an membrane bound substance).*/
	public void setVisibleFromOutside(boolean visibleFromOutside) {

		this.visibleFromOutside = visibleFromOutside;

	}
	/** If true, the volume is taken into account for computing the concentration,
	 * otherwise a virtual volume corresponding to the length of the physical object
	 * (with virtual radius 1) is used.*/
	public boolean isVolumeDependant() {

		return volumeDependant;

	}
	/** If true, the volume is taken into account for computing the concentration,
	 * otherwise a virtual volume corresponding to the length of the physical object
	 * (with virtual radius 1) is used.*/
	public void setVolumeDependant(boolean volumeDependant) {

		this.volumeDependant = volumeDependant;

	}

	public StringBuilder toXML(String ident) {

		StringBuilder temp = new StringBuilder();
		temp.append(ident).append("<IntracelularSubstance ").append(createXMLAttrubutes());
		temp.append(" />\n");
		return temp;
	}
	
	public StringBuilder createXMLAttrubutes()
	{
		StringBuilder temp  = super.createXMLAttrubutes();

		temp.append("visibleFromOutside=\"").append(this.isVisibleFromOutside()).append("\" ");
		temp.append("volumeDependent=\"").append(this.isVolumeDependant()).append("\" ");
		temp.append("asymmetryconstant=\"").append(this.asymmetryConstant).append("\" ");
		return temp;

	}

	public void readOutAttributes(Node attr)
	{
		super.readOutAttributes(attr);
		if(attr.getNodeName().equals("visibleFromOutside"))
		{
			this.visibleFromOutside = Boolean.parseBoolean(attr.getNodeValue());
		}
		if(attr.getNodeName().equals("volumeDependent"))
		{
			this.volumeDependant = Boolean.parseBoolean(attr.getNodeValue());
		}
		if(attr.getNodeName().equals("asymmetryconstant"))
		{
			this.asymmetryConstant = Double.parseDouble(attr.getNodeValue());
		}
	} 
	
	public String toString()
	{
		return " quant :"+quantity+" future "+futurequantity;
	}
	
	public Substance getCopy() {
		IntracellularSubstance s = new IntracellularSubstance();
		s.color = this.color;
		s.asymmetryConstant = this.asymmetryConstant;
		s.asymmetryProbabilty = this.asymmetryProbabilty;
		s.degradationConstant = this.degradationConstant;
		s.diffusionConstant = this.diffusionConstant;
		s.id = this.id;
		s.futurequantity = this.futurequantity;
		return s;
	}

	public double getConcentration(Volumen a) {
		if(volumeDependant)
		{
			return super.getConcentration(a);
		}
		else
		{
			return quantity;
		}
	}

	public void setConcentration(double concentration,Volumen a) {
		if(volumeDependant)
		{
			super.setConcentration(concentration, a);
		}
		else
		{
			futurequantity = concentration;
		}
	}

}
