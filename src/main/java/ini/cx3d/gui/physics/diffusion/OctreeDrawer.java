package ini.cx3d.gui.physics.diffusion;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.spacialOrganisation.AbstractPartitionManager;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.utilities.Rectangle;

import java.awt.Graphics;
import java.util.ArrayList;

public class OctreeDrawer extends Drawer{


	public OctreeDrawer()
	{
		super();
		this.name = "octree drawer";
	}
	@Override
	public void draw(Graphics g) {
		for (AbstractPartitionManager pm : ManagerResolver.I().getLocalPartitions()) {
			recursiveDrawTree(g,pm.getDiffusionNode());
		}
	}
	
	private void recursiveDrawTree(Graphics g,AbstractDiffusionNode d2) {
			for (Rectangle r : d2.getAddress().getSides()) {
				ArrayList<Vector3d> temp = r.edges();
				drawLine(temp.get(0), temp.get(1));
				drawLine(temp.get(0), temp.get(2));
				drawLine(temp.get(3), temp.get(1));
				drawLine(temp.get(3), temp.get(2));
			}
			
			if(!d2.isLeaf())
			{
				for(AbstractDiffusionNode d:d2.getLocalSubnodes())
				{
					recursiveDrawTree(g, d);
				}
			}
			else
			{
				//drawString(new Vector3d(d2.getAddress().getCenter()),d2.getAddress().getOctId()); 
			}
	}
}
