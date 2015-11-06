package ini.cx3d.gui.physics.diffusion;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.utilities.Rectangle;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class OctreeSequenceDrawer extends Drawer{


	public OctreeSequenceDrawer()
	{
		super();
	this.name = "octree sequence drawer";
	}
	@Override
	public void draw(Graphics g) {
		g.setColor(Color.orange);
		for (AbstractDiffusionNode n : DiffusionNodeManager.I().getLocalDiffusionNodes()) {
			Drawsingle(g, n);
		}
	}
	
	private void Drawsingle(Graphics g,AbstractDiffusionNode d2) {
			for (Rectangle r : d2.getAddress().getSides()) {
				ArrayList<Vector3d> temp = r.edges();
				drawLine(temp.get(0), temp.get(1));
				drawLine(temp.get(0), temp.get(2));
				drawLine(temp.get(3), temp.get(1));
				drawLine(temp.get(3), temp.get(2));
			}
//			drawString(new Vector3d(d2.getMiddle()),d2.address.id+""); 
	}
}
