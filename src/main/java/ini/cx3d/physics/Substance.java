package ini.cx3d.physics;

import ini.cx3d.Param;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.XMLSerializable;

import java.awt.Color;
import java.io.Serializable;

import org.w3c.dom.Node;

/**
 * Represents a diffusible or non-diffusible chemical, whether it is extra-cellular, membrane-bounded
 * or intra-cellular.
 * @author fredericzubler
 *
 */


public class Substance  implements XMLSerializable, Serializable{

	/* Name of the Substance.               */
	//asdfasf
	protected String id;
	/* How fast it is diffused by the methods in the PhysicalNode. */
	protected double diffusionConstant = 1000;
	/* How rapidly it is degraded by the PhysicalNode.*/
	protected double degradationConstant = 0.01;
	/* The color used to represent it if painted.*/
	protected Color color = Color.BLUE;

	/* The total amount present at a given PhysicalNode, or inside a PhysicalObject.*/
	protected double futurequantity = 0;
	protected double quantity = 0;

	public Substance(){}


	public Substance(String id, double diffusionConstant, double degradationConstant){
		this.id = id;
		this.diffusionConstant = diffusionConstant;
		this.degradationConstant = degradationConstant;
		if(!ECM.getInstance().getSubstanceTemplates().containsKey(this.id))
		{
			ECM.getInstance().addNewSubstanceTemplate(this);
		}
	}

	/**
	 * Especially used for the "artificial gradient" formation, in ECM.
	 * @param id
	 * @param color
	 */
	public Substance(String id, Color color){
		this.id = id;
		this.color = color;
	}

	/**
	 * Increases or decreases the quantity. Makes sure the quantity is never negative.
	 * @param deltaQ
	 */
	public void  changeQuantityFrom(double deltaQ){
		addQuantity(deltaQ);
	}

	/**
	 * Increases or decreases the quantity. Makes sure the quantity is never negative.
	 * @param deltaQ
	 */
	public void addQuantity(double deltaQ){
		synchronized(this) {
			futurequantity += deltaQ;
			futurequantity = Math.max(0,futurequantity);
		}
	}

	/** Well, as the name says, it multiplies the quantity and the concentration
	 * by a certain value. This method is mainly used for degradation .*/
	public void degrade(Volumen o){
		synchronized(this) {
			double decay =  degradationConstant*quantity*Param.SIMULATION_TIME_STEP;
			futurequantity -=decay;
		}
	}
	


	/**
	 * Determines whether an other object is equal to this Substance. 
	 * <br>The result is <code>true</code> if and only if the argument 
	 * is not null and is a Substance object with the same id, color, 
	 * degradationConstant and diffusionConstant. The
	 * quantity and concentration are note taken into account.
	 */
	public boolean equals(Object o){

		if (o instanceof Substance) {

			Substance s = (Substance) o;

			if(		s.id.equals(this.id) && 
					s.color.equals(this.color) &&
					Math.abs(s.degradationConstant - this.degradationConstant)<1E-10 &&
					Math.abs(s.diffusionConstant - this.diffusionConstant)<1E-10 
			){
				return true;
			}

		}
		return false;
	}

	/**
	 * Returns the color scaled by the concentration. Useful for painting PhysicalObjects / PhysicalNode
	 * based on their Substance concentrations.
	 * @return scaled Color
	 */
	public Color getConcentrationDependentColor(Volumen a){
		int alpha = (int)(255.0*getConcentration(a)*1.0);
		//ShowConsoleOutput.println(alpha);
		if(alpha<0){
			alpha = 0;
		}else if(alpha>255){
			alpha = 255;
		}
		return new Color(color.getRed(), color.getGreen(), color.getBlue(),alpha );
	}



	// --------- GETTERS & SETTERS--------------------------------------------------------


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getDiffusionConstant() {
		return diffusionConstant;
	}

	public void setDiffusionConstant(double diffusionConstant) {

		this.diffusionConstant = diffusionConstant;

	}

	public double getDegradationConstant() {
		return degradationConstant;
	}

	public void setDegradationConstant(double degradationConstant) {
		this.degradationConstant = degradationConstant;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public double getConcentration(Volumen a) {
		if(a.getVolume()==0) return 0;
		return quantity/a.getVolume();		
	}

	public void setConcentration(double concentration,Volumen a) {
		futurequantity = a.getVolume()*concentration;
	}

	public double getQuantity() {
		return quantity;
	}
	
	public double getFutureQuantity() {
		return futurequantity;
	}

	public void setQuantity(double quantity) {
		this.futurequantity = quantity;
	}

	protected StringBuilder createXMLAttrubutes()
	{
		StringBuilder temp= new StringBuilder();
		temp.append("name=\"").append(this.id).append("\" ");
		temp.append("color=\"").append(this.color.getRGB()).append("\" ");
		temp.append("degradationConstant=\"").append(this.degradationConstant).append("\" ");
		temp.append("diffusionConstant=\"").append(this.diffusionConstant).append("\" ");
		temp.append("quantity=\"").append(this.futurequantity).append("\" ");
		return temp;
	}

	protected void readOutAttributes(Node attr)
	{
		if(attr.getNodeName().equals("name"))
		{
			this.id = attr.getNodeValue();
			
		}
		else if(attr.getNodeName().equals("color"))
		{

			this.color = new Color(Integer.parseInt(attr.getNodeValue()));
		
		}
		else if(attr.getNodeName().equals("degradationConstant"))
		{

			this.degradationConstant =  Double.parseDouble(attr.getNodeValue());

		}
		else if(attr.getNodeName().equals("diffusionConstant"))
		{
			
			this.diffusionConstant =  Double.parseDouble(attr.getNodeValue());

		}
		else if(attr.getNodeName().equals("quantity"))
		{
			this.futurequantity =  Double.parseDouble(attr.getNodeValue());
		}	
	}

	public XMLSerializable fromXML(Node xml) {

		for(int i=0;i<xml.getAttributes().getLength();i++)
		{
			Node attr =  xml.getAttributes().item(i);
			readOutAttributes(attr);
		}
		return this;
	}

	public StringBuilder toXML(String ident) {

		StringBuilder temp = new StringBuilder();
		temp.append(ident).append("<substance ").append(createXMLAttrubutes());
		temp.append(" />\n");
		return temp;
	}


	public Substance getCopy() {
		// TODO Auto-generated method stub
		Substance s = new Substance();
		s.color = this.color;
		s.degradationConstant = this.degradationConstant;
		s.diffusionConstant = this.diffusionConstant;
		s.id = this.id;
		//s.futurequantity = this.futurequantity;
		return s;
	} 

	public void applyCalculations()
	{
		quantity = futurequantity;
	}
}
