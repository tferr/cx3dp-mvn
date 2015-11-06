
package ini.cx3d.simulation;


import ini.cx3d.Param;
import ini.cx3d.biology.Cell;
import ini.cx3d.biology.NeuriteElement;
import ini.cx3d.biology.SomaElement;
import ini.cx3d.gui.MonitoringGui;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.gui.simulation.ShowObjectCount;
import ini.cx3d.gui.spacialOrganisation.WhatTheHellAreYouWaitingFor;
import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;
import ini.cx3d.parallelization.ObjectHandler.commands.AbstractComplexCommand;
import ini.cx3d.parallelization.ObjectHandler.commands.CommandManager;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.simulation.commands.GlobalObjectCounter;
import ini.cx3d.simulation.commands.RequestStage;
import ini.cx3d.simulation.commands.SpreadStage;
import ini.cx3d.spacialOrganisation.InsertionHandler;
import ini.cx3d.spacialOrganisation.ManagedObjectBalancer;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.PartitionAddress;
import ini.cx3d.spacialOrganisation.PartitionManager;
import ini.cx3d.spacialOrganisation.SingleRemotePartitionManager;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;
import ini.cx3d.spacialOrganisation.commands.CommandCollector;
import ini.cx3d.spacialOrganisation.commands.RemoteMarginBox;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.TimeToken;
import ini.cx3d.utilities.Timer;
import ini.cx3d.utilities.VecT;
import ini.cx3d.utilities.export.IExporter;
import ini.cx3d.utilities.export.rendering.ImageExport;
import ini.cx3d.utilities.video.VideoRecorder;

import java.util.ArrayList;

/**
 * This class contains static methods to loop through all the "runnable" CX3D objects
 * stored in a list in ECM to call their run() method. As far as physics is concerned,
 * the call to the run() method is only made if the object has some of it's state variables
 * being modified, or is in a situation where it might occur. 
 * @author rjd & fredericzubler
 *
 *
 */
public class MultiThreadScheduler {

	//protected static AtomicInteger barrier = new AtomicInteger();
	/* static counter, needed in case where we want to make regular snapshots.*/

	protected static int inter_snapshot_time_steps = 30;
	public static double diffWatingTime = 200;
	public static int gridresolution =   100000;
	public static int maxnodesperPM =    2000;
	public static int globalmin= 100;

	public static boolean active = true;

	/* if false, the physics is not computed......*/
	public static boolean runPhyics = true;
	public static boolean runExtracellularDiffusion = true;
	public static boolean runIntracelularDiffusion= false;
	public static int buffsize=1024*1024*10;	
	public static int slotresolution= 30;

	public static int videoSnapshotIntervall = 1;


	public static VideoRecorder videorec;

	public static int insaneounter =0;


	private static int runs=100;
	public static boolean printCurrentECMTime = false;
	protected static boolean printCurrentStep = false;
	/** Runs all the CX3D elements for one time step, and pauses for a few ms.
	 * @param pauseTime the pause time in milliseconds.
	 */
	public static void runEveryBodyOnce(int pauseTime){
		ECM.pause(pauseTime);
		simulateOneStep();
	}


	public static ArrayAccessHashTable nodes;
	/** Runs all the CX3D runnable objects for one time step.*/
	public static String savename="currentsave";

	/** Runs all the CX3D runnable objects for one time step.*/
	public static void simulateOneStep(){

		SimulationState.getLocal();
		Timer.startRound();

		//Thread.yield();
		execute_output();

		TimeToken noexp =  Timer.start("tot_time_noexp");
		TimeToken getnodes = Timer.start("getnodes");
		nodes = getNodesToProcess();

		for (PartitionManager partitionManager : ManagerResolver.I().getLocalPartitions()) {
			if(runExtracellularDiffusion)
			{
				partitionManager.checkDiffusionHirarchie();
			}
			if(partitionManager.searchpartitions==null)
			{
				partitionManager.gatherImportantLocalNeighbours();
			}
		}
		execute_ObjectCounting();
		Timer.stop(getnodes);
		
		if(Hosts.getNextActive() || Hosts.getPrevActive() )
		{
			TimeToken fetchmargin =  Timer.start("genmarg");
			//DiffusionNodeManager.I().clearRemoteDiffusionNodes();
			//DiffusionNodeManager.I().generateDiffusionMargin();
		
			execute_GenMargins();
			ThreadHandler.WaitForStoping();
			TimeToken diggmarg =  Timer.start("genmarg_diff");
			DiffusionNodeManager.I().generateDiffusionMargin();
			execute_genMarginBox();
			Timer.stop(diggmarg);

			ThreadHandler.WaitForStoping();
			Timer.stop(fetchmargin);
			TimeToken commandsetup =  Timer.start("commandsetup");
			for (String host : CommandCollector.commands.keySet()) {

				WhatTheHellAreYouWaitingFor.waitfor(host+" sending","sending to "+host+" : "+ CommandCollector.get(host).margin.stage);

				CommandCollector.get(host).add((new SpreadStage(SimulationState.getLocal().getCopy())));
			}

			//		ManagerResolver.I().generateActiveHosts();
			CommandCollector.execute();

			//		Timer.start("send diffusion margins");

			//		Timer.stop("send diffusion margins");

			Timer.stop(commandsetup);
		}
		getnodes = Timer.start("getnodes");
		nodes = getNodesToProcess();
		Timer.stop(getnodes);


		TimeToken othermachines = Timer.start("othermachines1");

		execute_checkforArival();
		ThreadHandler.WaitForStoping();

		Timer.stop(othermachines);



		TimeToken prefetch = Timer.start("prefetch");
		
		execute_prefetch();
		ThreadHandler.WaitForStoping();
		Timer.stop(prefetch);
		
		
		//	DiffusionNodeManager.I().debug_checkForRemoteNeighbourConsistensy();
		
		//nodes = getNodesToBioProcess();
		TimeToken bio = Timer.start("bio");
		execute_biologymodules();
		ThreadHandler.WaitForStoping();
		Timer.stop(bio);

		TimeToken physics = Timer.start("physics");
		execute_externalDiffusion();
		if(runPhyics){

			execute_Physics();
			//execute_spherePhysics();
		}
		ThreadHandler.WaitForStoping();
		Timer.stop(physics);

		//		TimeToken allinone = Timer.start("allinone");
		//		execute_allInOne();
		//		execute_externalDiffusion();
		//		ThreadHandler.WaitForStoping();
		//		Timer.stop(allinone);

		TimeToken apply_changes = Timer.start("apply");
		execute_ApplyChanges();
		ThreadHandler.WaitForStoping();
		Timer.stop(apply_changes);
		if(Hosts.getNextActive() || Hosts.getPrevActive() )
		{
			TimeToken apply_remote_changes = Timer.start("apply remote changes");
			applyRemoteChanges();
			ThreadHandler.WaitForStoping();
			CommandCollector.execute();
			Timer.stop(apply_remote_changes);
		}


		othermachines = Timer.start("othermachines2");
		InsertionHandler.StartArivalCheckers();
		ThreadHandler.WaitForStoping();
		Timer.stop(othermachines);


//		TimeToken cashclearing = Timer.start("cashclearing");
//		ManagerResolver.I().clearCashes();
		SimulationState.getLocal().cycle_counter ++;
//		ThreadHandler.WaitForStoping();
//		Timer.stop(cashclearing);

		//		ShowConsoleOutput.println("start other machine");


		TimeToken balancing = Timer.start("balancingTime");
		spread_stage();
		if(SimulationState.getLocal().stagecounter%SimulationState.getLocal().balanceRound==0)
		{
			OutD.println("stage counter is at:"+SimulationState.getLocal().stagecounter);
			OutD.println("I'm in");
			//	othermachines = Timer.start("othermachines");
			MultiMachineHandshake();
			//	Timer.stop(othermachines);

			ajustGridResolutions();
			spread_stage();
			//			AddressChangeTracker.updateRemoteHostsWithPartitions();
			MultiMachineHandshake();
			boolean changed = ManagedObjectBalancer.StartBalanceProtocol();
			if(!active)
			{
				Hosts.RegisterAtNextHost();
			}
			while(!active){ Thread.yield();} // wait for activation remoteley
			spread_stage();

			//			othermachines = Timer.start("othermachines");
			MultiMachineHandshake();
			//			Timer.stop(othermachines);




			for (PartitionManager p : ManagerResolver.I().getLocalPartitions())
			{
				p.gatherImportantLocalNeighbours();
			}
			ECM.getInstance().clean();
			nodes = ECM.getInstance().getPhysicalNodes();
			ManagerResolver.I().findCenter();
			//updateNodesDependencies();
			for (PartitionManager partitionManager : ManagerResolver.I().getLocalPartitions()) {
				partitionManager.checkDiffusionHirarchie();
			}

		}

		Timer.stop(balancing);
		TimeToken lastpart = Timer.start("latpart");


		ECM.getInstance().increaseECMtime(Param.SIMULATION_TIME_STEP);
		SpaceNodeFacade.setnewMaxRadius();
		Timer.stop(noexp);
		
		
		//		PartitionManager.debug_checkright();

		

		execute_potentialsave();
		execute_potentialload();

		execute_loggers();
		Timer.stop(lastpart);
		Timer.stopRound();
		SetProcessing();
		if(frame!=null)
		{
			frame.repaint();
		}
		ThreadHandler.WaitForStoping();
		
		takeNextVideoFrame();


	}

	private static void execute_loggers() {
		for (IExporter e : SimulationState.getLocal().exporters) {
			e.process();
		}
	}

	private static void execute_potentialsave() {
		long saveround = SimulationState.getLocal().saveround;
		if(SimulationState.getLocal().stagecounter%(saveround)==0)
		{
			spread_stage();
			MultiMachineHandshake();
			ECM.saveToFile(savename+"_"+SimulationState.getLocal().stagecounter);
			spread_stage();
			MultiMachineHandshake();
		}

	}


	private static void execute_potentialload() {
		if(ECM.toBeLoaded()!=null)
		{
			ECM.readFromFile(ECM.toBeLoaded());
			spread_stage();

			MultiMachineHandshake();
			for (PartitionManager p : ManagerResolver.I().getLocalPartitions())
			{
				p.gatherImportantLocalNeighbours();
			}
			ECM.setToBeLoaded(null);

		}

	}


	private static void execute_ObjectCounting() {
		if(Hosts.getNextHost()==null)
		{
			GlobalObjectCounter c = new GlobalObjectCounter();
			c.apply();
		}

	}

	private static void SetProcessing() {

		double time =0;
		//time+=Timer.getLastTotal();
		//time-=Timer.getTimerTime("othermachines");
		time+=Timer.getTimerTime("prefetch");
		time+=Timer.getTimerTime("physics");
		time+=Timer.getTimerTime("bio");
		time+=Timer.getTimerTime("apply");
		time+=Timer.getTimerTime("latpart");
		
		SimulationState.getLocal().setProcessingTime(time);

	}


	private static void updateNodesDependencies() {
		ECM.getInstance().nodeListLock();
		nodes = ECM.getInstance().getPhysicalNodes();
		for(int i=0;i<nodes.size();i++)
		{
			PhysicalNode n = nodes.getIfTobeProcessed(i);
			if(n==null) continue;
			n.updateDependenciesIgnoreTimestep();
		}
		ECM.getInstance().nodeListunLock();
	}

	//	private static ArrayList<PhysicalNode> nodesToProcess;
	//	private static ArrayList<PhysicalNode> getNodesToProcess() {
	//
	//		if(ECM.getInstance().getPhysicalNodeListHasChanged())
	//		{
	//			ArrayList<PhysicalNode>  temp = new ArrayList<PhysicalNode>();
	//			int i = 0;
	//
	//			for(PhysicalNode n: ECM.getInstance().getPhysicalNodeList())
	//			{
	//				if(n.getSoNode().shallIBeProcessed())
	//				{
	//					temp.add(n);
	//				}
	//				else
	//				{
	//					//				ShowConsoleOutput.println("not taken");
	//					i++;
	//				}
	//			}
	//			nodesToProcess = temp;
	//		}
	//		//		ShowConsoleOutput.println("not taken : "+i);
	//		ECM.getInstance().nodeListunLock();
	//		return nodesToProcess;
	//	}

	public static ArrayAccessHashTable getNodesToProcess() {
		return ECM.getInstance().getPhysicalNodes();
	}

	private static void applyRemoteChanges() {
		for (SingleRemotePartitionManager i : ManagerResolver.I().getRemotePartitions()) {
			i.applyCalculations();
		}
	}



	public static void execute_checkforArival() {
		for (final SingleRemotePartitionManager i : ManagerResolver.I().getRemotePartitions()) {		
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {

					boolean t =i.prefetch();

					return !t;
				}
			});

		}
	}





	public static void spread_stage() {
		SimulationState.getLocal().stagecounter++;
		//ShowConsoleOutput.println("stage now = "+SimulationState.getLocal().stagecounter);
		SimulationState.getLocal().totalObjectCount = ManagerResolver.I().getTotalLocalObjectsCount();
		(new SpreadStage(SimulationState.getLocal().getCopy())).remoteExecuteOnAllNeighbouringHosts();

	}




	static HashT<String,HashT<Long,PhysicalNode>> sendToRemote = new HashT<String, HashT<Long,PhysicalNode>>();
	public static void execute_GenMargins() {
		sendToRemote = new HashT<String, HashT<Long,PhysicalNode>>();
		VecT<String> temp = Hosts.getHosts();

		for (String s : temp) {
			sendToRemote.put(s, new HashT<Long, PhysicalNode>());
		}
		final String localhost = Hosts.getLocalHost();
		final int size = nodes.size()/binsize()+1;
		for (int k = 0;k<binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
//					TimeToken so = Timer.start("genmarg1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;
						PhysicalObject n = (PhysicalObject) nodes.get(m); 
						if(n==null) continue;
						n.checkNeighbors(sendToRemote,localhost);
					}
//					Timer.stop(so);
					return false;
				}
			});
		}



	}


	public static void execute_GenMargins_NonParalell() {
		sendToRemote = new HashT<String, HashT<Long,PhysicalNode>>();
		for (String s : Hosts.getHosts()) {
			sendToRemote.put(s, new HashT<Long, PhysicalNode>());
		}
		String localhost = Hosts.getLocalHost();
		nodes = getNodesToProcess();
		for (int k = 0;k<nodes.size();k++) {
			PhysicalObject n = (PhysicalObject) nodes.get(k); 
			if(n==null) continue;
			n.checkNeighbors(sendToRemote,localhost);
		}
	}

	public static void execute_genMarginBox() {



		for (String s : Hosts.getHosts()) {
			if(!Hosts.isActive(s)) continue;
			if(!sendToRemote.containsKey(s))
			{
				OutD.println("did not find any remote on "+s);
				sendToRemote.put(s, new HashT<Long, PhysicalNode>()); 
			}
			ManagerResolver.I().getHostManager(s);
			CommandCollector c = CommandCollector.get(s);
			c.margin = new RemoteMarginBox<Boolean>(sendToRemote.get(s),SimulationState.getLocal().stagecounter,Hosts.getLocalHost());
			c.margin.prepareSend();
			c.diffMargin = DiffusionNodeManager.I().getDiffusionMargin(s);
			c.diffMargin.prepareSend();
		}	



	}

	
	public static void execute_prefetch_test_fetch() {
		final int size = nodes.size()/binsize()+1;
		if(runPhyics==false) return;
		for (int k = 0;k<binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
//					TimeToken so = Timer.start("prefetch1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;

						PhysicalObject node = (PhysicalObject) nodes.getIfTobeProcessed(m);
						if(node==null) continue;
						node.prefetchDependencies_test();
					}
//					Timer.stop(so);
					return false;

				}

			});

		}


	}
	

	public static void execute_prefetch() {
		final int size = nodes.size()/binsize()+1;
		if(runPhyics==false) return;
		for (int k = 0;k<binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;

						PhysicalObject node = (PhysicalObject) nodes.getIfTobeProcessed(m);
						if(node==null) continue;
						node.getSoNode().resolveTest();
						node.prefetchDependencies();
						
					}
					return false;

				}

			});

		}


	}




	public static int binsize() {
		return 400;
	}


	private static void ajustGridResolutions() {
		int lm = 0;

		boolean haschanged = false;
		for ( PartitionManager r: ManagerResolver.I().getLocalPartitions()) 
		{

			if(r.establishDiffusionGrid(gridresolution))
			{
				haschanged = true;
			}
		}

		while(ManagerResolver.I().checkNSplit(maxnodesperPM))
		{	
			lm++;
			OutD.println("splited "+lm+" times");
			haschanged = true;
		}
		for (PartitionManager partitionManager : ManagerResolver.I().getLocalPartitions()) {
			partitionManager.checkDiffusionHirarchie();
			if(partitionManager.searchpartitions==null)
			{
				partitionManager.gatherImportantLocalNeighbours();
			}
		}
	}


	private static void MultiMachineHandshake() {
		long milies=System.currentTimeMillis();
		while(!allServersReachedCurrentStage(milies))
		{

			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}


	private static void execute_output() {
		//		if(insaneounter>1)
		//		{
		//			ShowConsoleOutput.println("insanme count "+insaneounter);	
		//		}
		//		insaneounter=0;
		OutD.println("Neighbour connection Data: "+ SpaceNodeFacade.checked*1.0/SimulationState.getLocal().totalObjectCount+" "
				+SpaceNodeFacade.checkedIntense*1.0/SimulationState.getLocal().totalObjectCount+" "
				+SpaceNodeFacade.taken*1.0/SimulationState.getLocal().totalObjectCount+" "+SpaceNodeFacade.nieghbours*1.0/SimulationState.getLocal().totalObjectCount);
		//		if(ShowObjectCount.current!=null) ShowObjectCount.current.printout();

		//		if(printCurrentECMTime){
		//			ShowConsoleOutput.println("time = "+doubleToString(ECM.getInstance().getECMtime(), 2));
		//		}
		//	ShowConsoleOutput.println("tcouner "+counter_t+ " stage "+SimulationState.getLocal().stagecounter);
		ShowObjectCount.checked=SpaceNodeFacade.checked;
		ShowObjectCount.checkedIntense=SpaceNodeFacade.checkedIntense;
		ShowObjectCount.taken=SpaceNodeFacade.taken;
		ShowObjectCount.nieghbours=SpaceNodeFacade.nieghbours;

		SpaceNodeFacade.checked=0;
		SpaceNodeFacade.checkedIntense=0;
		SpaceNodeFacade.taken=0;
		SpaceNodeFacade.nieghbours=0;
		if(printCurrentStep){
			OutD.println("step = "+SimulationState.getLocal().cycle_counter);	
		}
	}


	private static void execute_biologymodules() {
		final int size = nodes.size()/binsize()+1;
		for (int k = 0;k<binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
//					TimeToken so = Timer.start("bio1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;
						PhysicalObject n = (PhysicalObject) nodes.getIfTobeProcessed(m); 
						if(n==null) continue;
						
//						PartitionAddress pad = n.getSoNode().getObjectRef().getPartitionId();
//						String host  = pad.getHost();
//						String host2 = Hosts.getLocalHost();
//						double [] pos1 = n.getSoNode().getPosition();
//						double [] pos2 = n.getMassLocation();
//						PartitionAddress ad =  ManagerResolver.I().getByCordinate(pos1).getAddress();
//						PartitionAddress ad2 = ManagerResolver.I().getByCordinate(pos2).getAddress();
//						boolean o = Hosts.isOnLocalHost(n.getSoNode().getObjectRef().getPartitionId().getHost());
//						if(!o)
//						{
//							System.out.println("this is not local why?"+n.getSoNode().getID());
//							continue;
//						}
						
						
						if(n.isAPhysicalSphere())
						{
							final SomaElement s =  ((PhysicalSphere)n).getSomaElement();
							final Cell c = s.getCell();
							c.run();
							s.run();
						}
						else
						{
							final NeuriteElement c = ((PhysicalCylinder)n).getNeuriteElement();
							c.run();
						}
					}
//					Timer.stop(so);
					return false;
				}
			});
		}
	}


	private static void execute_Physics() {

		final int size = nodes.size()/binsize()+1;
		for (int k = 0;k<binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override

				public boolean apply() {
//					TimeToken so = Timer.start("phys1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;
						PhysicalObject ps = (PhysicalObject) nodes.getIfTobeProcessed(m);
						if(ps==null) continue;
						if(!ps.getSoNode().isLocal())
						{
							OutD.println("this is not local doing it anyways "+ps.getSoNode().getID());
							//throw new RuntimeException("this is none local!!!");
						}
						if(ps.isOnTheSchedulerListForPhysicalObjects()){
							//Boolean s = false;
							for(int i =0;i<getRuns();i++)
							{
								PartitionAddress pad = ps.getSoNode().getObjectRef().getPartitionId();
								String host  = pad.getHost();
								String host2 = Hosts.getLocalHost();
							
								boolean o = Hosts.isOnLocalHost(ps.getSoNode().getObjectRef().getPartitionId().getHost());
								if(!o)
								{
									//System.out.println("this is not local why?"+ps.getSoNode().getID());
									continue;
								}
								int k=0;
								while(!ps.runPhysics())
								{
									k++;
									try {
										Thread.sleep(0);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
							//							if(!s)
							//							{
							//								return true;
							//							}
						}
						if(runIntracelularDiffusion)
						{
							ps.runIntracellularDiffusion();
						}
					}
//					Timer.stop(so);
					return false;

				}});
		}
	}


	private static void execute_ApplyChanges() {
		final int size = nodes.size()/binsize()+1;
		for (int k = 0;k<binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
//					TimeToken so = Timer.start("apply1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;
						PhysicalObject ps = (PhysicalObject) nodes.getIfTobeProcessed(m); 
						if(ps==null) continue;

						for ( int i=0;i<getRuns();i++)
						{
							ps.applyPhysicsCalculations();
						}
					}
//					Timer.stop(so);
					return false;
				}
			});
		}

		if(runExtracellularDiffusion)
		{
			final ArrayList<AbstractDiffusionNode> temp =DiffusionNodeManager.I().getLocalDiffusionNodes();
			final int size2 = temp.size()/binsize()+1;
			for (int k = 0;k<binsize();k++) {
				final int f = k; 
				CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
					public boolean apply() {
						TimeToken so = Timer.start("applydiffuse*");
						for(int m=f*size2;m<(f+1)*size2;m++)
						{
							if(m>=temp.size()) return false;
							AbstractDiffusionNode p= (AbstractDiffusionNode) temp.get(m); 
							if(p==null) continue;
							for ( int i=0;i<getRuns();i++)
							{
								p.applyCalculations();
							}
						}
						Timer.stop(so);
						return false;
					}
				});
			}
		}
	}




	private static void execute_externalDiffusion() {

		if(runExtracellularDiffusion){
			DiffusionNodeManager.I().checkNeighbours();
			DiffusionNodeManager.I().setToBeDiffused(ECM.getInstance().getSubstanceTemplates().keySet());
			final ArrayList<AbstractDiffusionNode> temp =DiffusionNodeManager.I().getLocalDiffusionNodes();
			final int size = temp.size()/binsize()+1;
			for (int k = 0;k<binsize();k++) {
				final int f = k; 
				CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
					@Override
					public boolean apply() {
//						TimeToken so = Timer.start("phys1*");
						for(int m=f*size;m<(f+1)*size;m++)
						{
							if(m>=temp.size()) return false;
							AbstractDiffusionNode p= (AbstractDiffusionNode) temp.get(m); 
							if(p==null) continue;
							for(int i=0;i<getRuns();i++)
							{
								p.diffuse();
							}

						}
//						Timer.stop(so);
						return false;
					}
				});
			}

		}
	}



	/** Runs the simulation, i.e. runs each active CX3D runnable objects endlessly.*/
	public static void simulate(){
		while(true)
			simulateOneStep();
	}

	/** Runs the simulation for a given number of time steps, i.e. runs each active CX3D 
	 * runnable objects.
	 * @param steps nb of steps that the simulation is run.
	 */
	public static void simulateThatManyTimeSteps(int steps){
		for (int i = 0; i < steps; i++) {
			simulateOneStep();
		}
	}

	public static void setPrintCurrentECMTime(boolean printCurrentECMTim) {
		printCurrentECMTime = printCurrentECMTim;
	}

	public static void setPrintCurrentStep(boolean printCurrentSte) {
		printCurrentStep = printCurrentSte;
	}

	public static void reset() {
		// TODO Auto-generated method stub

	}

	public static void Pause(boolean val) {
		ThreadHandler.setPause(val);
	}


	public static int getInter_snapshot_time_steps() {
		return inter_snapshot_time_steps;
	}

	public static void setInter_snapshot_time_steps(int inter_snapshot_time_step) {
		inter_snapshot_time_steps = inter_snapshot_time_step;
	}

	private static boolean allServersReachedCurrentStage(long milies)
	{
		for (String host : Hosts.getHosts()) {
			if(!Hosts.isActive(host)) continue;

			SimulationState remote= SimulationState.getRemote(host);
			SimulationState local = SimulationState.getLocal();
			if(remote==null || remote.stagecounter<local.stagecounter)
			{
				if(System.currentTimeMillis()-4000>milies)
				{
					milies=System.currentTimeMillis();
					//	ShowConsoleOutput.println("resending status");
					remote = new RequestStage().remoteExecuteAnswer(host);
					SimulationState.put(host,remote);
				}
				return false;
			}
			//			ShowConsoleOutput.println(host+"got the same time as me");
		}
		return true;
	}

	private static MonitoringGui frame;





	public static void setGui(MonitoringGui s)
	{
		frame = s;
	}

	public static void stopVideo() {
		videorec.stop();
		videorec = null;
	}

	private static void takeNextVideoFrame() {
		if(videorec !=null)
		{
			if(SimulationState.getLocal().cycle_counter%videoSnapshotIntervall==0)
			{
				videorec.paint(MonitoringGui.getCurrent());
			}
		}
	}

	public static VideoRecorder startVideoRecording(String filename)
	{
		videorec = new VideoRecorder(filename);
		return videorec;
	}

	public static void startVideo()
	{
		videorec.start();
	}

	private static void setRuns(int runs) {
		MultiThreadScheduler.runs = runs;
	}

	private static int getRuns() {
		return 1;
	}

	public static void setImageView(ImageExport monitoringImage) {


	}

	

}


