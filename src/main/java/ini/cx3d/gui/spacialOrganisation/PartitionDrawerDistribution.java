package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.PartitionAddress;
import ini.cx3d.spacialOrganisation.PartitionManager;
import ini.cx3d.utilities.Cuboid;
import ini.cx3d.utilities.Matrix;
import ini.cx3d.utilities.SomeColors;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.Vector;

public class PartitionDrawerDistribution extends Drawer{


	public PartitionDrawerDistribution()
	{
		super();
		this.name = "Partitions distribution";
	}
	
	
	Hashtable<Long, Integer> position  = new Hashtable<Long, Integer>();
	Hashtable<Integer,Vector<Integer>> neighbours  = new Hashtable<Integer, Vector<Integer>>();
	double partitioncount = 0;
	double numrOf=3;
	
	
	private TreeSet<Xcut> sortit()
	{
		TreeSet<Xcut> m = new TreeSet<Xcut>(
				new Comparator<Xcut>()
				{
					public int compare(Xcut o1, Xcut o2) {
						if(o1.x>o2.x) return 1;
						else return -1;
					
					}
				}
				
				
		);
		
		Hashtable<Double,Xcut> putter = new Hashtable<Double, Xcut>();  
		
		
		for (PartitionAddress pm : ManagerResolver.I().getLocalPartitionAddresses()) {
			if(!putter.containsKey(pm.getCenter()[0]))
			{
				Xcut l =  new Xcut();
				l.x = pm.getCenter()[0];
				m.add(l);
				putter.put(pm.getCenter()[0],l);
				
			}
			putter.get(pm.getCenter()[0]).pmas.add(pm);
		}
		return m;
	}
	
	@Override
	public void draw(Graphics g) {
		numrOf=5;
		
		if(ManagerResolver.I().getLocalPartitionAddresses().size()<2) return;
		if(partitioncount!=ManagerResolver.I().getLocalPartitionAddresses().size())
		{
			partitioncount=ManagerResolver.I().getLocalPartitionAddresses().size();
			Vector<PartitionAddress> alladdresses = new Vector<PartitionAddress>(ManagerResolver.I().getLocalPartitionAddresses());
			
			Object[] o = sortit().toArray();
			
			
			double minsize = SimulationState.getLocal().totalObjectCount/numrOf;
			int i=0;
			for(double t=0;t<numrOf;t++)
			{	
				int counter=0;
				
				while(counter<(minsize*0.6))
				{
					if(i>=o.length) break;
					Xcut xc = (Xcut) o[i];
					for (PartitionAddress part : xc.pmas) {
	
						position.put(part.address, (int)t);
						counter+=ManagerResolver.I().resolve(part).count();
					}
					i++;
				}
				System.out.println(counter+" : "+t);
			}
			countNeighbours();
			for (Integer t : neighbours.keySet()) {
				System.out.println(t+" has count: "+neighbours.get(t).size());
			}
			
			int lastfittness = Integer.MAX_VALUE;

		}		
		drawthis(g);

	}
	
	private void drawthis(Graphics g)
	{
		Color c = g.getColor();
		double off = -410;	
		for(double t=0;t<numrOf;t++)
		{	
			for(PartitionAddress m: ManagerResolver.I().getLocalPartitionAddresses())
			{
				if(position.get(m.address)==t)
				{
					g.setColor(SomeColors.getColorAssociated(t));
					recursiveDrawTree(g,m,off);
				}
			}
			//off+=420;
		}
		g.setColor(c);
	}

	private int countNeighbours() {
		for(int t =0;t<numrOf;t++)
		{
			neighbours.put(t, new Vector<Integer>());
		}
		for (PartitionManager o1 : ManagerResolver.I().getLocalPartitions())
		{
			for (PartitionManager o2 : ManagerResolver.I().getLocalPartitions())
			{
				//System.out.println("are we neigbours "+o1.address.getOctId()+" "+o2.address.getOctId()+" "+o1.address.areWeNeighbours(o2.address));
//				if(o1.count()==0) continue;
//				if(o2.count()==0) continue;
				if(!o1.address.areWeNeighbours(o2.address)) continue;
				int pos1 = position.get(o1.address.address);
				int pos2 = position.get(o2.address.address);
				if(pos1==pos2) continue;
				if(neighbours.get(pos1).contains(pos2)) continue;
				neighbours.get(pos1).add(pos2);
			}
		}
		int counter=0;
		for (Integer t : neighbours.keySet()) {
			counter +=neighbours.get(t).size();
//			System.out.println(t+" has count: "+neighbours.get(t).size());
		}
		return counter;
	}
	
	
	private int diffpernumber() {
		
		Hashtable<Integer, Integer> clustercount = new Hashtable<Integer, Integer>();
		for(int t=0;t<numrOf;t++)
		{	
			clustercount.put(t,(int)(ManagerResolver.I().getLocalPartitionAddresses().size()/numrOf));
		}
		for(PartitionAddress m: ManagerResolver.I().getLocalPartitionAddresses())
		{
			int t = position.get(m.address);
			clustercount.put(t,clustercount.get(t)-1);
		}
		int counter=0;
		for (Integer t : clustercount.keySet()) {
			counter +=clustercount.get(t)*clustercount.get(t);
		}
		return counter;
	}




	private void recursiveDrawTree(Graphics g,PartitionAddress d2,double offset) {
		
		
		double [] c1 = Matrix.add(d2.getUpperLeftCornerFront(), new double []{offset,0.0,0.0});
		double [] c2 = Matrix.add(d2.getLowerRightCornerBack(), new double []{offset,0.0,0.0});
		
		Cuboid o;
		drawCuboid(o = new Cuboid(c1,c2));
		g.setColor(Color.black);
		
		g.setColor(Color.green);
		drawCyrcle(new Vector3d(offset,0,0), 2);
//		drawString(new Vector3d(o.getCenter()),d2.getOctId());

	}
}

class Xcut
{
	public Vector<PartitionAddress> pmas = new Vector<PartitionAddress>();
	public double x;
}
