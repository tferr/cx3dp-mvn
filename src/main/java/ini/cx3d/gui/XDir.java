package ini.cx3d.gui;

import java.awt.Color;
import java.awt.Graphics;

public class XDir extends Drawer{


	public XDir()
	{
		super();
	 	this.name = "X dir";
	}

	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(c.black);
		drawLine(new Vector3d(0, 0, 0), new Vector3d(100, 0, 0));
		g.setColor(c);

	}

}
