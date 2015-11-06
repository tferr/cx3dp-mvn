package ini.cx3d.utilities;


import ini.cx3d.utilities.serialisation.CustomSerializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Cuboid implements Serializable, CustomSerializable{

	private double [] upperLeftCornerFront;
	private double [] lowerRightCornerBack;
	private double [] unit2;

	public Cuboid(double [] corner1,double [] corner2)
	{
		setCorners(corner1, corner2);
	}

	public void setCorners(double [] corner1,double [] corner2)
	{
		this.upperLeftCornerFront= getMax(corner1,corner2);
		this.lowerRightCornerBack = getMin(corner1,corner2);
		unit2= Matrix.scalarMult(0.5, Matrix.subtract(getUpperLeftCornerFront(), getLowerRightCornerBack()));
	}

	public Cuboid()
	{}

	protected double[] getMin(double[] a, double[] b) {
		double [] temp = new double[3]; 
		temp[0]  = Math.min(a[0], b[0]);
		temp[1]  = Math.min(a[1], b[1]);
		temp[2]  = Math.min(a[2], b[2]);
		return temp;
	}

	
	

	protected double[] getMax(double[] a, double[] b) {
		double [] temp = new double[3]; 
		temp[0]  = Math.max(a[0], b[0]);
		temp[1]  = Math.max(a[1], b[1]);
		temp[2]  = Math.max(a[2], b[2]);
		return temp;
	}


	public boolean containsCoordinates( double[] corrds)
	{
		boolean temp = true;
		temp &= getUpperLeftCornerFront()[0] >= corrds[0] && corrds[0]  > getLowerRightCornerBack()[0];
		temp &= getUpperLeftCornerFront()[1] >= corrds[1] && corrds[1]  > getLowerRightCornerBack()[1];
		temp &= getUpperLeftCornerFront()[2] >= corrds[2] && corrds[2]  > getLowerRightCornerBack()[2];
		return temp;
	}


	private boolean containsCoordinatesInternal(double[] corrds)
	{
		boolean temp = true;
		temp &= getUpperLeftCornerFront()[0] >= corrds[0] && corrds[0]  >= getLowerRightCornerBack()[0];
		temp &= getUpperLeftCornerFront()[1] >= corrds[1] && corrds[1]  >= getLowerRightCornerBack()[1];
		temp &= getUpperLeftCornerFront()[2] >= corrds[2] && corrds[2]  >= getLowerRightCornerBack()[2];
		return temp;
	}

	public boolean containsCuboild(Cuboid c)
	{
		if(     getUpperLeftCornerFront()[0] == c.getUpperLeftCornerFront()[0] &&
				getUpperLeftCornerFront()[1] == c.getUpperLeftCornerFront()[1] &&                                       
				getUpperLeftCornerFront()[2] == c.getUpperLeftCornerFront()[2] &&
				getLowerRightCornerBack()[0] == c.getLowerRightCornerBack()[0] &&
				getLowerRightCornerBack()[1] == c.getLowerRightCornerBack()[1] &&                                       
				getLowerRightCornerBack()[2] == c.getLowerRightCornerBack()[2]		                                                              

		)
		{
			return false;
		}
		ArrayList<double[]> o = c.getCorners();
		for (double[] ds : o) {
			if(!containsCoordinatesInternal(ds)) return false;
		}
		return true;
	}

	public boolean areWePointNeighbours(Cuboid c)
	{
		if(doIContainASideOfYou(c)) return true;
		if(doWeShareCorners(c)) return true;
		return false;
	}

	public boolean doIContainASideOfYou(Cuboid c)
	{
		for(Rectangle rect:getSides())
		{
			for(Rectangle r: c.getSides())
			{
				if(rect.contains(r)) return true;
			}
		}
		return false;
	}


	public boolean doIContainALineOfYou(Cuboid c)
	{
		for(Line l1:getLines())
		{
			for(Line l2: c.getLines())
			{
				if(l1.contains(l2)) return true;
			}
		}
		return false;
	}

	public Rectangle getCommonSide(Cuboid c)
	{
		for(Rectangle rect:getSides())
		{
			for(Rectangle r: c.getSides())
			{
				if(rect.contains(r)) return r;
			}
		}
		for(Rectangle rect:c.getSides())
		{
			for(Rectangle r: getSides())
			{
				if(rect.contains(r)) return r;
			}
		}

		return null;
	}

	public boolean doWeShareCorners(Cuboid c)
	{
		for (double []  a : getCorners()) {
			for(double []  b : c.getCorners())
			{
				if(Matrix.distance(a, b)==0) return true;
			}
		}
		return false;
	}


	public ArrayList<double[]> getCorners() {
		ArrayList<double[]> temp = new ArrayList<double[]>();
		double [] a= upperLeftCornerFront;
		double [] b = lowerRightCornerBack;
		double [] t;
		t = new double []{a[0],a[1],a[2]}; //1
		temp.add(t);
		t = new double []{a[0],a[1],b[2]}; //2
		temp.add(t);
		t = new double []{a[0],b[1],a[2]}; //3
		temp.add(t);
		t = new double []{a[0],b[1],b[2]}; //4
		temp.add(t);
		t = new double []{b[0],a[1],a[2]}; //5
		temp.add(t);
		t = new double []{b[0],a[1],b[2]}; //6
		temp.add(t);
		t = new double []{b[0],b[1],a[2]}; //7
		temp.add(t);
		t = new double []{b[0],b[1],b[2]}; //8
		temp.add(t);

		return temp;
	}


	public ArrayList<Rectangle> getSides()
	{
		ArrayList<Rectangle> r = new ArrayList<Rectangle>();
		r.add(getUpperRectangle());
		r.add(getLowerRectangle());
		r.add(getBackRectangle());
		r.add(getFrontRectangle());
		r.add(getLeftRectangle());
		r.add(getRightRectangle());
		return r;
	}

	private Rectangle getUpperRectangle()
	{
		double[] a = this.getUpperLeftCornerFront().clone();
		double[] b = this.getLowerRightCornerBack().clone();
		b[1] = a[1];
		return new Rectangle(a,b,1);
	}

	private Rectangle getLowerRectangle()
	{
		double[] a = this.getUpperLeftCornerFront().clone();
		double[] b = this.getLowerRightCornerBack().clone();
		a[1] = b[1];
		return new Rectangle(a,b,1);
	}

	private Rectangle getBackRectangle()
	{
		double[] a = this.getUpperLeftCornerFront().clone();
		double[] b = this.getLowerRightCornerBack().clone();
		a[2] = b[2];
		return new Rectangle(a,b,2);
	}

	private Rectangle getFrontRectangle()
	{
		double[] a = this.getUpperLeftCornerFront().clone();
		double[] b = this.getLowerRightCornerBack().clone();
		b[2] = a[2];
		return new Rectangle(a,b,2);
	}


	private Rectangle getLeftRectangle()
	{
		double[] a = this.getUpperLeftCornerFront().clone();
		double[] b = this.getLowerRightCornerBack().clone();
		b[0] = a[0];
		return new Rectangle(a,b,0);
	}

	private Rectangle getRightRectangle()
	{
		double[] a = this.getUpperLeftCornerFront().clone();
		double[] b = this.getLowerRightCornerBack().clone();
		a[0] = b[0];
		return new Rectangle(a,b,0);
	}


	protected void setUpperLeftCornerFront(double [] upperLeftCornerFront) {
		this.upperLeftCornerFront = upperLeftCornerFront;
	}

	protected void setLowerRightCornerBack(double [] lowerRightCornerBack) {
		this.lowerRightCornerBack = lowerRightCornerBack;
	}


	public double [] getUpperLeftCornerFront() {
		return upperLeftCornerFront;
	}

	public double [] getLowerRightCornerBack() {
		return lowerRightCornerBack;
	}

	public double getSize() {

		double [] a = getUpperLeftCornerFront();
		double [] b = getLowerRightCornerBack();
		double temp = Math.min(Math.min(a[0]-b[0],a[1]-b[1]),a[2]-b[2]);
		return temp;
	}

	public double [] getCenter()
	{
		return Matrix.scalarMult(0.5,Matrix.add(getUpperLeftCornerFront(),getLowerRightCornerBack()));
	}

	public double getVolume()
	{
		double [] cube = Matrix.subtract(getUpperLeftCornerFront(), getLowerRightCornerBack());
		return cube[0]*cube[1]*cube[2];
	}

	public ArrayList<Line> getLines()
	{
		ArrayList<double[]> corners = getCorners();
		ArrayList<Line> lines = new ArrayList<Line>();
		lines.add(new Line(corners.get(4), corners.get(0))); //0
		lines.add(new Line(corners.get(0), corners.get(1))); //1
		lines.add(new Line(corners.get(1), corners.get(5))); //2
		lines.add(new Line(corners.get(5), corners.get(4))); //3

		lines.add(new Line(corners.get(6), corners.get(2))); //4
		lines.add(new Line(corners.get(2), corners.get(3))); //5
		lines.add(new Line(corners.get(3), corners.get(7))); //6
		lines.add(new Line(corners.get(7), corners.get(6))); //7

		lines.add(new Line(corners.get(3), corners.get(1))); //8
		lines.add(new Line(corners.get(2), corners.get(0))); //9
		lines.add(new Line(corners.get(6), corners.get(4))); //10
		lines.add(new Line(corners.get(7), corners.get(5))); //11
		return lines;

	}

	public boolean areWeNeighbours(Cuboid c) {
		
		double [] sidethis = Matrix.scalarMult(0.5,getSideLengths());
		double [] sidec = Matrix.scalarMult(0.5,c.getSideLengths());
		
		double [] centerthis = getCenter();
		double [] centerc = c.getCenter();
		
		double [] totalLength = Matrix.add(sidethis,sidec);
		
		double da0 = Math.abs(centerthis[0]-centerc[0]);
		if(da0>totalLength[0]) return false;
		double da1 = Math.abs(centerthis[1]-centerc[1]);
		if(da1>totalLength[1]) return false;
		double da2 = Math.abs(centerthis[2]-centerc[2]);
		if(da2>totalLength[2]) return false;
		return true;
			
		
	}

	public boolean tempareWeNeighbours(Cuboid c) {
		if(this.doIContainASideOfYou(c)) return true;
		if(c.doIContainASideOfYou(this)) return true;
		if(this.doIContainALineOfYou(c)) return true;
		if(c.doIContainALineOfYou(this)) return true;
		if(this.doWeShareCorners(c)) return true;
		return false;
	}

	private boolean doWeShareNBRCorners(Cuboid c, int i) {
		int count = 0;
		for (double []  a : getCorners()) {
			for(double []  b : c.getCorners())
			{
				if(Matrix.distance(a, b)==0) count++;
			}
		}

		if(count==i) return true;
		else return false;
	}

	@Override
	public void deserialize(DataInputStream is) throws IOException {
		this.lowerRightCornerBack = new double[3];
		this.lowerRightCornerBack[0] = is.readDouble();
		this.lowerRightCornerBack[1] = is.readDouble();
		this.lowerRightCornerBack[2] = is.readDouble();

		this.upperLeftCornerFront = new double[3];
		this.upperLeftCornerFront[0] = is.readDouble();
		this.upperLeftCornerFront[1] = is.readDouble();
		this.upperLeftCornerFront[2] = is.readDouble();


	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		os.writeDouble(this.lowerRightCornerBack[0]);
		os.writeDouble(this.lowerRightCornerBack[1]);
		os.writeDouble(this.lowerRightCornerBack[2]);

		os.writeDouble(this.upperLeftCornerFront[0]);
		os.writeDouble(this.upperLeftCornerFront[1]);
		os.writeDouble(this.upperLeftCornerFront[2]);

	}
	
	public boolean intersectsWithSphere(double [] c, double radius)
	{
		
		double [] centerp = getCenter();
		double da0 = Math.max(Math.abs(centerp[0]-c[0])-unit2[0],0);
		if(da0>radius) return false;
		double da1 = Math.max(Math.abs(centerp[1]-c[1])-unit2[1],0);
		if(da1>radius) return false;
		double da2 = Math.max(Math.abs(centerp[2]-c[2])-unit2[2],0);
		if(da2>radius) return false;
		return da0*da0 + da1*da1 + da2*da2<radius*radius;
		
		
		
//		double [] centerp = getCenter();
//		double [] distabs = Matrix.subtract(centerp, c);
//		distabs[0] = Math.abs(distabs[0]);
//		distabs[1] = Math.abs(distabs[1]);
//		distabs[2] = Math.abs(distabs[2]);
//		double [] unit2 = Matrix.scalarMult(0.5, Matrix.subtract(getUpperLeftCornerFront(), getLowerRightCornerBack()));
//		distabs = Matrix.subtract(distabs, unit2);
//		
//		distabs[0] = Math.max(0,distabs[0]);
//		distabs[1] = Math.max(0,distabs[1]);
//		distabs[2] = Math.max(0,distabs[2]);
//		
//		double distsquare  =  Matrix.dot(distabs, distabs);
//		
//		return distsquare<radius*radius;
		
	}
	
	public boolean encloses(double[] c, double radius) {
		
		double [] centerp = getCenter();
		double da0 = Math.abs(centerp[0]-c[0])+radius;
		if(da0>unit2[0]) return false;
		double da1 = Math.abs(centerp[1]-c[1])+radius;
		if(da1>unit2[1]) return false;
		double da2 = Math.abs(centerp[2]-c[2])+radius;
		if(da2>unit2[2]) return false;
		return true;
		
	}
	
	
	public double [] getSideLengths()
	{
		return Matrix.subtract(getUpperLeftCornerFront(),getLowerRightCornerBack());
	}

	public boolean intersects(Cuboid address) {
		 double [] dist = Matrix.subtract(address.getCenter(), this.getCenter());
		 double [] a = Matrix.subtract(address.getCenter(), address.getUpperLeftCornerFront());
		 double [] b = Matrix.subtract(getCenter(), getUpperLeftCornerFront());
		 
		 double  k = Math.abs(dist[0]);
		 double l = Math.abs(a[0])+Math.abs(b[0]);
		 
		 if(k>l)
		 {
			 return false;
		 }
		 k = Math.abs(dist[1]);
		 l = Math.abs(a[1])+Math.abs(b[1]);
		 if(k>l)
		 {
			 return false;
		 }
		 k = Math.abs(dist[2]);
		 l = Math.abs(a[2])+Math.abs(b[2]);
		 if(k>l)
		 {
			 return false;
		 }
		 return true;
		 
	}
	
}
