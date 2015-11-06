package ini.cx3d.gui.physics.diffusion;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionAddress;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;

import java.awt.Color;
import java.awt.Graphics;

public class DrawLocalDiffusionNeighbours extends Drawer{


	public DrawLocalDiffusionNeighbours()
	{
		super();
		this.name = "Diffusion Neighbours Local";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.red);
		for (AbstractDiffusionNode pm : DiffusionNodeManager.I().getAllLocalDiffusionNodes()) {
			recursiveDrawTree(g,pm);
		}
		g.setColor(c);
	}
	
	private void recursiveDrawTree(Graphics g,AbstractDiffusionNode d2) {
			for (DiffusionAddress t : d2.getNeighboursAddresses()) {
				if(!t.isLocal()) continue;
				drawLine(new Vector3d(t.getCenter()),new Vector3d(d2.getAddress().getCenter()));
			}
	}
}
