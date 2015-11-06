package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.Param;
import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.MonitoringGui;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.spacialOrganisation.Iiterator;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.PartitionManager;

import java.awt.Color;
import java.awt.Graphics;

public class SpacenodeDrawer extends Drawer{


	public SpacenodeDrawer()
	{
		super();
		this.name = "SpacenodeDrawer";
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
					if(iter.getCurrent().address == currentmarked)
					{
						g.setColor(Param.VIOLET);
						drawCyrcle(new Vector3d(iter.getCurrent().getPosition()), 3);
						iter.next();
					}
					else
					{
//						ObjectReference temp = ORR.I().get(iter.getCurrent().address);
						g.setColor(Color.red);
						drawCyrcle(new Vector3d(iter.getCurrent().getPosition()), 0.01);
//						g.setColor(Color.blue);
//						drawCyrcle(new Vector3d(temp.getPosition()), 0.5);
//						g.setColor(Color.black);
//						drawString(new Vector3d(iter.getCurrent().getPosition()), iter.getCurrent().address+": "+iter.getCurrent().partitionId);
						iter.next();
					}
					
				}
			}
			g.setColor(c);
		}
		catch(Exception e){
			OutD.println("an error has occured during draw");
		}
		
		
	}
	
	
}
