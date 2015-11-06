package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.MonitoringGui;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.spacialOrganisation.Iiterator;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.PartitionManager;

import java.awt.Color;
import java.awt.Graphics;

public class SpaceNodePositionDrawer extends Drawer{


	public SpaceNodePositionDrawer()
	{
		super();
		this.name = "Position drawer";
	}
	
	@Override
	public void draw(Graphics g) {
		long currentmarked = Long.MAX_VALUE;
		try{
			String text = MonitoringGui.getCurrent().getMarking().getText();
			currentmarked= Long.parseLong(text);
		}
		catch (Exception e) {
			
		}
		try{
			Color c = g.getColor();
			g.setColor(Color.red);
			for (PartitionManager p : ManagerResolver.I().getLocalPartitions()) {
//				if(p.address.address != 12) 
//					continue;
				if(p.count() ==0) continue;
				Iiterator iter = p.getIterator();
				iter.next();
				while(!iter.isAtEnd())
				{
					
					g.setColor(Color.black);
					double[] ps = iter.getCurrent().getPosition();
					drawString(new Vector3d(ps),String.format("%.2f%n", ps[0]) +","
							+String.format("%.2f%n", ps[1])+","+
							String.format("%.2f%n", ps[2]));
					iter.next();
					
					
				}
			}
			g.setColor(c);
		}
		catch(Exception e){
			OutD.println("an error has occured during draw");
		}
		
		
	}
	
	
}
