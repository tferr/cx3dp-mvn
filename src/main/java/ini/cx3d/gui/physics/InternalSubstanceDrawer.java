package ini.cx3d.gui.physics;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.IntracellularSubstance;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.Matrix;

import java.awt.Color;
import java.awt.Graphics;

public class InternalSubstanceDrawer extends Drawer{


	private String id;
	private double max=Double.MIN_VALUE;
	public InternalSubstanceDrawer(String id)
	{
		super();
		this.id = id;
		this.name = "Internal Substance: "+id;
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();

		//drawLine(new Vector3d(new double[]{100,100,100}),new Vector3d(new double[]{-100,-100,-100}),10);
		try{
			ArrayAccessHashTable temp = ECM.getInstance().getPhysicalNodes();
			for(int i=0;i<temp.size();i++)
			{
				PhysicalNode s = temp.get(i);
				if(s==null) continue;
				drawsubstance(g, s);	
			}

//			for (RemotePartitionManager pmr : ManagerResolver.I().getAllRemotePartition()) {
//				for(MarginBox m: pmr.getMarginBoxes())
//				{
//					if(m==null) continue;
//					for(PhysicalNode n: m.getAllPhysicals())
//					{
//						drawsubstance(g, n);
//					}
//				}
//			}

		}
		catch (Exception e) {
			// TODO: handle exception
		}
		g.setColor(c);

	}
	private void drawsubstance(Graphics g, PhysicalNode o) {
		if(o instanceof PhysicalCylinder)
		{
			PhysicalCylinder s = (PhysicalCylinder)o;
			IntracellularSubstance sub = s.getIntracellularSubstance(id);
			if(sub!=null)
			{
				max = Math.max(max, s.getIntracellularConcentration(id));
				Color color = sub.getColor();
				int intensity = (int)(255/max*s.getIntracellularConcentration(id));
				g.setColor(Color.black);
				drawString(new Vector3d(Matrix.add(s.getMassLocation(),new double[]{10,0,0})), s.getIntracellularConcentration(id)+"");
				g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),intensity));
				if(s.getSpringAxis() ==null) return;
				drawLine(new Vector3d(s.proximalEnd()),new Vector3d(s.distalEnd()),s.getDiameter());
			}
		}
		else
		{
			PhysicalSphere s = (PhysicalSphere)o;

			IntracellularSubstance sub = s.getIntracellularSubstance(id);
			if(sub!=null)
			{
				max = Math.max(max, s.getIntracellularConcentration(id));
				Color color = sub.getColor();
				int intensity = (int)(254/max*s.getIntracellularConcentration(id));
				OutD.println("intensity" +s.getIntracellularConcentration(id));
				//							g.setColor(Color.black);
				//							drawString(new Vector3d(s.getMassLocation()), s.getIntracellularConcentration(id)+"");
				g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),intensity));
				drawCyrcle(new Vector3d(s.getMassLocation()),s.getDiameter());
			}
		}
	}
}
