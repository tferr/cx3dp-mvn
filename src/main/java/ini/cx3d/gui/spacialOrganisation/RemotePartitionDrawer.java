package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.SingleRemotePartitionManager;
import ini.cx3d.utilities.SomeColors;

import java.awt.Color;
import java.awt.Graphics;

public class RemotePartitionDrawer extends Drawer{


	public RemotePartitionDrawer()
	{
		super();
		SomeColors.initColorField();
		this.name = "Remote Partitions";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		
//		
		for (SingleRemotePartitionManager pm : ManagerResolver.I().getRemotePartitions()) {
			
			drawRemoteManager(g,pm);
		}
//		g.setColor(c);
	}
	
	private void drawRemoteManager(Graphics g, SingleRemotePartitionManager pm) {
		
		g.setColor(SomeColors.getColorAssociated(pm.getHost().hashCode()));
		drawCuboid(pm.getAddress());
		for (PhysicalNode n : pm.getNodes()) {
			g.setColor(SomeColors.getColorAssociated(pm.getHost().hashCode()));
			drawCyrcle(new Vector3d(n.getSoNode().getPosition()), 1);
//			g.setColor(Color.blue);
//			drawCyrcle(new Vector3d(temp.getPosition()), 0.5);
			g.setColor(Color.black);
			drawString(new Vector3d(n.getSoNode().getPosition()), n.getSoNode().getObjectRef().address+": "+n.getSoNode().getObjectRef().partitionId);
//			if(n.getSoNode().getID()==-1661032703999875L)
//			{
//				drawString(new Vector3d(n.soNodePosition()), n.getSoNode().getID()+" _ "+n.getSoNode().getObjectRef().getPartitionId().address);
//			}
		}
	
		
	}
}
