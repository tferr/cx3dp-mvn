package ini.cx3d.gui.physics;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.Cuboid;
import ini.cx3d.utilities.Line;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class BoundariesDrawer extends Drawer{


	public BoundariesDrawer()
	{
		super();
		this.name = "Boundaries";
	}
	@Override
	public void draw(Graphics g) {
		g.setColor(Color.black);
		if(ECM.getInstance().getArtificialWallForSpheres()|| ECM.getInstance().getArtificialWallForCylinders())
		{
			Cuboid c = new Cuboid(ECM.getInstance().getArtificialWalllMin(),ECM.getInstance().getArtificialWalllMax());
			ArrayList<Line> temp = c.getLines();
			for(Line l:temp)
			{
				drawLine(new  Vector3d(l.getA()), new  Vector3d(l.getB()));
			}
		}

	}

}
