package ini.cx3d.spacialOrganisation;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;
import ini.cx3d.parallelization.ObjectHandler.commands.AbstractComplexCommand;
import ini.cx3d.parallelization.communication.ConnectionManager;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.parallelization.communication.SentRecivedPackage;
import ini.cx3d.parallelization.communication.SimpleResponse;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffReg;
import ini.cx3d.physics.diffusion.DiffusionAddress;
import ini.cx3d.physics.diffusion.DiffusionMargin;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.simulation.commands.PauseCommand;
import ini.cx3d.simulation.commands.ResumeCommand;
import ini.cx3d.simulation.commands.SpreadStage;
import ini.cx3d.spacialOrganisation.commands.CommandCollector;
import ini.cx3d.spacialOrganisation.commands.RemoteMarginBox;
import ini.cx3d.spacialOrganisation.commands.ShipCommand;
import ini.cx3d.spacialOrganisation.slot.Slot;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.HashT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.TreeSet;



public class ManagedObjectBalancer {




	public static boolean StartBalanceProtocol()
	{
		ThreadHandler.WaitForStoping(); 
		if (MultiThreadScheduler.globalmin > SimulationState.getTotalGlobalNodeCount()) return false;
		if (ManagerResolver.I().getLocalPartitions().size()<2) return false;
		String h = getMostBoredHost();
		if(h==null) return false;
		reBalance(h,h.equals(Hosts.getNextHost()));
		return true;
	}


	


	private static void reBalance(String mostBoredHost,boolean next) {
		SimulationState s = SimulationState.getRemote(mostBoredHost);
		SimulationState local = SimulationState.getLocal();

		ArrayList<PartitionManager> tobesent = new ArrayList<PartitionManager>();

		TreeSet<Xcut> sortedPMs = getOrganizedPartitons(next);
		if(sortedPMs.size()<2) return;
		if(s==null || s.totalObjectCount==0)
		{
			double tbt= 0;
			Xcut[] ps = new Xcut[sortedPMs.size()];
			sortedPMs.toArray(ps);
			int i =0;
			while(tbt<local.totalObjectCount*0.3)
			{
				if(sortedPMs.size()-i<2)
					break;
				Xcut c = ps[i];
				i++;
				for (PartitionManager part : c.pmas) {

					tobesent.add(part);
					tbt+=part.count();
				}
			}
		}
		else
		{

			double maxtransfairable = (Math.abs(s.totalObjectCount-local.totalObjectCount));
			//	double processingtime = (local.avproessingTime_in_ms-s.avproessingTime_in_ms)/2;
			double loc = local.avproessingTime_in_ms;
			double rem = s.avproessingTime_in_ms;
			double processingtime = 1/loc*(loc-rem);
			double number_transfer = maxtransfairable*processingtime/2;
			OutD.println("will transfer"+number_transfer+" objects to "+mostBoredHost);
			double tbt= 0;
			Xcut[] ps = new Xcut[sortedPMs.size()];
			sortedPMs.toArray(ps);
			int i =0;
			while(tbt<number_transfer)
			{
				if(sortedPMs.size()-i<2) 
					break;
				Xcut c = ps[i];
				i++;
				for (PartitionManager part : c.pmas) {

					tobesent.add(part);
					tbt+=part.count();
				}
			}
		}

		Hosts.avtivateHost(mostBoredHost);


		DiffusionNodeManager.I().checkNeighbours();
		for (PartitionManager partitionManager : ManagerResolver.I().getLocalPartitions()) {
			partitionManager.checkDiffusionHirarchie();
		}

		generateAndSendShipCommand(tobesent,mostBoredHost);
		
		//ManagerResolver.I().getHostManager(mostBoredHost);
		//	updateOtherHostsWithRemote();
	//	command.remoteExecuteAnswer(mostBoredHost);

	}

	public static void updateOtherHostsWithRemote()
	{
		MultiThreadScheduler.execute_GenMargins_NonParalell();
		MultiThreadScheduler.execute_genMarginBox();
		for (String host : Hosts.getHosts()) {
			if(!Hosts.isActive(host)) continue;
			OutD.println("sending to "+host);
			CommandCollector.get(host).add((new SpreadStage(SimulationState.getLocal().getCopy())));
			CommandCollector.get(host).margin.setUpdateRemoteMargins(true);
		}
		CommandCollector.execute();
	}


	public static void generateAndSendShipCommand(ArrayList<PartitionManager> tobesent,String host)
	{
		ArrayList<DiffusionAddress> adds = new ArrayList<DiffusionAddress>();
		ArrayList<AbstractDiffusionNode> diffNodesToShip = new ArrayList<AbstractDiffusionNode>();
		for (PartitionManager partitionManager : ManagerResolver.I().getLocalPartitions()) {
			partitionManager.checkDiffusionHirarchie();
		}
		for (PartitionManager pm : tobesent) {
			pm.checkDiffusionHirarchie();
			try
			{
				diffNodesToShip.addAll(pm.getDiffusionnode().getAllSubnodes());
			}
			catch (NullPointerException e) {
				pm.checkDiffusionHirarchie();
			}
			adds.addAll(pm.getAllDiffusionAdresses());

			pm.removeLocaly();
			pm.getAddress().setHost(host);
		}
		

		// Diffusion
		for (AbstractDiffusionNode i : diffNodesToShip) {
			DiffusionNodeManager.I().removeDiffusionNode(i);
		}

		for (DiffusionAddress diffusionAddress : adds) {
			diffusionAddress.setHost(host);
		}

		for (AbstractDiffusionNode i : diffNodesToShip) {
			DiffReg.I().put(i.getAddress());
		}
		DiffusionNodeManager.I().applyAddressChanges(adds);

		ArrayList<DiffusionMargin> remotediffusion = new ArrayList<DiffusionMargin>();
		HashT<String, DiffusionMargin> remotetemp = DiffusionNodeManager.I().gnerateDiffusionMargin(host,diffNodesToShip);
		for(String hst : remotetemp.keySet())
		{
			DiffusionMargin t = remotetemp.get(hst);
			t.prepareSend();
			remotediffusion.add(t);
		}


		//end diffusion


		for (PartitionManager p : tobesent) {
			//
			p.address.setHost(host);

		}
		
		for (PartitionManager p : ManagerResolver.I().getLocalPartitions()) {
			p.setDependeciesOnHosts();
		}
	
		for (PartitionManager p : tobesent) {
			p.setDependeciesOnHosts();
		}
		for (PartitionManager p : tobesent) {
			p.removeLocaly();
			ManagerResolver.I().remove(p);
			p.DebugCheckallNodes();
		}
		ManagedObjectBalancer.DebugCheckAllReferencesLocally();
		HashT<String,HashT<Long,PhysicalNode>> sendToRemote1 = new HashT<String, HashT<Long,PhysicalNode>>();
		for (PartitionManager p : tobesent) {
			p.generateSendToRemote(sendToRemote1);
		}
	
		HashT<Long, PhysicalNode> nodesForLocalMargin = sendToRemote1.get(Hosts.getLocalHost());
		String s = Hosts.getLocalHost();
		nodesForLocalMargin = sendToRemote1.get(s);
		if(nodesForLocalMargin==null) nodesForLocalMargin = new HashT<Long, PhysicalNode>();
		
	

		HashT<String,HashT<Long,PhysicalNode>> sendToRemote2 = new HashT<String, HashT<Long,PhysicalNode>>();
		sendToRemote2.put(host, new HashT<Long, PhysicalNode>());
		for (PartitionManager p : ManagerResolver.I().getLocalPartitions()) {
			p.generateSendToRemote(sendToRemote2);
		}

		ArrayList<RemoteMarginBox<?>> remoteboxes = new ArrayList<RemoteMarginBox<?>>();

		RemoteMarginBox<Boolean> rMB = new RemoteMarginBox<Boolean>(sendToRemote2.get(host),SimulationState.getLocal().stagecounter,Hosts.getLocalHost());
		rMB.prepareSend();
		remoteboxes.add(rMB);


		ManagerResolver.I().getHostManager(host);
		ShipCommand m = new ShipCommand(tobesent,diffNodesToShip,remotediffusion,remoteboxes);
		m.remoteExecuteAnswer(host);
		createSPM(nodesForLocalMargin,host);
	
	}


	private static void createSPM(HashT<Long, PhysicalNode> nodesForLocalMargin,String host)
	{
		double [] max = new double[] {-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE};
		double [] min = new double[] {Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};
		SingleRemotePartitionManager spm = ManagerResolver.I().getHostManager(host);
		for(PhysicalNode n:nodesForLocalMargin.values())
		{
			ObjectReference ref = n.getSoNode().getObjectRef();
			max = getMax(max,ref.getPosition());
			min = getMin(min,ref.getPosition());
			ref.partitionId = spm.getAddress().address;
			ORR.I().put(ref);
		}
		
		spm.setMin(max,min);
		Slot slot = new Slot(min,max, 30);
		for(PhysicalNode n:nodesForLocalMargin.values())
		{
			slot.insert(n.getSoNode().getObjectRef());
		}
		double stage = SimulationState.getLocal().stagecounter;
		spm.setRemoteNodes(slot,nodesForLocalMargin,stage);
		spm.update(stage);

	}

	private static  double[] getMin(double[] a, double[] b) {
		double [] temp = new double[3]; 
		temp[0]  = Math.min(a[0], b[0]);
		temp[1]  = Math.min(a[1], b[1]);
		temp[2]  = Math.min(a[2], b[2]);
		return temp;
	}


	protected static double[] getMax(double[] a, double[] b) {
		double [] temp = new double[3]; 
		temp[0]  = Math.max(a[0], b[0]);
		temp[1]  = Math.max(a[1], b[1]);
		temp[2]  = Math.max(a[2], b[2]);
		return temp;
	}

	private static double max_waittime = 0;

	private static String getMostBoredHost()
	{
		String minhost = null;
		double min = SimulationState.getLocal().avproessingTime_in_ms;
		double max = SimulationState.getLocal().avproessingTime_in_ms;
		for (String host : Hosts.getHosts()) {
			double r;
			if(!Hosts.isActive(host))
			{
				r = 0;
			}
			else
			{
				r	= SimulationState.getRemote(host).avproessingTime_in_ms;
			}
			
			if(r<min)
			{
				min =r;
				minhost=host;
			}
			if(r>max)
			{
				max=r;
			}
		}
		double diff =max-min;
		if(diff<MultiThreadScheduler.diffWatingTime) return null;
		
		return minhost;
	
		
	}
	
	private static String getMostBoredHost2()
	{
		max_waittime= 0;
		double abs_max = 0;
		double l = SimulationState.getLocal().avproessingTime_in_ms;
		String rhost = null;

		for (String host : Hosts.getHosts()) {
			double r;  
			if(!Hosts.isActive(host))
			{
				r = 0;
			}
			else
			{
				r	= SimulationState.getRemote(host).avproessingTime_in_ms;
			}
			double diff =l-r;
			if(diff<MultiThreadScheduler.diffWatingTime) continue;
			if(diff > max_waittime) 
			{
				max_waittime = diff;
				rhost = host;
			}
			if(Math.abs(diff) > abs_max)
			{
				abs_max = Math.abs(diff);
			}
		}
		return rhost;
	}






	private static boolean WaitForAllToStop() {
		ThreadHandler.WaitForStoping();
		ArrayList<SentRecivedPackage> packages= new ArrayList<SentRecivedPackage>();
		packages.add(ConnectionManager.getSentAndRecived());
		for (String host : Hosts.getHosts()) {
			packages.add((new ManagedObjectBalance_SentRecivedStats()).remoteExecuteAnswer(host));
		}
		for (SentRecivedPackage i : packages) {
			for (SentRecivedPackage j : packages) {
				if(!i.CheckConsitency(i)) return false;
			}	
		}
		return true;
	}
	private static ArrayList<String> hosts;
	public static void pauseAll() {	
		while(!WaitForAllToStop())
		{
			Thread.yield();
		}
		//	SpaceNodeFacade.pauselock.writeLock().lock();
		hosts = Hosts.getHosts();
		for (String host : hosts) {
			new PauseCommand().remoteExecuteAnswer(host);
		}
		while(!WaitForAllToStop())
		{
			Thread.yield();
		}
	}

	public static void resumeAll() {
		//	SpaceNodeFacade.pauselock.writeLock().unlock();
		for (String host : hosts) {
			new ResumeCommand().remoteExecuteAnswer(host);
		}
	}




	private static TreeSet<Xcut> getOrganizedPartitons(final boolean next)
	{
		TreeSet<Xcut> m = new TreeSet<Xcut>(
				new Comparator<Xcut>()
				{
					public int compare(Xcut o1, Xcut o2) {
						if(next)
						{
							if(o1.x>o2.x) return 1;
							else return -1;
						}
						else
						{
							if(o1.x>o2.x) return -1;
							else return 1;
						}

					}
				}

		);

		Hashtable<Double,Xcut> putter = new Hashtable<Double, Xcut>();  


		for (PartitionManager pm : ManagerResolver.I().getLocalPartitions()) {
			double x = pm.address.getCenter()[0];
			if(!putter.containsKey(x))
			{
				Xcut l =  new Xcut();
				l.x = x;
				m.add(l);
				putter.put(x,l);

			}
			putter.get(x).pmas.add(pm);
		}
		return m;
	}
	
	public static void DebugCheckAllReferencesLocally() {
		
			ArrayAccessHashTable nodes = MultiThreadScheduler.getNodesToProcess();
			for(int i = 0;i<nodes.size();i++)
			{
				try{
					PhysicalObject node = (PhysicalObject) nodes.getIfTobeProcessed(i);
					if(node==null) continue;
					node.getSoNode().resolveTest();
				}
				catch (Exception e) {
					System.out.println("error reached");
				}
			}
		
		
	}


}


class ManagedObjectBalance_SentRecivedStats extends AbstractComplexCommand<SentRecivedPackage>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7738503491739565888L;

	public boolean apply()
	{
		//		if (CommandManager.getExecutionCount() >1)
		//		{
		//			return true;
		//		}
		send(new SimpleResponse<SentRecivedPackage>(mailboxID,ConnectionManager.getSentAndRecived()));
		return false;
	}



}