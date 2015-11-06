package ini.cx3d.gui.physics.diffusion;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.diffusion.DiffusionAddress;
import ini.cx3d.utilities.Rectangle;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class NotRecivedDiffNode extends Drawer{

	public static ArrayList<DiffusionAddress> outcast = new ArrayList<DiffusionAddress>();
	
	public static  void add(DiffusionAddress pm)
	{
		if(!outcast.contains(pm))
		{
			outcast.add(pm);
		}
	}
	public static  void remove(DiffusionAddress pm)
	{
		outcast.remove(pm);
	}
	
	
	public NotRecivedDiffNode()
	{
		super();
		this.name = "Not recived Diff node";
	}
	
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		
		g.setColor(Color.green);
		for (DiffusionAddress p : outcast) {
			drawRemoteManager(g,p);
		}
//		drawRemoteManager(g, DiffReg.I().get(-1651032704));
		g.setColor(c);
	}
	
	private void drawRemoteManager(Graphics g, DiffusionAddress pm) {
		Vector3d rem = new Vector3d(new double[]{-10,-10,-10}); 
		for (Rectangle r : pm.getSides()) {
			ArrayList<Vector3d> temp = r.edges();
			drawLine(temp.get(0), temp.get(1));
			drawLine(temp.get(0), temp.get(2));
			drawLine(temp.get(3), temp.get(1));
			drawLine(temp.get(3), temp.get(2));
		}
		//drawString(new Vector3d(pm.getCenter()), pm.id+"\n"+pm.getHost());
		
	}
}
