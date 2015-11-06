package ini.cx3d.spacialOrganisation.commands;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.commands.AbstractComplexCommand;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.parallelization.communication.SimpleResponse;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionAddress;
import ini.cx3d.physics.diffusion.DiffusionMargin;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.spacialOrganisation.ManagedObjectBalancer;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.PartitionManager;

import java.util.ArrayList;

public class ShipCommand extends AbstractComplexCommand<Boolean>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7992621147396009398L;
	/**
	 * 
	 */
	ArrayList<PartitionManager> pack;
	ArrayList<AbstractDiffusionNode> difnodes; 
	ArrayList<DiffusionMargin> difnodeNeighbours; 
	ArrayList<RemoteMarginBox<?>> remotepartitions;

	public ShipCommand(ArrayList<PartitionManager> pack, ArrayList<AbstractDiffusionNode> diffNodesToShip, ArrayList<DiffusionMargin> remotediffusion, ArrayList<RemoteMarginBox<?>> remoteboxes)
	{
		this.difnodeNeighbours = remotediffusion;
		this.difnodes = diffNodesToShip;
		this.pack = pack;
		this.remotepartitions = remoteboxes;
		
	}
	@Override
	public boolean apply()
	{

		OutD.println("got the package so aplying it");
		Hosts.setActive(client);
		for(PartitionManager p : pack)
		{
			p.address.setHost(Hosts.getLocalHost());
			ManagerResolver.I().addPartitionManager(p);
			OutD.println("adding partitions "+p.address.address);

		}
		
		DiffusionNodeManager.I().clearPrefetched(getClient());
		
		ArrayList<DiffusionAddress> diffaddress = new ArrayList<DiffusionAddress>();
		
		for (AbstractDiffusionNode i : difnodes) {
			OutD.println("insert ::  "+i.toString());
			DiffusionNodeManager.I().addDiffusionNode(i);
			diffaddress.add(i.getAddress());
		}
		DiffusionNodeManager.I().applyAddressChanges(diffaddress);
		
		for(PartitionManager p : pack)
		{
			p.checkDiffusionHirarchie();
		}
		for (DiffusionMargin d:this.difnodeNeighbours) {
			if(!d.host.equals(Hosts.getLocalHost()))
			{
				d.applyUpdate();
			}
		}
		
		
//		DiffusionNodeManager.I().debug_checkForLocalNeighbourConsistensy();
		SimulationState.getLocal().clearAverageTime();
		DiffusionNodeManager.I().generateDiffusionMargin();
				
		ManagerResolver.I().getHostManager(client);
		for (RemoteMarginBox<?> rpm : remotepartitions) {
			if(!rpm.thishost.equals(Hosts.getLocalHost()))
			{
				rpm.stage = SimulationState.getLocal().stagecounter;
				rpm.apply();
				rpm.setCurrent();
			}
		}
		
//		ManagedObjectBalancer.updateOtherHostsWithRemote();
		
		send(new SimpleResponse<Boolean>(mailboxID,true));
		if(!MultiThreadScheduler.active)
		{
			MultiThreadScheduler.active = true;
			//MultiThreadScheduler.logfile.printHeader();
		}
		return false;
	}
	



}