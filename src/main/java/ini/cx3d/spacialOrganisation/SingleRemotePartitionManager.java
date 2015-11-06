package ini.cx3d.spacialOrganisation;


import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.gui.spacialOrganisation.WhatTheHellAreYouWaitingFor;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.physics.PhysicalBond;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionAddress;
import ini.cx3d.physics.diffusion.DiffusionMargin;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.spacialOrganisation.commands.CommandCollector;
import ini.cx3d.spacialOrganisation.commands.RemoteMarginBox;
import ini.cx3d.spacialOrganisation.commands.Remote_InsertBulk;
import ini.cx3d.spacialOrganisation.slot.Slot;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.VecT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;




public class SingleRemotePartitionManager extends AbstractPartitionManager {



	private VecT<PhysicalNode> toinsert = new VecT<PhysicalNode>();
	private VecT<PhysicalBond> physicalBondToinsert = new VecT<PhysicalBond>();
	public HashT<Double,Slot> remoteNodesStaged= new HashT<Double, Slot>();
	private AtomicLong idgenerator = new AtomicLong();
	private Slot remoteNodes;
	private PartitionAddress address;
	private double laststage=0;

	private transient ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private HashT<Long, PhysicalNode> register;
	public HashT<Double,HashT<Long, PhysicalNode>> registerStaged= new HashT<Double, HashT<Long, PhysicalNode>>();

	public boolean prefetch()
	{
		double stage = SimulationState.getLocal().stagecounter;
		setPos();
		return setStage(stage);
	}
	
	private void setPos()
	{
		double [] c = SimulationState.getLocal().center;
		//double[] min = new double [] {-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE};
		//double[] max = new double [] {Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};
		double size = 2000;
		double[] min = new double [] {-size,-size,-size};
		double[] max = new double [] {size,size,size};
		if(address.getHost().equals(Hosts.getNextHost()))
		{
			max[0]= c[0];
		}
		else if(address.getHost().equals(Hosts.getPrevHost()))
		{
			min[0] = c[0];
		}
		address.setCorners(min, max);
	}


	public SingleRemotePartitionManager(String host) {
		OutD.println("creating manager remote");
		double[] min = new double [] {-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE};
		double[] max = new double [] {Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};
		address = new  PartitionAddress(host,(host.hashCode()*10000000-1)%997,min,max);
	}

	public ObjectReference generateObjectAddress(double [] coordinates)
	{
		ObjectReference temp = new ObjectReference((address.address*1000000)+ idgenerator.incrementAndGet(),coordinates,address);
		//OutD.println("generatedObjectAdress "+temp.address,"yellow");
		if(ORR.I().contains(temp.address)) 
		{
			System.out.println("this must be null address already exists!");
		}
		ORR.I().put(temp);
		return temp;
	}

	public void insert(ObjectReference closepoint,ObjectReference toinsert,PhysicalNode n)
	{
		
		if(n.getSoNode().getObjectRef().partitionId == address.address)
		{
			OutD.println("this should not be moved!"+n.getSoNode().getObjectRef().partitionId);
		}
		
		n.removeLocally();
		n.getSoNode().remove();
	
		this.toinsert.add(n);

		addToMargin(n,toinsert);
		toinsert.setPartitionId(getAddress());
		//OutD.println("SingleRemote"+toinsert.address,"green");
		ORR.I().put(toinsert);

	}

	public void addPysicalBond(PhysicalBond p)
	{
		physicalBondToinsert.add(p);
	}

	public void searchNeigbours(ObjectReference middle,double radius,IObjectReferenceSearchVisitor visitor)
	{
		//throw new RuntimeException("searching only for local nodes!");
		OutD.println("searching only for local nodes! doing it anyway though should not happen!!! tbh "+middle.address);
	}



	public void getAllInRangeOnThisManager(ObjectReference middle,double radius,IObjectReferenceSearchVisitor visitor)
	{	
//		TimeToken t3 = Timer.start("searchremoteLock*");
		getLock().readLock().lock();
//		TimeToken t2 = Timer.start("searchremote*");
		if(remoteNodes != null)
		{
			remoteNodes.searchRange(middle, radius,visitor);
		}
//		Timer.stop(t2);
		getLock().readLock().unlock();
//		Timer.stop(t3);
	}

	@Override
	public void debug_print() {

	}

	@Override
	public int count() {
		return -1;
	}

	@Override
	public PartitionAddress getAddress() {
		return address;
	}

	@Override
	public ObjectReference move(ObjectReference ref, double[] new_coordinates) {

		OutD.println("remote move try "+ref.address+" "+ref.partitionId);
		return ref;
	}

	public String getHost()
	{
		return address.getHost();
	}

	@Override
	public void remove(ObjectReference ref) {

		OutD.println("remote REmove not possible doing it anyways");
	}

	@Override
	public VecT<PartitionManager> split() {
		throw new RuntimeException("this is not alowed remotley!");
	}


	@Override
	public PhysicalNode getPhysicalNode(ObjectReference ref) {

		return register.get(ref.address);
	}

	@Override
	public void installLocaly() {

	}

	@Override
	public void removeLocaly() {
	}

	@Override
	public boolean establishDiffusionGrid(int maxcount) {
		throw new RuntimeException("this is not alowed remotley!");
	}

	@Override
	public AbstractDiffusionNode getDiffusionNode() {
		throw new RuntimeException("this is not alowed remotley!");
	}

	@Override
	public void changeAddress(ArrayList<DiffusionAddress> adds) {

	}

	public void applyCalculations()
	{
			//			for (PhysicalNode n: toinsert) {
			//				n.removeLocally();
			//			}
			Remote_InsertBulk c = new Remote_InsertBulk<Boolean>(this.address, toinsert,physicalBondToinsert,SimulationState.getLocal().stagecounter);

			String s = address.getHost();
			WhatTheHellAreYouWaitingFor.waitfor(s+" applycalc", "generating Bulk for "+s+" "+SimulationState.getLocal().stagecounter);
			CommandCollector.get(s).add(c);
					
			toinsert = new VecT<PhysicalNode>();
			physicalBondToinsert  = new VecT<PhysicalBond>();
	}

	public void clearCash()
	{

	}



	public boolean containsOtherPartitons()
	{
		return false;
	}

	@Override
	public boolean shallIBeProcessed(ObjectReference objectRef) {
		return true;
	}



	public void remotePhysicalNodeClear() {
	}

	public Collection<PhysicalNode> getNodes()
	{
		if(register==null)
		{
			register = new HashT<Long, PhysicalNode>();
		}
		return register.values();
	}

	public void setRemoteNodes(Slot remotenodes,HashT<Long, PhysicalNode> register,	double stage) {
		//		ShowConsoleOutput.println("I arrived!!!!"+stage+" "+this);
		synchronized (this) {
			if(remotenodes==null) 
			{
				System.out.println("whoot it was null");
			}
			remoteNodesStaged.put(stage,remotenodes);
			if(register==null) 
			{
				System.out.println("whoot it was null");
			}
			registerStaged.put(stage, register);
		}
	}

	public String toString()
	{
		return this.address.getHost()+" remotehost";

	}

	public void update(double stage) {
		OutD.println("set this stage"+stage+"");
		synchronized (this) {
			this.remoteNodes =remoteNodesStaged.get(stage);
			if(remoteNodes==null) 
			{
				System.out.println("whoot it was null");
			}
			remoteNodesStaged.remove(stage);
			this.register =registerStaged.get(stage);
			if(register==null) 
			{
				System.out.println("whoot it was null");
			}
			register.remove(stage);
		}
	}

	public RemoteMarginBox<Boolean> getMarginBox()
	{
		RemoteMarginBox<Boolean> r = new RemoteMarginBox<Boolean>(register,laststage,address.getHost());
		r.prepareSend();
		return r;
	}
	
	public DiffusionMargin getDiffusionMargin()
	{
		DiffusionMargin r = new DiffusionMargin();
		r.tobesent = DiffusionNodeManager.I().getAllLeafesOfHost(address.getHost());
		r.host = address.getHost();
		r.prepareSend();
		return r;
	}

	public boolean setStage(double stage) {
		String key = this.toString();
		synchronized (this) {
			
		
			if(!remoteNodesStaged.containsKey(stage))
			{	
				String s="waiting for"+stage+" "+this;
				for (Double d : remoteNodesStaged.keySet()) {
					s+="stage "+d +"countained!";
				}
				WhatTheHellAreYouWaitingFor.waitfor(key,s);
				return false;
			}
		}
		synchronized (this) {
			remoteNodes = remoteNodesStaged.get(stage);
			if(remoteNodes==null) 
			{
				System.out.println("whoot it was null");
			}
			remoteNodesStaged.remove(stage);
			
			register = registerStaged.get(stage);
			if(register==null) 
			{
				System.out.println("whoot it was null");
			}
			registerStaged.remove(stage);
		}
		
		WhatTheHellAreYouWaitingFor.notAnymore(key);
		laststage = stage;
		return true;

	}

	public void addToMargin(PhysicalNode n,ObjectReference r) {
		getLock().writeLock().lock();
		remoteNodes.insert(r);
		register.put(r.address, n);
		getLock().writeLock().unlock();
	}

	private ReadWriteLock getLock()
	{
		if(lock ==null)
		{
			synchronized (this) {
				if(lock==null)
				{
					lock = new ReentrantReadWriteLock();
				}
			}
			
		}
		return lock;
	}

	public void setMin(double[] max, double[] min) {
//		address.setCorners(max, min);
	}

}
