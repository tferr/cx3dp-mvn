package ini.cx3d.utilities.export.rendering;
import ini.cx3d.gui.Canvas3d;
import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.MonitoringGui;
import ini.cx3d.gui.RoiDrawer;
import ini.cx3d.gui.XDir;
import ini.cx3d.gui.YDir;
import ini.cx3d.gui.ZDir;
import ini.cx3d.gui.biology.CellModuleDrawer;
import ini.cx3d.gui.biology.LocalBiologyModuleDrawer;
import ini.cx3d.gui.physics.BoundariesDrawer;
import ini.cx3d.gui.physics.CylinderDrawer;
import ini.cx3d.gui.physics.CylinderDrawerJustLines;
import ini.cx3d.gui.physics.ForceDrawer;
import ini.cx3d.gui.physics.InternalSubstanceDrawer;
import ini.cx3d.gui.physics.PhyiscalBondDrawer;
import ini.cx3d.gui.physics.ShowNeighbors;
import ini.cx3d.gui.physics.SphereDrawer;
import ini.cx3d.gui.physics.SphereSliceDrawer;
import ini.cx3d.gui.physics.diffusion.ConcentrationDrawer;
import ini.cx3d.gui.physics.diffusion.ConcentrationDrawerNumbers;
import ini.cx3d.gui.physics.diffusion.GradientDrawer;
import ini.cx3d.gui.spacialOrganisation.PartitionDrawer;
import ini.cx3d.gui.spacialOrganisation.PartitionNeighbourDrawer;
import ini.cx3d.gui.spacialOrganisation.PartitionNumberDrawer;
import ini.cx3d.gui.spacialOrganisation.SpaceNodePositionDrawer;
import ini.cx3d.gui.spacialOrganisation.SpacenodeDrawer;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.dynamicLoading.LoadDrawers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class MonitoringImage implements Serializable {

	public ArrayList<Drawer> drawers_applied = new ArrayList<Drawer>(); 
	public Canvas3d component;


	public MonitoringImage() {
		component = new Canvas3d();
	}


	public Rectangle getDrawingDimension()
	{
		return component.getBounds();
	}

	public void addDrawer(final Drawer d,boolean show)
	{
		if(show)
		{
			drawers_applied.add(d);
			d.setCanvas(component);
		}
		component.setDrawers_applied(drawers_applied);

	}

	public void removeDrawer(final Drawer d,boolean show)
	{
		drawers_applied.remove(d);
	}

	
	public synchronized void renderBackground(BufferedImage bufferedImage)
	{
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, bufferedImage.getWidth(),bufferedImage.getHeight());
	}

	public synchronized void render(BufferedImage bufferedImage )
	{
		Graphics2D g2d = bufferedImage.createGraphics();
		loadNewDynamicDrawers();
		component.paint(g2d);
	}

	private void loadNewDynamicDrawers() {
		try {

			for (Drawer d : LoadDrawers.findNewDrawers()) {
				addDrawer(d, true);
			} 
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	public static MonitoringImage getStandardImageDrawSetup()
	{
		MonitoringImage t = new MonitoringImage();
    	t.addDrawer(new XDir(),true);
    	t.addDrawer(new YDir(),true);
    	t.addDrawer(new ZDir(),true);
	
		t.addDrawer(new PartitionDrawer(),true);
		t.addDrawer(new PartitionNumberDrawer(),false);
		t.addDrawer(new SpacenodeDrawer(), false);

		t.addDrawer(new ShowNeighbors(), false);
		t.addDrawer(new SphereDrawer(), true);
		t.addDrawer(new CylinderDrawer(), false);
		t.addDrawer(new CylinderDrawerJustLines(), true);
		t.addDrawer(new CellModuleDrawer(), false);
		t.addDrawer(new LocalBiologyModuleDrawer(), true);

		t.addDrawer(new PartitionNeighbourDrawer(), false);
		t.addDrawer(new BoundariesDrawer(), true);
		t.addDrawer(new SphereSliceDrawer(), false);
		t.addDrawer(new PhyiscalBondDrawer(), false);
		t.addDrawer(new SpacenodeDrawer(),false);
		t.addDrawer(new SpaceNodePositionDrawer(),false);
		t.addDrawer(new ForceDrawer(), false);
		t.addDrawer(new RoiDrawer(), true);


		return t;
	}

	private ArrayList<String> substances = new ArrayList<String>();
	public void addAllIntracellularSubstances()
	{
		for (String s : ECM.getInstance().getIntracelularSubstanceTemplates().keySet()) {
			if(!substances.contains(s))
			{
				substances.add(s);
				this.addDrawer(new InternalSubstanceDrawer(s), true);
			}
		}
	}

	public void addAllExtracellularSubstances()
	{
		for (String s : ECM.getInstance().getSubstanceTemplates().keySet()) {
			if(!substances.contains(s))
			{
				substances.add(s);
				this.addDrawer(new ConcentrationDrawer(s), false);
				this.addDrawer(new ConcentrationDrawerNumbers(s), true);
				this.addDrawer(new GradientDrawer(s), false);
			}
		}
	}


	public void setViewingSize720p()
	{
		component.setViewingSize( 1280, 720);
	}
	
	public void setViewingSize1080p()
	{
		component.setViewingSize( 1920, 1080);
	}
	
	public void setViewingSizePal()
	{
		component.setViewingSize(  640, 480);
	}


	public static MonitoringImage getMImageFromGui()
	    {
		   MonitoringImage I = new MonitoringImage();
		   for(Drawer d : MonitoringGui.getCurrent().drawers_applied)
		   {
			   I.addDrawer(d, true);
		   }
		   return I;
	    }

}