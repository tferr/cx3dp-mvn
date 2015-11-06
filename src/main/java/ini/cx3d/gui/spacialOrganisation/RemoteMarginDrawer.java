package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.Param;
import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.SingleRemotePartitionManager;

import java.awt.Color;
import java.awt.Graphics;

public class RemoteMarginDrawer extends Drawer{


	public RemoteMarginDrawer()
	{
		super();
		this.name = "Remote Margins";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(Param.PURPLE);
		for(SingleRemotePartitionManager s : ManagerResolver.I().getRemotePartitions()) {
			drawNodes(g, s);
		}
	
		g.setColor(c);
	}
	private void drawNodes(Graphics g,
			SingleRemotePartitionManager remotePartitionManager) {
		for(PhysicalNode n:remotePartitionManager.getNodes())
		{	
			drawCyrcle(new Vector3d(n.getSoNode().getPosition()), 2);
		}
		
	}
	
		

	
//	private void drawMargin(Graphics g, RemotePartitionManager rpm) {
//		
//		MarginBox[] pms = rpm.getMarginBoxes();
//		for (int i=0; i<pms.length;i++)
//		{
//			MarginBox pm = pms[i];
//			if(pm ==null) continue;
//			if(i>18) 
//				g.setColor(Color.black);
//			else if(i>6)
//				g.setColor(Color.green);
//			else
//				g.setColor(Param.ORANGE);
//			
//			Vector3d rem = new Vector3d(new double[]{-10,-10,-10}); 
//			for (Rectangle r : pm.getSides()) {
//				VecT<Vector3d> temp = r.edges();
//				drawLine(temp.get(0), temp.get(1));
//				drawLine(temp.get(0), temp.get(2));
//				drawLine(temp.get(3), temp.get(1));
//				drawLine(temp.get(3), temp.get(2));
//			}
//			drawString(new Vector3d(pm.getCenter()), rpm.stage+"");
//			for (ObjectReference n : pm.getSnapshot().keySet()) {
//				
//				g.setColor(Param.X_LIGHT_BLUE);
//				drawCyrcle(new Vector3d(n.position), 2.0);
//				g.setColor(Color.black);
//				
//			}
//			
//		}
//		
//	}
}
