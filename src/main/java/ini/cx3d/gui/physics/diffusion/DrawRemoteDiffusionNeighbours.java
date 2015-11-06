package ini.cx3d.gui.physics.diffusion;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionAddress;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.utilities.SomeColors;

import java.awt.Color;
import java.awt.Graphics;

public class DrawRemoteDiffusionNeighbours extends Drawer{


	public DrawRemoteDiffusionNeighbours()
	{
		super();
		this.name = "Diffusion Neighbours Remote";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.blue);
		for (AbstractDiffusionNode pm : DiffusionNodeManager.I().getAllLocalDiffusionNodes()) {
			recursiveDrawTree(g,pm);
		}
		g.setColor(c);
	}
	
	private void recursiveDrawTree(Graphics g,AbstractDiffusionNode d2) {
			for (DiffusionAddress t : d2.getNeighboursAddresses()) {
				if(t.isLocal()) continue;
				g.setColor(SomeColors.getColorAssociated(t.getHost()));
				drawLine(new Vector3d(t.getCenter()),new Vector3d(d2.getAddress().getCenter()));
			}
	}
}
