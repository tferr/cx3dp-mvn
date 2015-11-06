package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.PartitionAddress;

import java.awt.Color;
import java.awt.Graphics;

public class PartitionNumberDrawer extends Drawer{


	public PartitionNumberDrawer()
	{
		super();
		this.name = "Partitions numbers";
	}
	@Override
	public void draw(Graphics g) {
//		try
//		{
			synchronized (g) {
				
		
				Color c = g.getColor();
				g.setColor(Color.blue);
				for (PartitionAddress pm : ManagerResolver.I().getLocalPartitionAddresses()) {
				//	recursiveDrawTree(g,pm);
					drawString(new Vector3d(pm.getCenter()),pm.getOctId());
				}
				g.setColor(c);
			}
//		}
//		catch (Exception e) {
//			ShowConsoleOutput.println("cause???");
//			
//		}
	}
	
		

}
