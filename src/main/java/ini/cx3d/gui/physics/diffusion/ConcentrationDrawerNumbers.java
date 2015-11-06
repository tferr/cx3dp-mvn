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
import java.text.DecimalFormat;

public class ConcentrationDrawerNumbers extends Drawer{

	
	private String chemical;
	private double max=Double.MIN_VALUE;
	private double lastmax;
	public ConcentrationDrawerNumbers(String name,String chemical)
	{
		super();
		this.chemical = chemical;
		this.name = name;
	}
	
	public ConcentrationDrawerNumbers(String chemical)
	{
		super();
		this.chemical = chemical;
		this.name = "conc. of "+chemical;
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		lastmax = max;
		max=Double.MIN_VALUE;
		g.setColor(c.black);
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
				DecimalFormat twoPlaces = new DecimalFormat("0.00");

				
				if(!d2.getSubstances().containsKey(chemical)) return;
				double concentration = d2.getInternalConcentration(chemical);
				if(concentration>0)
				{
					drawString(new Vector3d(d2.getAddress().getCenter()),twoPlaces.format(concentration)+"");
				}
				
			}
		
	}
	
	public static void generateDrawerForEachChemical()
	{
		MonitoringGui t = MonitoringGui.getCurrent();
		if(t!=null)
		{
			for (Substance s : ECM.getInstance().getSubstanceTemplates().values()) {
				t.addDrawer(new ConcentrationDrawerNumbers("Concentration Chem: "+s.getId(),s.getId()), false);
			}
		}
	}
	
}
