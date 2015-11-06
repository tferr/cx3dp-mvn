package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.spacialOrganisation.Iiterator;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.PartitionManager;
import ini.cx3d.utilities.SomeColors;

import java.awt.Color;
import java.awt.Graphics;

public class DrawNodesToExchange extends Drawer{


	public DrawNodesToExchange()
	{
		super();
		SomeColors.initColorField();
		this.name = "Exchange nodes";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		try{
			g.setColor(Color.red);
			for (PartitionManager p : ManagerResolver.I().getLocalPartitions()) {
				Iiterator iter = p.getIterator();
				iter.next();
				
				while(!iter.isAtEnd())
				{
					PhysicalNode pn = p.getPhysicalNode(iter.getCurrent());
					int i = 0;
					for (String host : pn.dependingHosts) {
						i++;
						g.setColor(Color.black);
						if(pn.getSoNode().getID()==-1661032703999875L)
						{
							drawString(new Vector3d(pn.soNodePosition()), pn.getSoNode().getID()+"");
						}
						drawCyrcle(new Vector3d(iter.getCurrent().getPosition()), 4,i*2);
						g.setColor(SomeColors.getColorAssociated(host.hashCode()+1));
						drawCyrcle(new Vector3d(iter.getCurrent().getPosition()), 2,i*2);
						
					}
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
