package ini.cx3d.gui.physics;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.PhysicalBond;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.simulation.ECM;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;

public class PhyiscalBondDrawer extends Drawer{



	public PhyiscalBondDrawer()
	{
		super();
		this.name = "Physical Bonds";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.black);
		//drawLine(new Vector3d(new double[]{100,100,100}),new Vector3d(new double[]{-100,-100,-100}),10);
		try{
			Collection<PhysicalCylinder> temp = ECM.getInstance().getPhysicalCylinderList();
			for(PhysicalCylinder s:temp)
			{

	
				for (PhysicalBond p :s.getPhysicalBonds()) {
					try{
						g.setColor(Color.black);
						drawLine(new Vector3d(p.getFirstEndLocation()),new Vector3d(p.getSecondEndLocation()));
						
						double percent = 100/p.getRestingLength()*p.getActualLength();
						percent = percent-100;
						float a = (float)(1.0/p.getBreakingPointInPercent()*percent);
						g.setColor(new Color(1.0f,0.0f,0.0f,a));
						drawLine(new Vector3d(p.getFirstEndLocation()),new Vector3d(p.getSecondEndLocation()));
						
					}
					catch (Exception e) {
						// TODO: handle exception
					}
				}
				

			}
			
			Collection<PhysicalSphere> tempS = ECM.getInstance().getPhysicalSphereList();
			for(PhysicalSphere s:tempS)
			{
				for (PhysicalBond p :s.getPhysicalBonds()) {
					try{
						g.setColor(Color.gray);
						drawLine(new Vector3d(p.getFirstEndLocation()),new Vector3d(p.getSecondEndLocation()));
						
						double percent = 100/p.getRestingLength()*p.getActualLength();
						percent = Math.abs(percent-100);
						float a = (float)(1.0/p.getBreakingPointInPercent()*percent);
						if(a>1) OutD.println("bigger1"+p.getID());
						a = Math.min(a, 1.0f);
						g.setColor(new Color(1.0f,0.0f,0.0f,a));
						drawLine(new Vector3d(p.getFirstEndLocation()),new Vector3d(p.getSecondEndLocation()));
					}
					catch (Exception e) {
						// TODO: handle exception
					}
				}
				

			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		g.setColor(c);

	}
}
