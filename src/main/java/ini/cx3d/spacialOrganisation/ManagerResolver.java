/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.spacialOrganisation;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.Matrix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.management.RuntimeErrorException;

public class ManagerResolver implements Serializable {


	
	private AtomicLong partitionNumber = new AtomicLong();
	private HashT<Long, PartitionManager> localmanagers = new HashT<Long, PartitionManager>();
	private HashT<Long,SingleRemotePartitionManager> remotemanagers = new HashT<Long,SingleRemotePartitionManager>();
	private ReadWriteLock managersreadwrite = new ReentrantReadWriteLock();

	
	public static void SetI(ManagerResolver m)
	{
		ManagerResolver.current = m;
	}
	
	private static ManagerResolver current = new ManagerResolver();
	public static ManagerResolver I()
	{
		return current;
	}

	public ManagerResolver()
	{
		current = this;

	}


	public AbstractPartitionManager createInitialPartitionManager(double size)
	{
		return createInitialPartitionManager(new double[]{-size,-size,-size},new double[]{size,size,size});
	}
	
	public AbstractPartitionManager createInitialPartitionManager(double [] upperleftfront, double [] lowerrightback)
	{		
		PartitionAddress add = new PartitionAddress(Hosts.getLocalHost(),upperleftfront,lowerrightback);
		PartitionManager p = new PartitionManager(add);
		AbstractDiffusionNode node = new DiffusionNode(upperleftfront,lowerrightback);
		DiffusionNodeManager.I().addDiffusionNode(node);
		p.setDiffusionnode(node);
		managersreadwrite.writeLock().lock();
		localmanagers.put(add.address,p);
		managersreadwrite.writeLock().unlock();
		if(localmanagers.size()==0) throw new RuntimeException("adding pm failed!");
		AbstractPartitionManager temp = localmanagers.get(add.address);
		return temp;
	}

	public AbstractPartitionManager createPartitionManager(double [] upperleftfront, double [] lowerrightback,long parrentid, int i ,AbstractDiffusionNode n)
	{		
		PartitionAddress add = new PartitionAddress(Hosts.getLocalHost(),parrentid,i,upperleftfront,lowerrightback);
		PartitionManager p = new PartitionManager(add);
		p.setDiffusionnode(n);
		managersreadwrite.writeLock().lock();
		localmanagers.put(add.address,p);
		managersreadwrite.writeLock().unlock();
		AbstractPartitionManager temp = localmanagers.get(add.address);
		return temp;
	}

	public AbstractPartitionManager getByCordinate(double [] coords)
	{   

		AbstractPartitionManager temp=null;
		managersreadwrite.readLock().lock();
		for (AbstractPartitionManager d : localmanagers.values()) {
			if(d.getAddress().containsCoordinates(coords))
			{
				temp = d;
				break;
			}
		}
//		if(temp==null)
//		{
//			ShowConsoleOutput.println("coords"+coords[0]+","+coords[1]+","+coords[2]);
//		}
		if(temp ==null)
		{
			for (AbstractPartitionManager d : remotemanagers.values()) {
				if(d.getAddress().containsCoordinates(coords))
				{
					temp = d;
					break;
				}
			}
		}
		managersreadwrite.readLock().unlock();
		return temp;
	}

	public AbstractPartitionManager resolve(long a) {

		if(localmanagers.containsKey(a)) return localmanagers.get(a);
		return remotemanagers.get(a);
	}
	
	public boolean isLocal(long a) {

		if(localmanagers.containsKey(a))return true;
		return false;
	}
	
	public boolean isRemote(long a) {

		return !isLocal(a);
	}

	public AbstractPartitionManager resolve(PartitionAddress a) {
		if(localmanagers.containsKey(a.address)) return localmanagers.get(a.address);
		return remotemanagers.get(a.address);
	}

	public AbstractPartitionManager local() {

		AbstractPartitionManager pm=null;
		managersreadwrite.readLock().lock();
		for (AbstractPartitionManager d : localmanagers.values()) {
				pm =  d;
		}
		managersreadwrite.readLock().unlock();
		return pm;
	}

	public int getTotalLocalObjectsCount()
	{
		int count = 0;
		managersreadwrite.readLock().lock();
		for (AbstractPartitionManager d : localmanagers.values()) {
			count+= d.count();
		}
		managersreadwrite.readLock().unlock();
		return count;
	}

	public boolean checkNSplit(int maxcount)
	{
		boolean changedsetup=false;
		ArrayList<PartitionManager> pms= new ArrayList<PartitionManager>(localmanagers.values());
		for (PartitionManager d : pms) {
		
			if(d.count()>maxcount)
			{
				for (PartitionManager e : pms) {
					e.split();
				}
				changedsetup = true;
			}
		}
		return changedsetup;
	}



	public void remove(AbstractPartitionManager partitionManager) {
		managersreadwrite.writeLock().lock();
		localmanagers.remove(partitionManager.getAddress().address);
		managersreadwrite.writeLock().unlock();

	}

	public void addPartitionManager(PartitionManager partitionManager)
	{
		managersreadwrite.writeLock().lock();
		localmanagers.put(partitionManager.getAddress().address, partitionManager);
		managersreadwrite.writeLock().unlock();
		partitionManager.installLocaly();
		
	}
	
	public void addPartitionManager(SingleRemotePartitionManager partitionManager)
	{
		partitionManager.installLocaly();
		managersreadwrite.writeLock().lock();
		remotemanagers.put(partitionManager.getAddress().address, partitionManager);
		managersreadwrite.writeLock().unlock();
	}


	public boolean establishDiffusionGrid(int gridresolution)
	{
		if(DiffusionNodeManager.I().establishDiffusionGrid(gridresolution))
		{
			return true;
		}
		return false;
	}


	public ArrayList<PartitionManager> getLocalPartitions()
	{
		managersreadwrite.readLock().lock();
		ArrayList<PartitionManager> pms = new ArrayList<PartitionManager>(localmanagers.values());
		managersreadwrite.readLock().unlock();
		return pms;
	}
	
	public ArrayList<AbstractPartitionManager> getAllPartitions()
	{
		managersreadwrite.readLock().lock();
		ArrayList<AbstractPartitionManager> pms = new ArrayList<AbstractPartitionManager>(localmanagers.values());
		pms.addAll(remotemanagers.values());
		managersreadwrite.readLock().unlock();
		return pms;
	}

	public ArrayList<PartitionAddress> getLocalPartitionAddresses()
	{
		ArrayList<PartitionAddress> pta = new ArrayList<PartitionAddress>();
		managersreadwrite.readLock().lock();
		for (AbstractPartitionManager d : localmanagers.values()) {
			pta.add(d.getAddress());
		}
		managersreadwrite.readLock().unlock();
		return pta;
	}


	public void clearCashes()
	{
		managersreadwrite.readLock().lock();
		for (PartitionManager d : localmanagers.values()) {
			d.clearCash();
		}
//		for (RemotePartitionManager d : remotemanagers.values()) {
//			d.clearCash();
//		}
		managersreadwrite.readLock().unlock();
	}



	


	public Collection<SingleRemotePartitionManager> getRemotePartitions() {
		return remotemanagers.values();
	}

	
	public boolean containsHostManager(String host)
	{
		long code = (host.hashCode()*10000000-1)%997;
		return remotemanagers.containsKey(code);
	}
	
	public SingleRemotePartitionManager getHostManager(String host)
	{
		managersreadwrite.readLock().lock();
		long code = (host.hashCode()*10000000-1)%997;
		if(!remotemanagers.containsKey(code))
		{
			SingleRemotePartitionManager spm = new SingleRemotePartitionManager(host);
			OutD.println(spm.getAddress().address-code+"generated address vs real"+(host.hashCode()*10000000-1)+"host "+host);
			remotemanagers.put(spm.getAddress().address, spm);
		}
		managersreadwrite.readLock().unlock();
		return remotemanagers.get(code);
	}

	public ArrayList<PartitionAddress> getAllPartitionAddresses() {
		ArrayList<PartitionAddress> pta = new ArrayList<PartitionAddress>();
		managersreadwrite.readLock().lock();
		for (AbstractPartitionManager d : localmanagers.values()) {
			pta.add(d.getAddress());
		}
		managersreadwrite.readLock().unlock();
		return pta;
	}

	

	public AbstractPartitionManager resolve(ObjectReference re) {

		AbstractPartitionManager pm=null;
		pm = resolve(re.partitionId);
		int i = 1000;
		while(pm==null)
		{
			i--;
			if(i==0)
			{
				SimulationState  st = SimulationState.getLocal();
				throw new PMNotFoundException();
			}
			re = ORR.I().get(re.address);
			pm = resolve(re.partitionId);
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return pm;
	}



	public boolean containsLocalPartition(long partitionId) {
		boolean b = this.localmanagers.containsKey(partitionId);
		return b; 
	}


	public void debug_print() {
		OutD.println("ManagerResolver.print()");
		managersreadwrite.readLock().lock();
		for (AbstractPartitionManager d : localmanagers.values()) {
			OutD.println(d.getAddress().address+" :: "+d);
		}
		
		managersreadwrite.readLock().unlock();
	}

	public void debug_showCurrentIDs() {
		for (AbstractPartitionManager p : this.localmanagers.values()) {
			OutD.println("Manager id:"+p.getAddress().address);
		}
		
	}
	public void findCenter()
	{
		double [] temp=new double[]{0,0,0};
		for(PartitionAddress pm:getLocalPartitionAddresses())
		{
			temp = Matrix.add(temp,pm.getCenter());
		}
		SimulationState.getLocal().center = Matrix.scalarMult(1.0/getLocalPartitionAddresses().size(),temp);
		
	}
	

}
