package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.spacialOrganisation.Iiterator;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.PartitionManager;

import java.awt.Color;
import java.awt.Graphics;

public class SpacenodeDrawerLines extends Drawer{


	public SpacenodeDrawerLines()
	{
		super();
		this.name = "Spacenodes order";
	}
	
	@Override
	public void draw(Graphics g) {
		try{
			Color c = g.getColor();
			g.setColor(Color.red);
			for (PartitionManager p : ManagerResolver.I().getLocalPartitions()) {
				Iiterator iter = p.getIterator();
				iter.next();
				if(!iter.isAtEnd())
				{
					
					g.setColor(Color.green);
					drawCyrcle(new Vector3d(iter.getCurrent().getPosition()), 0.1);
					g.setColor(Color.red);
					Vector3d start = new Vector3d(iter.getCurrent().getPosition());
					Vector3d end;
					while(!iter.isAtEnd())
					{
						end =  new Vector3d(iter.getCurrent().getPosition());
						drawLine(start,end);
						iter.next();
						start = end;
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
