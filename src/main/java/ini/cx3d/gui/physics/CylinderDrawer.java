package ini.cx3d.gui.physics;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.simulation.ECM;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;

public class CylinderDrawer extends Drawer{



	public CylinderDrawer()
	{
		super();
		this.name = "Physical Cylinders";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		//drawLine(new Vector3d(new double[]{100,100,100}),new Vector3d(new double[]{-100,-100,-100}),10);
		try{
			Collection<PhysicalCylinder> temp = ECM.getInstance().getPhysicalCylinderList();
			for(PhysicalCylinder s:temp)
			{
				g.setColor(s.getColor());
				try{
					drawLine(new Vector3d(s.proximalEnd()),new Vector3d(s.distalEnd()),s.getDiameter());
				}
				catch (Exception e) {
					// TODO: handle exception
				}

			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		g.setColor(c);

	}
}
