package ini.cx3d.physics.diffusion;

import static ini.cx3d.utilities.Matrix.distance;
import ini.cx3d.Param;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.Substance;
import ini.cx3d.physics.Volumen;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.Matrix;
import ini.cx3d.utilities.Rectangle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.print.attribute.standard.MediaSize.Other;

public class DiffusionNode extends AbstractDiffusionNode{

	private static final long serialVersionUID = 5945983742138203901L;
	private HashT<String,Substance> substances = new HashT<String,Substance>();
	private double lastECMTimeDegradateWasRun;
		
	public DiffusionNode(double[] upperLeftCornerFront,double [] lowerRightCornerBack,AbstractDiffusionNode i,int childnbr)
	{
		super(upperLeftCornerFront,lowerRightCornerBack,i,childnbr);
	}
	
	public DiffusionNode(double[] upperLeftCornerFront,double [] lowerRightCornerBack)
	{
		super(upperLeftCornerFront,lowerRightCornerBack);
	}
	

	public DiffusionNode() {
		// TODO Auto-generated constructor stub
	}




	protected void diffusionAlgorithm() {
		if(resistance==Double.MAX_VALUE) return;
		this.degradate(ECM.getInstance().getECMtime());
		
		
		//calculate in/out flowing quantity from neighbours
		ArrayList<String> keys = DiffusionNodeManager.I().getSubstances();
		
		for(Substance sub: substances.values())
		{
			if(!ECM.getInstance().allArtificialSubstances.containsKey(sub.getId()))
			{
				ECM.getInstance().allArtificialSubstances.put(sub.getId(),sub.getCopy());
			}
		}
		
		for (AbstractDiffusionNode n_other : this.getNeighbours()) {
			if(n_other.getResistance()==Double.MAX_VALUE) continue;
			double distance = Matrix.distance(n_other.address.getCenter(), this.address.getCenter());
			Rectangle r_other=n_other.address.getCommonSide(this.address) ;
			Rectangle r_this= this.address.getCommonSide(n_other.address);
			double crosssection = Math.min(
					r_other.getCrossSectionSize(),
					r_this.getCrossSectionSize());
			
			
			for (String key: keys) {
				
				if(!n_other.getSubstances().containsKey(key)) continue;
				Substance s_other = n_other.getSubstances().get(key);
				
				if(!this.getSubstances().containsKey(key))
				{			
					this.substances.put(key,s_other.getCopy());
				}
				Substance s_this = this.getSubstance(key);
	
				
				// J is the diffusion flux in dimensions of [(amount of substance) length-2 time-1] we consider D in (micomeeter^2/houer)
				double this_C = s_this.getConcentration(this);
				double other_C = s_other.getConcentration(n_other);
				
				double diff_C =other_C- this_C;
				
				double D = s_this.getDiffusionConstant();
				
				double J  =   diff_C*D/distance;
				double amount = J*crosssection*Param.SIMULATION_TIME_STEP;
				if(amount>0 && s_other.getQuantity()<amount)
				{
					return;
				}
				if(amount<0 && s_this.getQuantity()<-amount)
				{
					return;
				}
				s_this.addQuantity(amount);
				
			}
		}
	}
	
	
	@Override
	protected double getInternalConcentreation(String substanceid, double[] pos) {
		
		//according to shephards method.
		double p=1; // weigting parameter
		double diagonal = distance(address.getUpperLeftCornerFront(),address.getLowerRightCornerBack());
		double dist =  distance(this.address.getCenter(),pos);
		double conc = getInternalConcentration(substanceid);
		if(dist<diagonal/1000)
		{
			return conc;
		}
		
		double currentw =  1/Math.pow(p,(dist));
		
		double totalw = currentw;
		double totalshepard = conc*currentw; 
//		ShowConsoleOutput.println("totalw = "+totalw +" totalshep "+totalshepard);
		for(AbstractDiffusionNode n: getNeighbours())
		{
			dist =  distance(n.address.getCenter(),pos);
			conc = n.getInternalConcentration(substanceid);
			currentw = 1/Math.pow(p,(dist));
			totalw +=currentw;
			totalshepard += conc*currentw;
//			ShowConsoleOutput.println("totalw = "+totalw +" totalshep "+totalshepard);
		}
		
		return totalshepard/totalw;
	}

	@Override
	protected AbstractDiffusionNode getNewInstance(double[] upperLeftCornerFront,double [] lowerRightCornerBack,AbstractDiffusionNode i,int childnbr) {
		return new DiffusionNode(upperLeftCornerFront, lowerRightCornerBack,i,childnbr);
	}


	@Override
	public void changeSubstanceQuantityIternaly(String id,double deltaD,double [] pos) {
		setResistance(0);
		if(!substances.containsKey(id))
		{
			substances.put(id, ECM.getInstance().getSubstanceTemplates().get(id).getCopy());
		}
		substances.get(id).changeQuantityFrom(deltaD);
	}


	@Override
	public double getInternalConcentration(String substanceID) {
		if(substances.get(substanceID)==null) 
		{
//			ShowConsoleOutput.println(substances.size());
//			ShowConsoleOutput.println("why!!");
			return 0;
		}
		return substances.get(substanceID).getConcentration(this);
//		return address.id;
	}
	

	
	protected void print(String ident)
	{
		super.print(ident);
		
		for	(Substance s : this.substances.values()) {
			OutD.println(s.toXML(ident+"   "));
		}
	}
	
	/** 
	 * Runs the degradation of all Substances (only if it is not up-to-date). This method 
	 * is called before each operation on Substances ( 
	 * @param currentEcmTime the current time of the caller 
	 * (so that it doesn't require a call to ECM). 
	 */
	protected void degradate(double currentEcmTime){ //changed to proteceted
		
		if(lastECMTimeDegradateWasRun == currentEcmTime){
			return;
		}
		for (Substance s : substances.values()) {
				s.degrade(this);
		}
		lastECMTimeDegradateWasRun= currentEcmTime;

	}

	@Override
	public Substance getSubstance(String id2) {

		return substances.get(id2);
	}


	@Override
	public HashT<String, Double> getAllQuantaties() {
		HashT<String, Double> temp= new HashT<String, Double>();
		for (Substance s : substances.values()) {
			temp.put(s.getId(), s.getQuantity());
		}
		return temp;
	}

	public String toString()
	{
		String test = "DiffusionNode "+this.getAddress()+" "+super.toString()+"\n";
		for (String s  : substances.keySet()) {
			test+=s+":"+substances.get(s).getConcentration(this)+":"+substances.get(s).getQuantity()+":"+substances.get(s)+"\n";
		}
		return test;
	}

	@Override
	protected void distributeSuntances(ArrayList<AbstractDiffusionNode> newnodes) {
		for (Substance s : substances.values()) {
			for (AbstractDiffusionNode a : newnodes) {
				DiffusionNode d = (DiffusionNode) a;
				Substance new_s= s.getCopy();
				new_s.setConcentration(s.getConcentration(this),this);
				d.substances.put(s.getId(),new_s);
			}
		}
		substances.clear();
	}

	public HashT<String, Substance> getSubstances() {
		// TODO Auto-generated method stub
		return substances;
	}

	@Override
	public void applyCalculations() {
		for (Substance s : substances.values()) {
			s.applyCalculations();
		}
		
	}


	@Override
	protected double[] getInternalGradient(String substanceid, double[] pos) {
		
		double [] dirtotal = new double[]{0,0,0};
		double max = 0;
		for (AbstractDiffusionNode d : getNeighbours()) {
			double [] dir  = Matrix.subtract(d.address.getCenter(),  pos);
			double dxdiff = (d.getInternalConcentration(substanceid)- this.getInternalConcentration(substanceid));
			if(max<dxdiff)
			{
				dirtotal = dir;
			}
		}
		return dirtotal;
	}


	@Override
	public void deserialize(DataInputStream is) throws IOException {
		
		super.deserialize(is);
		int to = is.readInt();
		for(int i=0;i<to;i++ ) {
			String key = is.readUTF();
			double val = is.readDouble();
			Substance s = new Substance();
			s.setId(key);
			s.setQuantity(val);
		}
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		super.serialize(os);
		os.writeInt(substances.size());
		for(Substance d : substances.values()) {
			os.writeUTF(d.getId());
			os.writeDouble(d.getQuantity());
		}
	
	}

	@Override
	public void setConcentration(String id, double d) {
		if(!substances.containsKey(id))
		{
			substances.put(id, ECM.getInstance().getSubstanceTemplates().get(id).getCopy());
		}
		substances.get(id).setConcentration(d,this);
		
	}
	
	
}
