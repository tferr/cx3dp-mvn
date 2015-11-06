package ini.cx3d.gui.biology;

import ini.cx3d.biology.LocalBiologyModule;
import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.ArrayAccessHashTable;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class LocalBiologyModuleDrawer extends Drawer{

	

	public LocalBiologyModuleDrawer()
	{
		super();
		this.name = "Local Bio. Modules";
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
				ArrayList<LocalBiologyModule> ms =new ArrayList<LocalBiologyModule>();
				try{
					if(s instanceof PhysicalCylinder)
					{
					  ms = s.getAsPhysicalCylinder().getNeuriteElement().getLocalBiologyModulesList();
					}
					else
					{
						ms = s.getAsPhysicalSphere().getSomaElement().getLocalBiologyModulesList();
					}
				}
				catch (Exception e) {
					// TODO: handle exception
				}
				if(ms.size()==0) continue; 
				String toprint = "";
				Vector3d origin = new Vector3d(s.soNodePosition());
				int j =0;
				g.setColor(Color.pink);
				drawCyrcle(origin,2 );
				g.setColor(Color.black);
				for(LocalBiologyModule cm :ms)
				{
					
					toprint= cm.toString();
					drawString(origin,toprint,j*10);
					j++;
				}
				
				
				//drawString(origin,toprint);

				
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		g.setColor(c);
		
	}
}
