package ini.cx3d.gui.physics.diffusion;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.simulation.ECM;
import ini.cx3d.spacialOrganisation.AbstractPartitionManager;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.utilities.Matrix;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class GradientDrawer extends Drawer{


	private String chemical;
	private double max=Double.MIN_VALUE;
	private double lastmax;
	public GradientDrawer(String name,String chemical)
	{
		super();
		this.chemical = chemical;
		this.name = name;
	}

	public GradientDrawer(String chemical)
	{
		super();
		this.chemical = chemical;
		this.name = "gradient of "+chemical;
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

			//				if(!d2.getSubstances().containsKey(chemical)) return;

			//double localcon = d2.getConcentration(chemical, d2.getCenter());
			//max = Math.max(localcon, max);

			// get 3 random positions and calculate the differences	
			//				for (int i =0;i<10;i++) {
			//					for (int j =0;j<10;j++) {
			//					
			//
			//					
			//						g.setColor(col);
			//						drawCyrcle(new Vector3d(randpos),len/(10));
			//						g.setColor(Color.black);
			////						drawString(new Vector3d(randpos),""+concentration);
			//					}
			
//			g.setColor(Color.red);
//			double max=0,min=Double.MAX_VALUE;
//			double [] max_dir=d2.getCenter();
//			for(double []a:genDescretePoints())
//			{
//				double[] pos = Matrix.add(d2.getCenter(),a);
//				double temp =d2.getConcentration(chemical, pos);
//				if(max<temp)
//				{
//					max = temp;
//					max_dir=pos;
//				}
//				min = Math.min(min,d2.getConcentration(chemical, pos));	
//			}
//			
//			
//			for(double []a:genDescretePoints())
//			{
//				double[] pos = Matrix.add(d2.getCenter(),a);
//				Color c = Param.X_LIGHT_RED;
//				g.setColor(c);
//				drawCyrcle(new Vector3d(pos), 40);
//				double temp =d2.getConcentration(chemical, pos);
//				g.setColor(c.black);
//				drawString(new Vector3d(pos), ""+temp);
//			}
//			double[] dir = Matrix.scalarMult(100,Matrix.normalize());
			//	Vector3d start = new Vector3d(Matrix.add(dir,d2.getCenter()));
			double [] max_dir = d2.getGradient(this.chemical,d2.getAddress().getCenter()); 
			max_dir = Matrix.add(max_dir,d2.getAddress().getCenter());
			g.setColor(Color.black);
			drawLine(new Vector3d( max_dir), new Vector3d(d2.getAddress().getCenter()));
			g.setColor(Color.pink);
			drawCyrcle(new Vector3d( max_dir), 3);
			
		}


//		double localcon =d2.getConcentration(chemical, d2.getCenter());
//
//		double[][] vectorsToNeighbors = new double[3][]; 
//		double[] differencesBetweenTheNeighborsAndThis = new double[3];

		// get 3 random positions and calculate the differences	
		//				for (int i =0;i<3;i++) {
		//					
		//					double[] pos = Matrix.add(d2.getCenter(),Matrix.randomNoise(0.4, 3));
		//					
		//				
		//					double cur_con = d2.getConcentration(chemical, pos);
		//					
		//					// prepare the linear system to be solved
		//					vectorsToNeighbors[i] = Matrix.subtract(d2.getCenter(), pos);
		//					differencesBetweenTheNeighborsAndThis[i] = (cur_con-localcon);
		//					// only three equations;
		//				}


		



	}
	private Color getRightColor(Color col,double t, double max,double min) {
		int alpha = Math.min(255,(int)(255.0/(max-min)*(t-min)));
		return new Color(col.getRed(),col.getGreen(),col.getBlue(),alpha);
	}

	private double [] getRandom(int i, int j,double space)
	{
		double [] tmp =Matrix.randomNoise(200, 2);
		double [] temp2 = new double [3];
		temp2[0] = i*space;
		temp2[1] = j*space;
		temp2[2] = 0;
		return temp2;
	}

	private ArrayList<double []> genRandompoints()
	{
		double r=200;
		ArrayList<double[]> temp = new ArrayList<double[]>();
		for(int i=0;i<10;i++)
		{
			double phi = ECM.getRandomDouble()*Math.PI*2;
			for(int j=0;j<10;j++)
			{
				double theta = ECM.getRandomDouble()*Math.PI*2-Math.PI;
				double [] t = new double[3];
				t[0]=r*Math.cos(theta)*Math.cos(phi);
				t[1]=r*Math.cos(theta)*Math.sin(phi);
				t[2]=r*Math.sin(theta);
				temp.add(t);
			}
		}
		return temp;
	}
	
	private ArrayList<double []> genDescretePoints()
	{
		double r=200;
		ArrayList<double[]> temp = new ArrayList<double[]>();
		
		for(double phi=0;phi<2*Math.PI;phi+=Math.PI/8)
		{
			for(double theta=Math.PI-Math.PI/8;theta<Math.PI;theta+=Math.PI/8)
			{
				double [] t = new double[3];
				t[0]=r*Math.cos(theta)*Math.cos(phi);
				t[1]=r*Math.cos(theta)*Math.sin(phi);
				t[2]=r*Math.sin(theta);
				temp.add(t);
			}
		}
		return temp;
	}

}
