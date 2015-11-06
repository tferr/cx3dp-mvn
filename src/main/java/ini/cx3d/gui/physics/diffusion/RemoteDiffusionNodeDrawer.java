package ini.cx3d.gui.physics.diffusion;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionAddress;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.utilities.Rectangle;
import ini.cx3d.utilities.SomeColors;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class RemoteDiffusionNodeDrawer extends Drawer{


	public RemoteDiffusionNodeDrawer()
	{
		super();
		SomeColors.initColorField();
		this.name = "Remote Diffusion Nodes";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
	
		for (AbstractDiffusionNode pm : DiffusionNodeManager.I().getRemoteDiffusionNodes()) {
			g.setColor(SomeColors.getColorAssociated(pm.getAddress().getHost().hashCode()));
			drawRemoteManager(g,pm.getAddress());
		}
		g.setColor(c);
	}
	
	private void drawRemoteManager(Graphics g, DiffusionAddress pm) {
		Vector3d rem = new Vector3d(new double[]{-10,-10,-10}); 
		for (Rectangle r : pm.getSides()) {
			ArrayList<Vector3d> temp = r.edges();
			drawLine(temp.get(0), temp.get(1));
			drawLine(temp.get(0), temp.get(2));
			drawLine(temp.get(3), temp.get(1));
			drawLine(temp.get(3), temp.get(2));
		}
		g.setColor(Color.black);
		drawString(new Vector3d(pm.getCenter()),pm.getOctId()); 
	//	drawString(new Vector3d(pm.getCenter()), pm.address+"");
		
	}
}
