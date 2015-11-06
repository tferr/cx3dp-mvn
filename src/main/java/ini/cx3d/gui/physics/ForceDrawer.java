package ini.cx3d.gui.physics;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.spacialOrganisation.AbstractPartitionManager;
import ini.cx3d.spacialOrganisation.Iiterator;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.PartitionManager;

import java.awt.Color;
import java.awt.Graphics;



public class ForceDrawer extends Drawer{



	public ForceDrawer()
	{
		super();
		this.name = "Force";
	}
//	@Override
//	public void draw(Graphics g) {
//		Color c = g.getColor();
//
//		Set<Long> temp = ECM.getInstance().physicalSphereList.keySet();
//		for(long i:temp)
//		{
//			try
//			{
//				PhysicalSphere s = ECM.getInstance().physicalSphereList.get(i);
//				g.setColor(s.getColor());
//				drawCyrcle( new Vector3d(s.getSomaElement().getPhysical().getSoNode().getPosition()), s.getDiameter());
//			}
//			catch (Exception e) {
//				// TODO: handle exception
//			}	
//		}
//		g.setColor(c);
//	}
	
	public void draw(Graphics g) {

		try{
			Color c = g.getColor();
			g.setColor(Color.red);
			for (PartitionManager p : ManagerResolver.I().getLocalPartitions()) {
				Iiterator iter = p.getIterator();
				iter.next();
				while(!iter.isAtEnd())
				{
				
						ObjectReference r = iter.getCurrent();
						AbstractPartitionManager pm = ManagerResolver.I().resolve(r.getPartitionId());
						PhysicalNode o = pm.getPhysicalNode(r);
						PhysicalObject po = (PhysicalObject)o;
						drawArrow(g,new Vector3d(po.getSoNode().getPosition()),new Vector3d(po.getLastForce()));
						iter.next();
					
				}
			}
			g.setColor(c);
		}
		catch(Exception e){
			OutD.println("an error has occured during draw"+e);
		}
	}
}
