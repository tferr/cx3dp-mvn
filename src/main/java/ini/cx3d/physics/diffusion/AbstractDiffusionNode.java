package ini.cx3d.physics.diffusion;


import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.physics.Substance;
import ini.cx3d.physics.Volumen;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.Matrix;
import ini.cx3d.utilities.serialisation.CustomSerializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;


public abstract class AbstractDiffusionNode implements Serializable, Volumen, CustomSerializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -5542519493299094034L;

	protected double resistance = 0;
	private ArrayList<Long> snodes = new ArrayList<Long>(8);
	private ArrayList<Long> nbours = new ArrayList<Long>();

	protected DiffusionAddress address;

	AbstractDiffusionNode() {

	}


	protected void addNeighbour(DiffusionAddress a)
	{
		if(isLeaf())
		{
			nbours.add(a.id);
		}
		else
		{
			for (AbstractDiffusionNode n : getSubnodes()) {
				if(a.doIContainASideOfYou(n.address) || n.address.doIContainASideOfYou(a))
					n.addNeighbour(a);
			}
		}
	}

	protected void removeNeighbour(DiffusionAddress a)
	{
		if(isLeaf())
		{
			nbours.remove(a.id);
		}
		else
		{
			for (AbstractDiffusionNode n : getSubnodes()) {
				n.removeNeighbour(a);
			}
		}
	}

	public DiffusionAddress getID() {
		return getAddress();
	}



	public AbstractDiffusionNode(double[] upperLeftCornerFront,double [] lowerRightCornerBack)
	{
		address = new DiffusionAddress(Hosts.getLocalHost(),upperLeftCornerFront,lowerRightCornerBack);
	}

	public AbstractDiffusionNode(double[] upperLeftCornerFront,double [] lowerRightCornerBack,AbstractDiffusionNode i,int childnbr)
	{

		address = new DiffusionAddress(Hosts.getLocalHost(),i.getAddress().id,childnbr,upperLeftCornerFront,lowerRightCornerBack);
	}



	public ArrayList<AbstractDiffusionNode> introduceLayer()
	{
		OutD.println("   => "+toString(),"green");
		if(snodes.size()>0)
		{
			return getSubnodes();
		}
		this.snodes.clear();
		ArrayList<AbstractDiffusionNode> newnodes = new ArrayList<AbstractDiffusionNode>(8);
		double [] cubediv2 = Matrix.scalarMult(0.5, Matrix.subtract(address.getUpperLeftCornerFront(), address.getLowerRightCornerBack()));
		double [][] uls = new double [8][3];
		double [][] lbs = new double [8][3];
		for (int i=0;i<8;i++) {
			uls[i] = address.getUpperLeftCornerFront().clone();
		}

		uls[2][0]-=cubediv2[0]; 

		uls[0][1]-=cubediv2[1]; 

		uls[1][0]-=cubediv2[0];
		uls[1][1]-=cubediv2[1];


		uls[4][2]-=cubediv2[2];

		uls[7][0]-=cubediv2[0];
		uls[7][2]-=cubediv2[2];


		uls[5][1]-=cubediv2[1];
		uls[5][2]-=cubediv2[2];


		uls[6][0]-=cubediv2[0];
		uls[6][1]-=cubediv2[1];
		uls[6][2]-=cubediv2[2];
		for (int i=0;i<8;i++) {
			lbs[i] = Matrix.subtract(uls[i], cubediv2);
		}


		for(int i =0; i<8;i++)
		{
			AbstractDiffusionNode n = getNewInstance(uls[i],lbs[i],this,i);
			DiffusionNodeManager.I().addDiffusionNode(n);
			newnodes.add(n);
		//	DiffusionAddress k =  DiffReg.I().get( n.getAddress().id);
//			OutD.println(k.id,"blue");
			snodes.add(n.address.id);
			OutD.println("   => "+n.toString(),"blue");
		}


		distributeSuntances(newnodes);
		UpdateNeighbours();
		return newnodes;
	}

	protected abstract AbstractDiffusionNode getNewInstance(double[] upperLeftCornerFront,double [] lowerRightCornerBack,AbstractDiffusionNode i,int childnbr);
	protected abstract void distributeSuntances(ArrayList<AbstractDiffusionNode> newnodes);


	public AbstractDiffusionNode getSubnode(int i)
	{
		return DiffusionNodeManager.I().getDiffusionNode(DiffReg.I().get(snodes.get(i)));
	}

	private void UpdateNeighbours() {

		for(DiffusionAddress i: getSnodes())
		{
			for(DiffusionAddress j: getSnodes())
			{
				if(i.equals(j)) continue;

				if(i.doIContainASideOfYou(j))
				{
					AbstractDiffusionNode d= DiffusionNodeManager.I().getDiffusionNode(i);
					d.addNeighbour(j);
				}

			}
		}
		expandNbours();
		for(DiffusionAddress d: getNbours())
		{
			if(d.isLocal())
			{
				AbstractDiffusionNode d_r= DiffusionNodeManager.I().getDiffusionNode(d);
				d_r.removeNeighbour(address);
				for(DiffusionAddress i: getSnodes())
				{
					if(i.doIContainASideOfYou(d) || d.doIContainASideOfYou(i))
					{

						AbstractDiffusionNode i_r= DiffusionNodeManager.I().getDiffusionNode(i);
						d_r.addNeighbour(i);
						i_r.addNeighbour(d);
					}

				}
			}
			else
			{
				for(DiffusionAddress i: getSnodes())
				{
					if(i.doIContainASideOfYou(d) || d.doIContainASideOfYou(i))
					{

						AbstractDiffusionNode i_r= DiffusionNodeManager.I().getDiffusionNode(i);
						i_r.addNeighbour(d);
					}

				}
			}
		}
		nbours=null;
	}


	public void diffuse()
	{
		diffusionAlgorithm();
	}

	protected abstract void diffusionAlgorithm();

	public double getConcentration(String substanceid,double [] pos)
	{
		if(snodes.size()==0) return getInternalConcentreation(substanceid,pos);
		for (DiffusionAddress a : getSnodes()) {
			AbstractDiffusionNode d =  DiffusionNodeManager.I().getDiffusionNode(a);
			if(d.address.containsCoordinates(pos)) 
				return d.getConcentration(substanceid, pos);
		}
		throw new RuntimeException("this is not a position in the diffusion space");
	}

	public void print() {
		print("");
	}

	protected void print(String ident)
	{
		OutD.println(ident+" Node : "+this.address.id);
		if(this.nbours!=null)
		{
			String temp = ident+"   neighbours:";

			for (DiffusionAddress n : this.getNbours()) {
				temp+=n.id+" ";
			}
			OutD.println(temp);
		}
		if(snodes.size()>0)
		{
			for	(DiffusionAddress a : getSnodes()) {
				if(a.isLocal())
				{
					AbstractDiffusionNode node = DiffusionNodeManager.I().getDiffusionNode(a);
					node.print(ident+"   ");
				}
			}
		}
	}



	public void changeSubstanceQuantity(String substanceid,double deltaD,double [] pos)
	{
		if(isLeaf())
		{
			changeSubstanceQuantityIternaly(substanceid,deltaD,pos);
			return;
		}
		for (AbstractDiffusionNode d : DiffusionNodeManager.I().getDiffusionNodes(snodes)) {
			if(d.address.containsCoordinates(pos))
			{
				d.changeSubstanceQuantity(substanceid, deltaD,pos);
				return;
			}
		}
	}

	public AbstractDiffusionNode getCoordinatesDiffusionNode(double [] pos)
	{
		if(isLeaf())
		{
			return this;
		}
		for (AbstractDiffusionNode d : DiffusionNodeManager.I().getDiffusionNodes(snodes)) {
			if(d.address.containsCoordinates(pos))
			{
				return d.getCoordinatesDiffusionNode(pos);

			}
		}
		return null;
	}


	protected abstract double getInternalConcentreation(String substanceid, double[] pos);
	protected abstract void changeSubstanceQuantityIternaly(String id,double deltaD,double [] pos);
	public abstract double getInternalConcentration(String substanceID);
	public abstract Substance getSubstance(String id2);
	protected abstract void degradate(double currentEcmTime);



	public void introduceLayersTillResolution(double d)
	{
		if(this.address.getSize()>d)
		{
			for (AbstractDiffusionNode n : introduceLayer()) {
				n.introduceLayersTillResolution(d);
			}
		}
	}

	public void introduceLayersToDepth(int i )
	{
		i--;
		if(i>=0)
		{
			if(isLeaf())
			{
				introduceLayer();	
			}
			for(AbstractDiffusionNode n: DiffusionNodeManager.I().getDiffusionNodes(snodes))
			{
				n.introduceLayersToDepth(i);
			}
		}
	}

	public boolean isLeaf()
	{
		if(snodes ==null) return true;
		return snodes.size()==0;
	}

	public ArrayList<AbstractDiffusionNode> getLocalSubnodes()
	{
		return DiffusionNodeManager.I().getLocalDiffusionNodes(snodes);
	}

	public ArrayList<AbstractDiffusionNode> getSubnodes()
	{
		return DiffusionNodeManager.I().getDiffusionNodes(snodes);
	}

	public void getAllAddresses(ArrayList<DiffusionAddress> addresses)
	{
		addresses.add(new DiffusionAddress(this.address));
		if(snodes!=null)
		{
			for (DiffusionAddress b : getSnodes()) {
				if(b.isLocal())
				{
					AbstractDiffusionNode node = DiffusionNodeManager.I().getDiffusionNode(b);
					node.getAllAddresses( addresses);
				}
			}
		}
	}

	public DiffusionAddress getAddress() {

		return address;
	}

	public boolean contains(double[] position) {
		return address.containsCoordinates(position);
	}

	public ArrayList<AbstractDiffusionNode> getNeighbours() {
		if(nbours!=null)
		{
			return DiffusionNodeManager.I().getDiffusionNodes(new ArrayList<Long>(nbours));
		}
		else
		{
			return new ArrayList<AbstractDiffusionNode>();
		}

	}

	public ArrayList<AbstractDiffusionNode> getAllSubnodes()
	{
		ArrayList<AbstractDiffusionNode> temp = new ArrayList<AbstractDiffusionNode>();
		temp.add(this);
		for (AbstractDiffusionNode abstractDiffusionNode : this.getSubnodes()) {
			temp.addAll(abstractDiffusionNode.getAllSubnodes());
		}
		return temp;
	}


	public abstract HashT<String, Double> getAllQuantaties();

	public abstract HashT<String, Substance> getSubstances(); 

	public abstract void applyCalculations();

	protected abstract double [] getInternalGradient(String id,double [] pos);

	public double [] getGradient(String substanceid,double [] pos)
	{
		if(isLeaf())
		{
			return getInternalGradient(substanceid,pos);
		}
		for (AbstractDiffusionNode d : DiffusionNodeManager.I().getDiffusionNodes(snodes)) {
			if(d.address.containsCoordinates(pos))
			{
				return d.getGradient(substanceid, pos);

			}
		}
		return new double[]{0,0,0};
	}

	@Override
	public double getVolume() {
		// TODO Auto-generated method stub
		return address.getVolume();
	}

	@Override
	public void deserialize(DataInputStream is) throws IOException {

		this.address = new DiffusionAddress();
		this.address.deserialize(is);
		snodes = null;
		int to = is.readInt();
		nbours= new ArrayList<Long>();
		for (int i=0;i<to;i++) {
			DiffusionAddress d = new DiffusionAddress();
			d.deserialize(is);
			DiffReg.I().put(d);
			nbours.add(d.id);
		}
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		address.serialize(os);
		if(nbours!=null)
		{
			os.writeInt(nbours.size());
		}
		else
		{
			os.writeInt(0);
		}
		if(nbours!=null)
		{
			for (DiffusionAddress d : getNbours()) {
				d.serialize(os);
			}
		}

	}


	public ArrayList<DiffusionAddress> getNeighboursAddresses() {
		if(nbours==null) return new ArrayList<DiffusionAddress>();
		return getNbours();
	}



	protected ArrayList<DiffusionAddress> getNbours() {
		if(nbours==null) return null;
		return DiffReg.I().get(nbours);
	}



	protected ArrayList<DiffusionAddress> getSnodes() {
		return DiffReg.I().get(snodes);
	}

	public void checkNeighbours()
	{
		if(!this.isLeaf()) return; 
		ArrayList<Long> newneighbours = new ArrayList<Long>();
		for (long l : this.nbours) {

			DiffusionAddress s = DiffReg.I().get(l);
			if(s!=null)
			{
				if(DiffusionNodeManager.I().containsLocaly(s))
				{
					newneighbours.add(l);
					continue;
				}
			//	ShowConsoleOutput.println("not local contained but remote?");
				if(DiffusionNodeManager.I().contains(s))
				{
					newneighbours.add(l);
					continue;
				}
			}

			OutD.println("not remote nor local so find it."+s.getOctId());

			for(DiffusionAddress d:DiffReg.I().getMyChildern(l))
			{
				if(this.address.doIContainASideOfYou(d) || d.doIContainASideOfYou(this.address))
				{
					DiffusionNodeManager.I().getDiffusionNode(d); //checkAvilability!!!
					newneighbours.add(d.id);
				}
			}


		}
		this.nbours = newneighbours;
	}


	public void checkDiffusionHirarchie() {
		if(snodes ==null) return;
		for (DiffusionAddress a : getSnodes()) {
			DiffusionNodeManager.I().getDiffusionNode(a).checkDiffusionHirarchie();
		}
	}




	public void expandNbours() {
		boolean finished=false;
		while(!finished)
		{
			finished = true;
			for(DiffusionAddress n: getNbours())
			{
				if(!n.isLocal()) continue;
				AbstractDiffusionNode d_r= DiffusionNodeManager.I().getDiffusionNode(n);
				if(!d_r.isLeaf())
				{
					this.nbours.remove(n.id);
					for(DiffusionAddress da :d_r.getSnodes())
					{
						if(da.doIContainASideOfYou(this.address))
						{
							this.nbours.add(da.id);
						}
					}
					finished= false;
				}
			}
		}
	}


	public void setResistance(double resistance) {
		this.resistance = resistance;
	}


	public double getResistance() {
		return resistance;
	}


	public abstract void setConcentration(String string, double d);
}

