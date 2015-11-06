package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.PartitionAddress;

import java.awt.Color;
import java.awt.Graphics;

public class PartitionDrawer extends Drawer{


	public PartitionDrawer()
	{
		super();
		this.name = "Partitions";
	}
	@Override
	public void draw(Graphics g) {
//		try
//		{
			synchronized (g) {
				
		
				Color c = g.getColor();
				g.setColor(Color.blue);
				for (PartitionAddress pm : ManagerResolver.I().getLocalPartitionAddresses()) {
					recursiveDrawTree(g,pm);
				//	drawString(new Vector3d(pm.getCenter()),pm.address+"");
				}
				g.setColor(c);
			}
//		}
//		catch (Exception e) {
//			ShowConsoleOutput.println("cause???");
//			
//		}
	}
	
		
	
	
	private void recursiveDrawTree(Graphics g,PartitionAddress d2) {
			drawCuboid(d2);
			for (int i = 0;i<8;i++)
			{
				double p[]  = d2.getCorners().get(i);
				g.setColor(Color.pink);
				//drawCyrcle(new Vector3d(p), 40);
//				drawString(new Vector3d(p),i+"");
				g.setColor(Color.blue);
			}
			
	}
}
