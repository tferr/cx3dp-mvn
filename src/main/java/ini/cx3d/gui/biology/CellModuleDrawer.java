package ini.cx3d.gui.biology;

import ini.cx3d.biology.Cell;
import ini.cx3d.biology.CellModule;
import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.simulation.ECM;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;

public class CellModuleDrawer extends Drawer{

	

	public CellModuleDrawer()
	{
		super();
		this.name = "Cell Modules";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		//drawLine(new Vector3d(new double[]{100,100,100}),new Vector3d(new double[]{-100,-100,-100}),10);
		
		try{
			Collection<Cell> temp =  ECM.getInstance().getCellList();
			for(Cell s:temp)
			{
//			for(int i =0;i<temp.size();i++)
//			{
//				Cell s = temp.get(i);
				ArrayList<CellModule> t2 = s.getCellModules();
				for(CellModule cm :t2)
				{
					g.setColor(Color.pink);
					Vector3d origin = new Vector3d(s.getSomaElement().getLocation());
					drawCyrcle(origin,2 );
					g.setColor(Color.black);
					drawString(origin, cm.toString());
				}
				

				
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		g.setColor(c);
		
	}
}
