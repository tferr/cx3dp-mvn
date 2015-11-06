package ini.cx3d.spacialOrganisation.fixGrid;

import java.util.concurrent.locks.ReentrantLock;

import ini.cx3d.spacialOrganisation.IObjectReferenceSearchVisitor;
import ini.cx3d.spacialOrganisation.ORR;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.utilities.Cuboid;


public class Grid {

	private static Grid current;
	
	
	
	public static Grid I(double min [], double max[],int xbins, int ybins, int zbins)
	{
		if(current==null)
		{
			current=new  Grid(min, max,xbins,ybins, zbins);
		}
		return current;
	}
	
	public static Grid I()
	{
		return current;
	}
	
	
	public Cuboid cube;
	public int [] bins;
	private double [] min;
	private double [] max;
	public VecOptimisticLock [] subGrids;
	private double  maxradiusseen; 
	private ReentrantLock lock = new ReentrantLock();
	
	private Grid(double min [], double max[],int xbins, int ybins, int zbins)
	{
		cube = new Cuboid(min,max);
		this.max =cube.getUpperLeftCornerFront();
		this.min = cube.getLowerRightCornerBack();
		bins = new int[]{xbins,ybins,zbins};
		subGrids =  new  VecOptimisticLock[bins[0]*bins[1]*bins[2]];
		
	}
	
	
	private int [] getBinAdddress(ObjectReference r)
	{
		int [] address = {0,0,0};
		address[0] =  (int)(bins[0]/(max[0]-min[0])*(r.getPosition()[0]-min[0]));
		address[1] =  (int)(bins[1]/(max[1]-min[1])*(r.getPosition()[1]-min[1]));
		address[2] =  (int)(bins[2]/(max[2]-min[2])*(r.getPosition()[2]-min[2]));
		return address;
	}
	
	public int [] getBinAdddress(double[] d)
	{
		int [] address = {0,0,0};
		address[0] =  (int)(bins[0]/(max[0]-min[0])*(d[0]-min[0]));
		address[1] =  (int)(bins[1]/(max[1]-min[1])*(d[1]-min[1]));
		address[2] =  (int)(bins[2]/(max[2]-min[2])*(d[2]-min[2]));
		return address;
	}
	
	private boolean samebin(int i1[] , int i2[])
	{
		return i1[0]==i2[0] && i1[1]==i2[1] && i1[2]==i2[2];
	}
	
	private int [] getRadius(double r)
	{
		int [] rad = {0,0,0};
		rad[0] =  (int)(bins[0]/(max[0]-min[0])*(r-min[0]))+1;
		rad[1] =  (int)(bins[1]/(max[1]-min[1])*(r-min[1]))+1;
		rad[2] =  (int)(bins[2]/(max[2]-min[2])*(r-min[2]))+1;
		return rad;
	}
	
	
	public VecOptimisticLock getSlot(int [] i)
	{
		
		return subGrids[calcAddress(i)];
	}
	
	private  int calcAddress(int [] i)
	{
		return i[2]*bins[2]*bins[1]+i[1]*bins[1]+i[0];
	}
	
	public boolean contains(ObjectReference r) {
		VecOptimisticLock s = getSlot(getBinAdddress(r));
		if(s ==null) return false;
		if(s.contains(r)) return true;
		return false;
	}
	public void checkNCreate(int [] i)
	{
		int[][] r = getRange(i, 5);
		if(!containsKey(i))
		{
			int []  k = new int[3];
			for(k[0]=r[0][0];k[0]<r[1][0];k[0]++)
			{
				for(k[1]=r[0][1];k[1]<r[1][1];k[1]++)
				{
					for(k[2]=r[0][2];k[2]<r[1][2];k[2]++)
					{	
						if(!containsKey(k))
						{
							lock.lock();
							if(!containsKey(k))
							{
								subGrids[calcAddress(k)] = new VecOptimisticLock(20);
							}
							count ++;
							lock.unlock();
						}
					}
				}
			}
			
		}
		
	}
	
	private boolean containsKey(int[] i) {
		return subGrids[calcAddress(i)] !=null;
	}


	public int count=0;
	
	private int [][] getRange(int [] i,int [] r) 
	{
		int [][] range = new int[2][3];
		
		range[0][0] = Math.max(0, i[0]-r[0]);
		range[0][1] = Math.max(0, i[1]-r[1]);
		range[0][2] = Math.max(0, i[2]-r[2]);
		
		range[1][0] = Math.min(bins[0], i[0]+r[0]+1);
		range[1][1] = Math.min(bins[1], i[1]+r[1]+1);
		range[1][2] = Math.min(bins[2], i[2]+r[2]+1);
		return range;
		
	}
	
	private int [][] getRange(int [] i,int r) 
	{
		return getRange(i, new int[]{r,r,r});
	}
	
	public void searchRange(ObjectReference point,double radius, IObjectReferenceSearchVisitor visitor)
	{
		double[] pos2 = point.getPosition();
		int [] center = getBinAdddress(point);
		maxradiusseen = Math.max(radius,maxradiusseen);
		int [] rad = getRadius(maxradiusseen);
		int[][] r = getRange(center, rad);
		
		double rad1 = point.getRadius();
		int [] i = new int[3];
		for(i[0]=r[0][0];i[0]<r[1][0];i[0]++)
		{
			for(i[1]=r[0][1];i[1]<r[1][1];i[1]++)
			{
				for (i[2]=r[0][2];i[0]<r[1][2];i[2]++)
				{
//					if(distanceInRange(point.getPosition(), , radius))
//					{
						VecOptimisticLock s2 = getSlot(i);
						if(s2 == null) continue;
						ObjectReference ref;

						for(int l=0;l<s2.capacity();l++)
						{
							ref= s2.get(l);
							if(ref ==null) continue;
							double[] pos1 = ref.getPosition();
							double totr = rad1+ref.getRadius()+2; //2 as margin for having neighours with no effect
							
							double d1 = pos1[0]-pos2[0];
						    double d2 = pos1[1]-pos2[1];
						    double d3 = pos1[2]-pos2[2];
	
						    
						    if(d1*d1+d2*d2+d3*d3>totr*totr) continue; 
						    if(ref.address  == point.address) continue;
							visitor.visit(ORR.I().get(ref.address));
						}
//					}
					
				}
			}	
			
		}
	}
	
//	private boolean distanceInRange(double [] c, int [] r,double radius)
//	{
//		//write this again
//		return true;
//		
//	}
	
	public void insert(ObjectReference r)
	{
		int i [] = getBinAdddress(r);
		checkNCreate(i);
		getSlot(i).add(r);
		if(!getSlot(getBinAdddress(r)).contains(r))
		{
			System.out.println("error");
		}
	}
	
	public void remove(ObjectReference r)
	{
		getSlot(getBinAdddress(r)).remove(r);
	}
	private volatile double moveTimetot = 0;
	
	public void move(ObjectReference r, double[] position)
	{
		
		if(!containsCoordinates(position)) 
		{
			position = correctPosition(position);
		}
////		System.out.println("fine "+r.address+" "+r.getPosition()[0]+" "+r.getPosition()[1]+" "+r.getPosition()[2]);
//		if(getSlot(getBinAdddress(r))==null)
//		{
//			System.out.println("slot null why "+r.address+" "+r.getPosition()[0]+" "+r.getPosition()[1]+" "+r.getPosition()[2]);
//		} 
//		VecOptimisticLock c = getSlot(getBinAdddress(r));
//		if(!c.contains(r))
//		{
//			
//			System.out.println("error");
//			System.out.print("");
//		}
		int[] i1 = getBinAdddress(r);
		int[] i2 = getBinAdddress(position);
		if(samebin(i1, i2)) return;
		VecOptimisticLock s = getSlot(i1);
		s.remove(r);
		r.setPosition(position);
		checkNCreate(i2);
		getSlot(i2).add(r);
//		int [] p =  getBinAdddress(r);
//		if(!samebin(p, i2))
//		{
//			System.out.println("inconsistent!"+r);
//		}
//		if(!getSlot(getBinAdddress(r)).contains(r))
//		{
//			System.out.println("error");
//		}
		
	}
	public boolean containsCoordinates( double[] corrds)
	{
		boolean temp = true;
		temp &= max[0] > corrds[0] && corrds[0]  > min[0];
		temp &= max[1] > corrds[1] && corrds[1]  > min[1];
		temp &= max[2] > corrds[2] && corrds[2]  > min[2];
		return temp;
	}
	
	
	private double[] correctPosition(double[] position) {

		position [0]  = Math.min(Math.max(position[0], min[0]+1), max[0]-1);
		position [1]  = Math.min(Math.max(position[1], min[1]+1), max[1]-1); 
		position [2]  = Math.min(Math.max(position[2], min[2]+1), max[2]-1);
		return position;
		
	}

	public void DebugCheckConsistency()
	{
		int i [] = new int[3];
		int tot = 0;
		for(i[0]=0;i[0]<bins[0];i[0]++)
		{
			for(i[1]=0;i[1]<bins[1];i[1]++)
			{
				for(i[2]=0;i[2]<bins[2];i[2]++)
				{
					VecOptimisticLock s = getSlot(i);
					if(s==null) continue;
					for(int f = 0;f<s.capacity();f++)
					{
						ObjectReference j = s.get(f);
						if(j==null) continue;
						tot++;
						int [] p =  getBinAdddress(j);
						if(!samebin(p, i))
						{
							System.out.println("inconsistent!"+j);
						}
					}
				}
			}
			
		}
		System.out.println("total "+tot);
	}
	
	public void DebugGetMaxListSize()
	{
		int i [] = new int[3];
		int tot = 0;
		for(i[0]=0;i[0]<bins[0];i[0]++)
		{
			for(i[1]=0;i[1]<bins[1];i[1]++)
			{
				for(i[2]=0;i[2]<bins[2];i[2]++)
				{
					VecOptimisticLock s = getSlot(i);
					if(s==null) continue;
					//tot= Math.max(tot, s.size());
				}
			}
			
		}
		System.out.println("maxListSize "+tot);
	}

	public int getXMax()
	{
		return bins[0];
	}
	
	public void executeOnGrid(int x,GridVisitor v)
	{
		int i[] = new int[3];
		i[0] = x;
		for(i[1]=0;i[1]<bins[1];i[1]++)
		{
			for(i[2]=0;i[2]<bins[2];i[2]++)
			{
				VecOptimisticLock s2 = getSlot(i);
				if(s2 == null) continue;
				ObjectReference ref;
				for(int l=0;l<s2.capacity();l++)
				{
					
					ref= s2.get(l);
					if(ref!=null)
					{
						v.process(ref);
					}
				}
			}
		}
	}
	
}

