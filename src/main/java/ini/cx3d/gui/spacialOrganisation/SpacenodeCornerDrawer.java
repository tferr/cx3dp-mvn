package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.spacialOrganisation.Iiterator;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.PartitionManager;

import java.awt.Color;
import java.awt.Graphics;



public class SpacenodeCornerDrawer extends Drawer{


	public SpacenodeCornerDrawer()
	{
		super();
		this.name = "Spacenode Corners";
	}
	
	@Override
	public void draw(Graphics g) {
		try{
			Color c = g.getColor();
			g.setColor(Color.black);
			for (PartitionManager p : ManagerResolver.I().getLocalPartitions()) {
				Iiterator iter = p.getIterator();
				iter.next();
				while(!iter.isAtEnd())
				{
					for (double[] i: getCorners(iter.getCurrent(), 10)) {
						drawCyrcle(new Vector3d(i), 1);
					}
					iter.next();
				}
			}
			g.setColor(c);
		}
		catch(Exception e){
			OutD.println("an error has occured during draw");
		}
		
		
	}
	
	public double [][] getCorners(ObjectReference middle,double radius)
	{

		double [][] corners= new double [8][];
		double [] corner000;
		double [] corner001;
		double [] corner010;
		double [] corner011;
		double [] corner100;
		double [] corner101;
		double [] corner110;
		double [] corner111;
		
		double [] coordinats = middle.getPosition();
		corner000= coordinats.clone();
		corner000[0] -=radius;corner000[1] -=radius;corner000[2] -=radius;
		corners[0] = corner000;
		
		corner001= coordinats.clone();
		corner001[0] -=radius;corner001[1] -=radius;corner001[2] +=radius;
		corners[1] = corner001;
		
		corner010= coordinats.clone();
		corner010[0] -=radius;corner010[1] +=radius;corner010[2] -=radius;
		corners[2] = corner010;
		
		corner011= coordinats.clone();
		corner011[0] -=radius;corner011[1] +=radius;corner011[2] +=radius;
		corners[3] = corner011;
		
		corner100= coordinats.clone();
		corner100[0] +=radius;corner100[1] -=radius;corner100[2] -=radius;
		corners[4] = corner100;
		
		corner101= coordinats.clone();
		corner101[0] +=radius;corner101[1] -=radius;corner101[2] +=radius;
		corners[5] = corner101;
		
		corner110= coordinats.clone();
		corner110[0] +=radius;corner110[1] +=radius;corner110[2] -=radius;
		corners[6] = corner110;
		
		corner111= coordinats.clone();
		corner111[0] +=radius;corner111[1] +=radius;corner111[2] +=radius;
		corners[7] = corner111;
		return corners;
	}
}
