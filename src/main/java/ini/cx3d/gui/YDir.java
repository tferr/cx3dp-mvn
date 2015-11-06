package ini.cx3d.gui;

import java.awt.Color;
import java.awt.Graphics;

public class YDir extends Drawer{


	public YDir()
	{
		super();
	 	this.name = "Y dir";
	}

	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(c.red);
		drawLine(new Vector3d(0, 0, 0), new Vector3d(0, 100, 0));
		g.setColor(c);

	}
}
