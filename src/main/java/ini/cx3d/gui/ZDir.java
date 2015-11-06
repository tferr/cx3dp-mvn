package ini.cx3d.gui;

import java.awt.Color;
import java.awt.Graphics;

public class ZDir extends Drawer{


	public ZDir()
	{
		super();
	 	this.name = "Z dir";
	}

	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(c.BLUE);
		drawLine(new Vector3d(0, 0, 0), new Vector3d(0, 0,100));
		g.setColor(c);

	}
}
