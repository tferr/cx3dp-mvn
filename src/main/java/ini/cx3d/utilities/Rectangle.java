package ini.cx3d.utilities;

import ini.cx3d.gui.Vector3d;

import java.util.ArrayList;

public class Rectangle
{
	private double [] b;
	private double [] a;
	int projectionDimension;
	public Rectangle(double [] a, double [] b,int projectionDimension)
	{
		this.a = a;
		this.b = b;
		this.projectionDimension = projectionDimension;
	}
	
	public boolean contains(Rectangle rect)
	{
		if(rect.projectionDimension != projectionDimension) return false; //not the same orientation
		if(a[projectionDimension]!=rect.a[projectionDimension]) return false; //not in the same plane
		
		int x = 0;
		int y = 0;
		
		switch(projectionDimension)
		{
			case 0:
				x = 1;
				y = 2;
			break;
			case 1:
				x = 0;
				y = 2;
			break;
			case 2:
				x = 0;
				y = 1;
			break;
		}
		
		
		boolean contained = true; 
		contained &= isbetween(a[x],b[x],rect.a[x]);
		contained &= isbetween(a[x],b[x],rect.b[x]);
		contained &= isbetween(a[y],b[y],rect.a[y]);
		contained &= isbetween(a[y],b[y],rect.b[y]);
		return contained;
	}
	
	
	private boolean isbetween(double a, double b, double c) {
		boolean res = true;
		a = Math.max(a, b);
		b = Math.min(a, b);
		res &= a>=c;
		res &= c>=b;
		return res;
	}

	
	public double getCrossSectionSize()
	{
		double [] temp = Matrix.subtract(a, b);
		double t= 1;
		for(int i=0;i<temp.length;i++)
		{
			if(i!=projectionDimension)
				t*=temp[i];
		}
		return t;
	}

	public ArrayList<Vector3d> edges()
	{
		double [] x,y,z,k;
		if(projectionDimension == 2)
		{
			x = a.clone();
			y = a.clone();
			y[0] = b[0];
			
			z = a.clone();
			z[1] = b[1];
			k = b.clone();
		}
		else if(projectionDimension == 1)
		{
			x = a.clone();
			y = a.clone();
			y[0] = b[0];
			
			z = a.clone();
			z[2] = b[2];
			k = b.clone();
		}
		else
		{
			
				x = a.clone();
				y = a.clone();
				y[1] = b[1];
				
				z = a.clone();
				z[2] = b[2];
				k = b.clone();
		}
		
		
		ArrayList<Vector3d> temp = new ArrayList<Vector3d>();
		temp.add(new Vector3d(x));
		temp.add(new Vector3d(y));
		temp.add(new Vector3d(z));
		temp.add(new Vector3d(k));
		return temp;
	}
	
	public int getProjectionDimension()
	{
		return projectionDimension;
	}
	
	public double [] getCenter()
	{
		return Matrix.scalarMult(0.5,Matrix.add(a, b));
	}

	public void setA(double [] a) {
		this.a = a;
	}

	public double [] getA() {
		return a;
	}

	public void setB(double [] b) {
		this.b = b;
	}

	public double [] getB() {
		return b;
	}

}

