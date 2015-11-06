package ini.cx3d.gui;

import ini.cx3d.utilities.Matrix;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class sphere extends Drawer{


	public sphere()
	{
		super();
		this.name = "X dir";
	}

	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(c.black);
		double r = 2;
		ArrayList<Vector3d> temp = SpherePointsWithBorder(200,10,r);
		for (Vector3d i :temp) {
			drawCyrcle(i, r);
			
		}
		g.setColor(c.red);
		for (Vector3d i1 :temp) {
			for (Vector3d i :getNeighbour(temp,i1, 4)) {
				drawLine(i1, i);
				
			}
		}
		
		g.setColor(c);

	}

	public ArrayList<Vector3d> SpherePoints(int n)
	{
		int i;
		double x, y, z, w, t;
		double r= 30;
		ArrayList<Vector3d> temp = new ArrayList<Vector3d>();
		for( i=0; i< n; i++ ) {
			z = r*(2.0 * Matrix.getRandomDouble() - 1.0);
			t =  2.0 * Math.PI * Matrix.getRandomDouble();
			w =  Math.sqrt( r - z*z );
			x = w * Math.cos( t );
			y = w * Math.sin( t );
			temp.add(new Vector3d(x,y,z));
		}
		return temp;
	}
	
	
	public ArrayList<Vector3d> SpherePointsWithBorder(int n,double r,double r2)
	{
		int i;
		double x, y, z, w, t;
		ArrayList<Vector3d> temp = new ArrayList<Vector3d>();
		
		while(temp.size()<n) {
			z = r*(2.0 * Matrix.getRandomDouble() - 1.0);
			t =  2.0 * Math.PI * Matrix.getRandomDouble();
			w =  Math.sqrt( r*r - z*z );
			x = w * Math.cos( t );
			y = w * Math.sin( t );
			Vector3d newV = new Vector3d(x,y,z);
			boolean toadd = true;
			for (Vector3d v : temp) {
				if(v.disance(newV)<r2)
				{
					toadd = false;
				}
			}
			if(toadd)
			{
				temp.add(newV);
			}
		}
		return temp;
	}

	public ArrayList<Vector3d> getNeighbour(ArrayList<Vector3d> all,Vector3d middle,double r)
	{
		ArrayList<Vector3d> temp = new ArrayList<Vector3d>();
		for(Vector3d v: all)
		{
			if(v.disance(middle)<r)
			{
				temp.add(v);
			}
		}
		return temp;
	}

}
