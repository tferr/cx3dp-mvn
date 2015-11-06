package ini.cx3d.gui;
import ini.cx3d.utilities.Matrix;

import java.io.Serializable;
import java.util.ArrayList;


public class Vector3d implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4403248450307788344L;
	private double x;
	private double y;
	private double z;
	
	public void setZ(double z) {
		this.z = z;
	}

	public double getZ() {
		return z;
	}

	public Vector3d(double x, double y, double z)
	{
		this.setX(x);
		this.setY(y);
		this.setZ(z);
	}
	
	public Vector3d(double [] v)
	{
		this.setX(v[0]);
		this.setY(v[1]);
		this.setZ(v[2]);
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getX() {
		return x;
	}
	
	public Vector3d scalarMult(double s)
	{
		Vector3d temp = new Vector3d(x,y,z);
		temp.x*=s;
		temp.y*=s;
		temp.z*=s;
		return temp;
	}
	
	public Vector3d minus(Vector3d a)
	{
		Vector3d temp = new Vector3d(x,y,z);
		temp.x-=a.x;
		temp.y-=a.y;
		temp.z-=a.z;
		return temp;
	}
	
	public Vector3d plus(Vector3d a)
	{
		Vector3d temp = new Vector3d(x,y,z);
		temp.x+=a.x;
		temp.y+=a.y;
		temp.z+=a.z;
		return temp;
	}
	
	public double length()
	{
		return Math.sqrt(x*x+y*y+z*z);
	}
	
	public Vector3d Normalize()
	{
		Vector3d temp = new Vector3d(x,y,z);
		double length = temp.length();
		temp.x/=length;
		temp.y/=length;
		temp.z/=length;
		return temp;
	}
	
	public double [] toArray()
	{
		return new double[] {x,y,z};
	}

	public double [] toPolar()
	{
		double length =Math.sqrt(x*x + y*y + z*z); 
		double theta=Math.atan2(Math.sqrt(x*x + y*y) , z);
		double  phi =  Math.atan2(y,x);
		return new double [] {length,theta,phi};
	}
	
	public String toString()
	{
		return "("+x+ ","+y+","+z+")";
	}

	public Vector3d mult(double d) {
		Vector3d temp = new Vector3d(x,y,z);
		temp.x*=d;
		temp.y*=d;
		temp.z*=d;
		return temp;
	}

	public double disance(Vector3d a) {
		double x = this.x-a.x;
		double y = this.y-a.y;
		double z = this.z-a.z;
		return Math.sqrt(x*x+y*y+z*z);
	}
	

	public static Vector3d sum(ArrayList<Vector3d> c)
	{
		Vector3d d = new Vector3d(0,0,0); 
		for(Vector3d v:c)
		{
			d.x+=v.x;
			d.y+=v.y;
			d.z+=v.z;
		}
		return d;
		
	}
	
	public static Vector3d average(ArrayList<Vector3d> c)
	{
		Vector3d d = sum(c);
		d  = d.mult(1.0/c.size());
		return d;
	}

	public void setVect(Vector3d vect) {
		this.x = vect.x;
		this.y = vect.y;
		this.z = vect.z;
	}
	
	public Vector3d applyTransform(double [][] at)
	{
		return new Vector3d(Matrix.mult(at, toArray()));
	}
	
	public  double getAngleRadiant(Vector3d branch)
	{
		double candidate1 =  Matrix.angleRadian(this.toArray(), branch.toArray());
		//ShowConsoleOutput.println("angle between "+candidate1);
		return candidate1;
	}
	
}
