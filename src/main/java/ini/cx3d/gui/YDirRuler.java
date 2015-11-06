package ini.cx3d.gui;

import java.awt.Color;
import java.awt.Graphics;

public class YDirRuler extends Drawer{


	public YDirRuler()
	{
		super();
	 	this.name = "Y Ruler";
	}

	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(c.black);
		int length = 200;
		for(int i=0;i<length;i+=length/20)
		{
			drawLine(new Vector3d(0, i, 0), new Vector3d(10, i, 0));
			drawString(new Vector3d(12, i-4, 0), i+" um");
		}
		drawLine(new Vector3d(0, 0, 0), new Vector3d(0, length, 0));
		
		g.setColor(c);

	}

}
