package ini.cx3d.gui.physics.diffusion;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.MonitoringGui;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.Substance;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.simulation.ECM;
import ini.cx3d.spacialOrganisation.AbstractPartitionManager;
import ini.cx3d.spacialOrganisation.ManagerResolver;

import java.awt.Color;
import java.awt.Graphics;

public class ConcentrationDrawer extends Drawer{

	
	private String chemical;
	private double max=Double.MIN_VALUE;
	private double lastmax;
	public ConcentrationDrawer(String name,String chemical)
	{
		super();
		this.chemical = chemical;
		this.name = name;
	}
	
	public ConcentrationDrawer(String chemical)
	{
		super();
		this.chemical = chemical;
		this.name = "draw ex sub. "+chemical;
	}
	
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		lastmax = max;
		max=Double.MIN_VALUE;
		for (AbstractPartitionManager pm : ManagerResolver.I().getLocalPartitions()) {
			recursiveDrawTree(g,pm.getDiffusionNode());
		}
		g.setColor(c);
		
	}
	
	private void recursiveDrawTree(Graphics g,AbstractDiffusionNode d2) {
	
		
			if(!d2.isLeaf())
			{
				for(AbstractDiffusionNode d:d2.getLocalSubnodes())
				{
					recursiveDrawTree(g, d);
				}
			}
			else
			{
				if(!d2.getSubstances().containsKey(chemical)) return;
				if(!ECM.getInstance().getSubstanceTemplates().containsKey(chemical)) return;
				Color col = ECM.getInstance().getSubstanceTemplates().get(chemical).getColor();
				double concentration = d2.getInternalConcentration(chemical);
				max = Math.max(max, concentration); 
				int alpha = Math.min(255,(int)(255.0/Math.pow(lastmax,1.0/5.0)*Math.pow(concentration,1.0/5.0)));
				col = new Color(col.getRed(),col.getGreen(),col.getBlue(),alpha);
				g.setColor(col);
				drawCyrcle(new Vector3d(d2.getAddress().getCenter()),d2.getAddress().getSize());
			}
		
	}
	
	public static void generateDrawerForEachChemical()
	{
		MonitoringGui t = MonitoringGui.getCurrent();
		if(t!=null)
		{
			for (Substance s : ECM.getInstance().getSubstanceTemplates().values()) {
				t.addDrawer(new ConcentrationDrawer("Drawer Chem: "+s.getId(),s.getId()), false);
			}
		}
	}
	
}
