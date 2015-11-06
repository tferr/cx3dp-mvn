package ini.cx3d.spacialOrganisation.slot;
import ini.cx3d.Param;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.gui.spacialOrganisation.DrawSlotGrid;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.spacialOrganisation.IObjectReferenceSearchVisitor;
import ini.cx3d.spacialOrganisation.ISpacialRepesentation;
import ini.cx3d.spacialOrganisation.IToExectue;
import ini.cx3d.spacialOrganisation.Iiterator;
import ini.cx3d.spacialOrganisation.ORR;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.OptimisticExecuter;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.Cuboid;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.Matrix;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Slot extends Cuboid implements Serializable,ISpacialRepesentation{
	DirtyLinkedList<ObjectReference>[][][] slots;

	private transient HashT<Integer,Lock> locks;
	public double [] unit =  new double [3];
	public double [] unit2;
	private static double maxradiusseen;
	private AtomicInteger counter = new AtomicInteger();
	//HashT<Long,ObjectReference> totalList = new HashT<Long,ObjectReference>();
	
	
	public int[] res= new int[3];

	public Slot(double [] corner1,double[] corner2,int resolution)
	{
		super(corner1, corner2);
		
		double[] dist = Matrix.subtract(getUpperLeftCornerFront(),getLowerRightCornerBack());

		this.res[0] = (int) Math.min(dist[0]/(maxradiusseen*1.5),resolution);
		if(this.res[0]==0)
		{
			this.res[0] = 1;
		}
		this.res[1] = (int) Math.min(dist[1]/(maxradiusseen*1.5),resolution);
		if(this.res[1]==0)
		{
			this.res[1] = 1;
		}
		this.res[2] = (int) Math.min(dist[2]/(maxradiusseen*1.5),resolution);
		if(this.res[2]==0)
		{
			this.res[2] = 1;
		}
		
		//if(res==0) throw new RuntimeException("resolution of slot null?");
		this.res = res;
		slots = new DirtyLinkedList[res[0]][res[1]][res[2]];
		
		double [] max = getUpperLeftCornerFront();
		double [] min = getLowerRightCornerBack();
		unit[0] = (max[0]-min[0])/res[0];
		unit[1] = (max[1]-min[1])/res[1];
		unit[2] = (max[2]-min[2])/res[2];
		unit2  = Matrix.scalarMult(0.5,unit);
	}


	public int [] coordinatesToIndices(double[] pos)
	{
		int[] index = new int[3];
		double [] max = getUpperLeftCornerFront();
		double [] min = getLowerRightCornerBack();
		index[0] = (int)(res[0]/(max[0]-min[0])*(pos[0]-min[0]));
		index[1] = (int)(res[1]/(max[1]-min[1])*(pos[1]-min[1]));
		index[2] = (int)(res[2]/(max[2]-min[2])*(pos[2]-min[2]));

		index[0] = Math.min(Math.max(0,index[0]),res[0]-1);
		index[1] = Math.min(Math.max(0,index[1]),res[1]-1);
		index[2] = Math.min(Math.max(0,index[2]),res[2]-1);

		return index;
	}
	
	public int [] coordinatesToIndices2(double[] pos)
	{
		int[] index = new int[3];
		double [] max = getUpperLeftCornerFront();
		double [] min = getLowerRightCornerBack();
		index[0] = (int)(res[0]/(max[0]-min[0])*(pos[0]-min[0]));
		index[1] = (int)(res[1]/(max[1]-min[1])*(pos[1]-min[1]));
		index[2] = (int)(res[2]/(max[2]-min[2])*(pos[2]-min[2]));

		return index;
	}
	
	public boolean indexValid(int[] index)
	{
		if(!(index[0]>=0 && index[0]<res[0])) 
			return false; 
		if(!(index[1]>=0 && index[1]<res[1])) 
			return false; 
		if(!(index[2]>=0 && index[2]<res[2])) 
			return false; 
		return true;
	}

	public DirtyLinkedList<ObjectReference> getSlotContent(int [] indices)
	{
		return  slots[indices[0]][indices[1]][indices[2]];
	}
	
	public DirtyLinkedList<ObjectReference> getSlotContent(int x,int y,int z)
	{
		return  slots[x][y][z];
	}


	public boolean doesSlotContain(int [] indices)
	{
		if(slots[indices[0]][indices[1]][indices[2]] ==null)
		{
			return  false;
		}
		return  true;
	}


	private Lock lock(int [] index)
	{
		Lock t;
			
		if(locks==null) locks = new HashT<Integer, Lock>();
		int key= res[1]*res[0]*index[2]+res[0]*index[1]+index[0];
		while((t= locks.get(key))==null)
		{
			synchronized (this) {
				
				if(!locks.containsKey(key))
				{
					locks.put(key, new ReentrantLock());
				}
				
			}
		}
		if(t==null)
		{
			locks.writeLock();
			t = locks.get(key);
			locks.writeUnLock();
		}
		t.lock();
		return t;
	}

	private void unlock(int [] index)
	{
		int key= res[1]*res[0]*index[2]+res[0]*index[1]+index[0];
		Lock temp = locks.get(key);
		if(temp==null)
		{
			locks.writeLock();
			temp = locks.get(key);
			locks.writeUnLock();
		}
		temp.unlock();
	}


	public void insert(final ObjectReference r)
	{
		OptimisticExecuter.exectue(new IToExectue() {
			
			public void execute() {
		
				int[] indices = coordinatesToIndices(r.getPosition());
				boolean  isdirty = dirtyRegion(indices);
				ORR.addHistory(r, "insert to slot"+indices[0]+", "+indices[1]+", "+indices[2]);
				Lock loc = lock(indices);
				if(slots[indices[0]][indices[1]][indices[2]] ==null)
				{
					slots[indices[0]][indices[1]][indices[2]] = new DirtyLinkedList<ObjectReference>();
				}
				slots[indices[0]][indices[1]][indices[2]].add(r);
				counter.incrementAndGet();
				loc.unlock();
		//		ShowConsoleOutput.println("twweee");
				if(!isdirty)
				{
					setRangeDirty(r,Param.MAX_DIAMETER*2);
				}
			}

			@Override
			public boolean check() {
				// TODO Auto-generated method stub
				return contains(r);
			}
		});
		
//		debug_print();
	}

	private boolean dirtyRegion(int[] indices) {
		return true;
//		if(slots[indices[0]][indices[1]][indices[2]] ==null) return false;
//		{
//			return slots[indices[0]][indices[1]][indices[2]].isDirty();
//		}
	}


	public void remove(final ObjectReference r)
	{
	
		OptimisticExecuter.exectue(new IToExectue() {
			
			public void execute() {
				int[] indices = coordinatesToIndices(r.getPosition());
				ORR.addHistory(r, "remove from slot"+indices[0]+", "+indices[1]+", "+indices[2]);
				boolean  isdirty = dirtyRegion(indices);
				Lock loc = lock(indices);
				if(slots[indices[0]][indices[1]][indices[2]] ==null)
				{
					OutD.println("Slot.remove()");
					OutD.println("not found:-(");
				}
				else
				{
					if(slots[indices[0]][indices[1]][indices[2]].remove(r))
					{
						counter.decrementAndGet();
						if(counter.get()<0)
						{
							System.out.println("hmmm");
						}
					}
				}
				loc.unlock();
				if(!isdirty)
				{
					setRangeDirty(r,Param.MAX_DIAMETER*2);
				}
			}

			public boolean check() {
				return !contains(r);
			}
		});
	}
	
	public boolean contains(ObjectReference r)
	{
		boolean t=false;
		int[] indices = coordinatesToIndices(r.getPosition());
		//boolean  isdirty = dirtyRegion(indices);
		Lock loc = lock(indices);
		if(slots[indices[0]][indices[1]][indices[2]] ==null)
		{
			t=false;
		}
		else
		{
			boolean suc=false;
			while(!suc)
			{
				try{
					t= slots[indices[0]][indices[1]][indices[2]].contains(r);
					suc=true;
				}
				catch (ConcurrentModificationException e) {
					
				}
			}
//			if(t==false)
//			{
//				DirtyLinkedList<ObjectReference> temp = slots[indices[0]][indices[1]][indices[2]];
//				int[] lp = findit(r);
//				System.out.println("hmmm");
//			}
		}
		loc.unlock();
		return t;
	}

	public void searchRange(ObjectReference point,double radius, IObjectReferenceSearchVisitor visitor)
	{
		double[] pos2 = point.getPosition();
		maxradiusseen = Math.max(radius,maxradiusseen);
		int [] center = coordinatesToIndices(point.getPosition());
		int [] start = coordinatesToIndices(Matrix.subtract(point.getPosition(), new double []{radius,radius,radius}));
		int [] end = coordinatesToIndices(Matrix.add(point.getPosition(), new double []{radius,radius,radius}));
		int i[] = new int[3];

		double r1 = point.getRadius();
		int k =0;
//		TimeToken t = Timer.start("searchrange");
		for(i[0]=start[0];i[0]<=end[0];i[0]++)
		{
			for(i[1]=start[1];i[1]<=end[1];i[1]++)
			{
				for(i[2]=start[2];i[2]<=end[2];i[2]++)
				{
//					if(visitworth(i))
//					{
						k++;
						if(getSlotContent(i) ==null) continue;
						if(distanceInRange(point.getPosition(), i, radius))
						{
							
							ObjectReference r;
							DirtyLinkedList<ObjectReference> temp = getSlotContent(i);
//							temp.startIteration();
							for(int l=0;l<temp.size();l++)
							{
								r= temp.unlockedGet(l);
								double[] pos1 = r.getPosition();
								double totr = r1+r.getRadius()+2; //2 as margin for having neighours with no effect
								//checkrange
								double d1 = pos1[0]-pos2[0];
//							    if(d1>totr) continue; 
							    double d2 = pos1[1]-pos2[1];
//							    if(d2>totr) continue; 
							    double d3 = pos1[2]-pos2[2];
//							    if(d3>totr) continue;
							    
							    if(d1*d1+d2*d2+d3*d3>totr*totr) continue; //disance
							    if(r.address  == point.address) continue;
//								TimeToken t2 = Timer.start("visit");
								visitor.visit(ORR.I().get(r.address));
//								Timer.stop(t2);
							}
//							temp.stopIteration();
							
						}
					}
//				}
			}
		}
//		if(k>27)
//		{
//			ShowConsoleOutput.println("k="+k);
//		}
//		Timer.stop(t);
		
		
	
	}
	
	
	public void searchRange(double[] point,double radius, DrawSlotGrid s)
	{
		int [] center = coordinatesToIndices(point);
		int [] start = coordinatesToIndices(Matrix.subtract(point, new double []{radius,radius,radius}));
		int [] end = coordinatesToIndices(Matrix.add(point, new double []{radius,radius,radius}));
		int i[] = new int[3];
		
//		ShowConsoleOutput.println((start[0]-end[0])*(start[1]-end[1])*(start[2]-end[2]) +" search size"+radius+" "+unitx);
		for(i[0]=start[0];i[0]<=end[0];i[0]++)
		{
			for(i[1]=start[1];i[1]<=end[1];i[1]++)
			{
				for(i[2]=start[2];i[2]<=end[2];i[2]++)
				{
				//	if(!indexValid(i)) continue;
//					if(visitworth(i))
//					{
//						if(getSlotContent(i) ==null) continue;
						if(distanceInRange(point, i, radius))
						{
							s.drawColoredSlot(this, i[0], i[1],i[2]);
						}
//				}
				}
			}
		}

	}

	private boolean visitworth(int[] i) {
		if(slots[i[0]][i[1]][i[2]] ==null) return  false;
		if(!dirtyRegion(i)) return false;
		return true;
	}

	
	private boolean distanceInRange(double [] c, int [] p,double radius)
	{
		double [] lrc = this.getLowerRightCornerBack();
		double  c0 = lrc[0]+p[0]*unit[0] +unit2[0];
		double  c1 = lrc[1]+p[1]*unit[1] +unit2[1];
		double  c2 = lrc[2]+p[2]*unit[2] +unit2[2];
		double da0 = Math.max(Math.abs(c0-c[0])-unit2[0],0);
		if(da0>radius) return false;
		double da1 = Math.max(Math.abs(c1-c[1])-unit2[1],0);
		if(da1>radius) return false;
		double da2 = Math.max(Math.abs(c2-c[2])-unit2[2],0);
		if(da2>radius) return false;
		return da0*da0 + da1*da1 + da2*da2<radius*radius;
		
	}

	
	public double [] getSlotCenter(int [] p)
	{
		return  Matrix.add(this.getLowerRightCornerBack(), new double[]{p[0]*unit[0] +unit2[0],p[1]*unit[1]+unit2[1],p[2]*unit[2]+unit2[2]});
	}
	
	public Cuboid getSlotCuboid(int [] p)
	{
		double [] low  = Matrix.add(this.getLowerRightCornerBack(), new double[]{p[0]*unit[0],p[1]*unit[1],p[2]*unit[2]});
		double [] uper = Matrix.add(this.getLowerRightCornerBack(), new double[]{p[0]*unit[0] +unit[0],p[1]*unit[1]+unit[1],p[2]*unit[2]+unit[2]});
		return new Cuboid(low,uper);
	}
	
	@Override
	public int count() {
		return counter.get();
	}


	@Override
	public void debug_print() {
		int i[] = new int[3];
		for(i[0]=0;i[0]<res[0];i[0]++)
		{
			for(i[1]=0;i[1]<res[1];i[1]++)
			{
				for(i[2]=0;i[2]<res[2];i[2]++)
				{
					if(doesSlotContain(i))
					{
						OutD.println(getSlotContent(i).size());
					}
				}
			}
		}
				
	}

	
	private void setRangeDirty(ObjectReference point,double radius)
	{
		int [] center = coordinatesToIndices(point.getPosition());
		int [] start = coordinatesToIndices(Matrix.subtract(point.getPosition(), new double []{radius,radius,radius}));
		int [] end = coordinatesToIndices(Matrix.add(point.getPosition(), new double []{radius,radius,radius}));
		int i[] = new int[3];
		
//		ShowConsoleOutput.println((start[0]-end[0])*(start[1]-end[1])*(start[2]-end[2]) +" search size"+radius+" "+unitx);
		for(i[0]=start[0];i[0]<end[0];i[0]++)
		{
			for(i[1]=start[1];i[1]<end[1];i[1]++)
			{
				for(i[2]=start[2];i[2]<end[2];i[2]++)
				{
					if(slots[i[0]][i[1]][i[2]]!=null)
					{
						if(distanceInRange(point.getPosition(), i, radius))
						{
							slots[i[0]][i[1]][i[2]].setDirty();
						}
					}
				}
			}
		}

	}

	@Override
	public void insertNode(ObjectReference closePoint, ObjectReference toins) {
//		ShowConsoleOutput.println("inserting "+toins.address);
		insert(toins);
	}


	@Override
	public ObjectReference move(final ObjectReference ref,final double[] position) {
		OptimisticExecuter.exectue(new IToExectue() {
			
			public void execute() {
				int [] i1=  coordinatesToIndices(ref.getPosition());
				if(slots[i1[0]][i1[1]][i1[2]]==null)
				{
					int [] k =findit(ref);
					OutD.print("so what I find is: "+k[0]+", "+k[1]+","+k[2]);
					OutD.println(" origrefpos: "+ref.getPosition()[0]+", "+ref.getPosition()[1]+","+ref.getPosition()[2]);
				}
				
				int [] i2 = coordinatesToIndices(position);
				remove(ref);
				ORR.addHistory(ref, "move to another  slot from "+i1[0]+", "+i1[1]+", "+i1[2]+" to "+i2[0]+", "+i2[1]+", "+i2[2]);
				ref.setPosition(position);				
				insert(ref);
			}

			@Override
			public boolean check() {
				return contains(ref);
			}
			
		});
		return ref;
	}

	private int[] findit(ObjectReference ref) {
		for(int x=0;x<res[0];x++)
		{
			for(int y=0;y<res[1];y++)
			{
				for(int z=0;z<res[2];z++)
				{
					DirtyLinkedList<ObjectReference> l = this.slots[x][y][z];
					if(l == null) continue;
					for (ObjectReference r : l) {
						if(r.address == ref.address)
						{
							OutD.println(" foundrefpos: "+r.getPosition()[0]+", "+r.getPosition()[1]+","+r.getPosition()[2]);
							return new int[]{x,y,z};
						}
						
					}
				}
			}
		}
		return new int[]{-1,-1,-1};
	}


	private boolean equalIndec(int [] i1,int[] i2)
	{
		
		if(i1[0]==i2[0] && i1[1]==i2[1] && i1[2]==i2[2]) return true;
		return false;
	}

	@Override
	public Iiterator getIterator() {
		return new Iterator(this);
	}


	public int[] getRes() {
		return res;
	}


	@Override
	public void installLocally() {
		int i []= new int[3];
		for(i[0]=0;i[0]<res[0];i[0]++)
		{
			for(i[1]=0;i[1]<res[1];i[1]++)
			{
				for(i[2]=0;i[2]<res[2];i[2]++)
				{
					DirtyLinkedList<ObjectReference> l = this.slots[i[0]][i[1]][i[2]];
					if(l == null) continue;
					for(ObjectReference r:l)
					{
						OutD.println("install localy Slot "+r.address);
						ORR.I().put(r);
					}
				}
			}
		}

	}

	private void  init()
	{
		//slots = new DirtyLinkedList[res[0]][res[1]][res[2]];
		//OutD.println("got it all!");
	}

	private void writeObject(java.io.ObjectOutputStream out) throws  java.io.IOException
	{
		out.defaultWriteObject();
		ObjectOutputStream outputStream = new ObjectOutputStream(out);
		outputStream.writeObject(res);
		outputStream.writeInt(counter.get());
		ArrayList<int[]> nonempty = new ArrayList<int[]>();
		int i []= new int[3];
		for(i[0]=0;i[0]<res[0];i[0]++)
		{
			for(i[1]=0;i[1]<res[1];i[1]++)
			{
				for(i[2]=0;i[2]<res[2];i[2]++)
				{
					DirtyLinkedList<ObjectReference> l = this.slots[i[0]][i[1]][i[2]];
					if(l == null) continue;
					nonempty.add(i.clone());
				}
			}
		}
		outputStream.writeInt(nonempty.size());
		for (int [] k : nonempty) {
			DirtyLinkedList<ObjectReference> l = this.slots[k[0]][k[1]][k[2]];
			outputStream.writeObject(k);
			outputStream.writeObject(l);
		}
		//outputStream.writeObject(slots);
		outputStream.writeObject(unit);

	}
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
	{

		in.defaultReadObject();
		ObjectInputStream inStream = new ObjectInputStream(in);
		res = ((int[]) inStream.readObject());
		counter  = new AtomicInteger();
		int cou =  inStream.readInt();
		if(cou<0)
		{
			System.out.println("whooottttt???");
		}
		counter.set(cou);
		slots = new DirtyLinkedList[res[0]][res[1]][res[2]];
		
		int count = inStream.readInt();
		for (int i =0;i<count;i++) {
			int [] k = (int[]) inStream.readObject();
			this.slots[k[0]][k[1]][k[2]] =  (DirtyLinkedList<ObjectReference>) inStream.readObject();
//			for(ObjectReference r: this.slots[k[0]][k[1]][k[2]])
//			{
//				OutD.println("installing "+r.address);
//				ORR.I().put(r);
//			}
		}
		
		unit = ((double []) inStream.readObject());
		unit2  = Matrix.scalarMult(0.5,unit);
		init();
	}


	@Override
	public boolean shallIBeProcessed(ObjectReference r) {
		// TODO Auto-generated method stub
		int i[] = coordinatesToIndices(r.getPosition());
		return dirtyRegion(i);    
	}


	@Override
	public void DebugCheckAllNodes_afterRemoval() {
		ArrayAccessHashTable nodes = MultiThreadScheduler.getNodesToProcess();
		for(int x=0;x<res[0];x++)
		{
			for(int y=0;y<res[1];y++)
			{
				for(int z=0;z<res[2];z++)
				{
					
					DirtyLinkedList<ObjectReference> l = this.slots[x][y][z];
					if(l == null) continue;
					for (ObjectReference r : l) {
						if(nodes.containsKey(r.address))
						{
							System.out.println("error should not exist anymore!");
							nodes.remove(r.address);
						}
					}
				}
			}
		}
		
	}
}
