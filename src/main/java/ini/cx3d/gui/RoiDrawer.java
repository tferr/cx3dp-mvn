package ini.cx3d.gui;

import ini.cx3d.utilities.Cuboid;
import ini.cx3d.utilities.Line;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class RoiDrawer extends Drawer{


	public RoiDrawer()
	{
		super();
		this.name = "Roid Drawer";
	}
	@Override
	public void draw(Graphics g) {
		g.setColor(Color.GREEN);
	
		Cuboid c = MonitoringGui.getCurrent().component.getRoi();
		
		ArrayList<Line> temp = c.getLines();
		for(Line l:temp)
		{
			drawLine(new  Vector3d(l.getA()), new  Vector3d(l.getB()));
		}
		

	}

}
