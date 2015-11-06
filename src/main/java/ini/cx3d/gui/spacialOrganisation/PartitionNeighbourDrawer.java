package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.PartitionAddress;

import java.awt.Color;
import java.awt.Graphics;

public class PartitionNeighbourDrawer extends Drawer{


	public PartitionNeighbourDrawer()
	{
		super();
		this.name = "Partition neighbours";
	}
	@Override
	public void draw(Graphics g) {
//		try
//		{
			synchronized (g) {
				
		
				Color c = g.getColor();
				g.setColor(Color.black);
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
			for(PartitionAddress p:  d2.getNeighbours())
			{
				this.drawLine(new Vector3d(p.getCenter()),new Vector3d(d2.getCenter()));
			}
		
	}
}
