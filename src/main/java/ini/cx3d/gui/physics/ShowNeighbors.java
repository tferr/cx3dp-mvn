package ini.cx3d.gui.physics;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.ArrayAccessHashTable;

import java.awt.Color;
import java.awt.Graphics;

public class ShowNeighbors extends Drawer{



	public ShowNeighbors()
	{
		super();
		this.name = "Show Neighbors";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		//drawLine(new Vector3d(new double[]{100,100,100}),new Vector3d(new double[]{-100,-100,-100}),10);
		g.setColor(Color.black);
		try{
			ArrayAccessHashTable temp = ECM.getInstance().getPhysicalNodes();
			for(int i=0;i<temp.size();i++)
			{
				PhysicalNode s = temp.getIfTobeProcessed(i);
				if(s==null) continue;
				for (PhysicalNode n :s.getNeighboringPhysicalNodes()) {
					drawLine(new Vector3d( n.getSoNode().getPosition()),new Vector3d(s.getSoNode().getPosition()));
				}
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		g.setColor(c);

	}
}
