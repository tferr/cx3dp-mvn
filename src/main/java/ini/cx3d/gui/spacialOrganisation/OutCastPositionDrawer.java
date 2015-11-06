package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;

import java.awt.Color;
import java.awt.Graphics;

public class OutCastPositionDrawer extends Drawer{

	public static double [] outcast =null;
	public OutCastPositionDrawer()
	{
		super();
		this.name = "Outcast Position";
	}
	
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		
		g.setColor(Color.green);
		if(outcast!=null)
		{
			drawCyrcle(new Vector3d(outcast), 3);
		}
		g.setColor(c);
	}
	
}
