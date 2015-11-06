package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.spacialOrganisation.AbstractPartitionManager;
import ini.cx3d.spacialOrganisation.ManagerResolver;

import java.awt.Color;
import java.awt.Graphics;

public class MarginDrawer extends Drawer{


	public MarginDrawer()
	{
		super();
		this.name = "Margins";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.lightGray);
		for (AbstractPartitionManager pm : ManagerResolver.I().getLocalPartitions()) {
//			drawMargin(g,((PartitionManager)pm).getMarginBoxes());
		}
		g.setColor(c);
	}
	
		

//	
//	private void drawMargin(Graphics g, MarginBox[] pms) {
//		if(pms==null) return;
//		for (MarginBox pm : pms) {
//			if(pm ==null) continue;
//			Vector3d rem = new Vector3d(new double[]{-10,-10,-10}); 
//			for (Rectangle r : pm.getSides()) {
//				VecT<Vector3d> temp = r.edges();
//				drawLine(temp.get(0), temp.get(1));
//				drawLine(temp.get(0), temp.get(2));
//				drawLine(temp.get(3), temp.get(1));
//				drawLine(temp.get(3), temp.get(2));
//			}
//			g.setColor(Color.blue);
//			for (ObjectReference n : pm.getSnapshot().keySet()) {
//				
//				drawCyrcle(new Vector3d(n.position), 2.0);
//			}
//			g.setColor(Color.lightGray);
//		}
//		
//	}
}
