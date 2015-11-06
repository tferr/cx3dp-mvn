package ini.cx3d.gui.biology;

import ini.cx3d.biology.Cell;
import ini.cx3d.biology.LocalBiologyModule;
import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalSphere;

import java.awt.Graphics;
import java.util.Vector;

public class GolgiStaining extends Drawer{


	public static Vector<Cell> cells = new Vector<Cell>();
	
	
	public GolgiStaining()
	{
		super();
		this.name = "golgi staining";
	}
	@Override
	public void draw(Graphics g) {

		try{
			for (Cell c: cells) {
				g.setColor(c.getSomaElement().getPhysical().getColor());
				visit(c.getSomaElement().getPhysicalSphere());
			
			}
		}
		catch (Exception e) {
			OutD.println(e);
		}

	}
	private void visit(PhysicalSphere physicalSphere) {
		drawCyrcle( new Vector3d(physicalSphere.massLocation), physicalSphere.getDiameter()/2);
		for (PhysicalCylinder c : physicalSphere.getDaughters()) {
			visit(c);
		}
		
	}
	private void visit(PhysicalCylinder c) {
		
		setColor(c.getColor());
		drawLine(new Vector3d(c.proximalEnd()),new Vector3d(c.distalEnd()));
		if(c.getDaughterLeft()!=null)
		{
			visit(c.getDaughterLeft());
		}
		if(c.getDaughterRight()!=null)
		{
			visit(c.getDaughterRight());
		}
		
	}
	
	
	
}
