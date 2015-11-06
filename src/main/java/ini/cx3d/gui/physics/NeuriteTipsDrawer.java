package ini.cx3d.gui.physics;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.simulation.ECM;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;

public class NeuriteTipsDrawer extends Drawer{



	public NeuriteTipsDrawer()
	{
		super();
		this.name = "drawTips";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.red);
		//drawLine(new Vector3d(new double[]{100,100,100}),new Vector3d(new double[]{-100,-100,-100}),10);
		try{
			Collection<PhysicalCylinder> temp = ECM.getInstance().getPhysicalCylinderList();
			for(PhysicalCylinder s:temp)
			{
				if(s.getDaughterLeft()==null)
				{
					drawOval(new Vector3d(s.distalEnd()).minus(new Vector3d(3,3,3)),new Vector3d(s.distalEnd()).plus(new Vector3d(3,3,3)));
				}
				
			}
		}
		catch (Exception e) {
			OutD.println(e);
		}
		g.setColor(c);

	}
}
