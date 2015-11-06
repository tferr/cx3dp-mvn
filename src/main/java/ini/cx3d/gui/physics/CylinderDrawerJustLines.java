package ini.cx3d.gui.physics;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.simulation.ECM;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;

public class CylinderDrawerJustLines extends Drawer{



	public CylinderDrawerJustLines()
	{
		super();
		this.name = "Physical Cylinders just Lines";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.green);
		//drawLine(new Vector3d(new double[]{100,100,100}),new Vector3d(new double[]{-100,-100,-100}),10);
		try{
			Collection<PhysicalCylinder> temp = ECM.getInstance().getPhysicalCylinderList();
			for(PhysicalCylinder s:temp)
			{
//				PhysicalCylinder s = c;
				g.setColor(s.getColor());
				drawLine(new Vector3d(s.proximalEnd()),new Vector3d(s.distalEnd()));
			}
		}
		catch (Exception e) {
			OutD.println(e);
		}
		g.setColor(c);

	}
}
