package ini.cx3d.physics.diffusion;

import ini.cx3d.gui.physics.diffusion.NotRecivedDiffNode;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.spacialOrganisation.AbstractPartitionManager;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.commands.CommandCollector;
import ini.cx3d.utilities.HashT;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;

public class DiffusionNodeManager implements Serializable{


	private static DiffusionNodeManager m = new DiffusionNodeManager();
	private ArrayList<String> substances = new ArrayList<String>();
	public static void SetI(DiffusionNodeManager m)
	{
		DiffusionNodeManager.m = m;
	}

	public static DiffusionNodeManager I()
	{
		return m;
	}

	private HashT<Long, AbstractDiffusionNode> nodes = new HashT<Long, AbstractDiffusionNode>();
	private HashT<String,HashT<Long, AbstractDiffusionNode>> prefetched = new HashT<String,HashT<Long, AbstractDiffusionNode>>();

	public void addDiffusionNode(AbstractDiffusionNode node)
	{

		node.address.setHost(Hosts.getLocalHost());

		nodes.put(node.address.id, node);
		applyAddressChanges(node.address);
		//		ShowConsoleOutput.println("added "+node.address);
		//		debug_checkForDoubles();

	}

	public void removeDiffusionNode(AbstractDiffusionNode node)
	{
		if(!nodes.containsKey(node.address.id))
		{ 
			throw new RuntimeException("could not remove this why?");
		}

		nodes.remove(node.address.id);
		//		debug_checkForDoubles();
	}

	public void debug_checkForDoubles()
	{
		for (Long s : nodes.keySet()) {
			for(AbstractDiffusionNode o: getRemoteDiffusionNodes())
			{
				if(o.getAddress().equals(DiffReg.I().get(s))) throw new RuntimeException("got same 2x");
			}
		}
	}

	public void debug_checkForLocalNeighbourConsistensy()
	{
		for (AbstractDiffusionNode s : nodes.values()) {
			for(DiffusionAddress o: s.getNeighboursAddresses())
			{
				if(o==null) continue;
				if(o.isLocal() && !o.containedLocal()) throw new RuntimeException("inconsistent!");
				if(!o.isLocal() && o.containedLocal()) throw new RuntimeException("inconsistent!2");

			}
		}
	}

	public void debug_checkForRemoteNeighbourConsistensy()
	{
		for (AbstractDiffusionNode s : nodes.values()) {
			for(DiffusionAddress o: s.getNeighboursAddresses())
			{
				if(!o.isLocal())
				{
					boolean contained = false;
					for (String k: prefetched.keySet()) {
						contained|=prefetched.get(k).containsKey(o.id);
					}

					if(!contained)
					{
						NotRecivedDiffNode.add(o);
						throw new RuntimeException("not contained at all!");
					}
					for (String k: prefetched.keySet()) {
						if(prefetched.get(k).containsKey(o.id) && !o.getHost().equals(k)) throw new RuntimeException("remotley inconsistent!");
						if(!prefetched.get(k).containsKey(o.id) && o.getHost().equals(k)) throw new RuntimeException("remotley inconsistent!");
					}
				}
			}
		}
	}


	public boolean containsLocaly(DiffusionAddress address)
	{
		return nodes.containsKey(address.id);
	}

	public AbstractDiffusionNode getDiffusionNode(DiffusionAddress address)
	{
		AbstractDiffusionNode n = null;
		if(nodes.containsKey(address.id))
		{
			n=nodes.get(address.id);
		}
		else if(!address.isLocal())
		{
			if(!prefetched.containsKey(address.getHost()))
			{	throw new RuntimeException("remotehostInexistent");

			}
			if(!this.prefetched.get(address.getHost()).containsKey(address.id))
			{
				NotRecivedDiffNode.add(address);
				printRecursiveUP(address);
				throw new RuntimeException("this is not local nor margined!");

			}
			n= this.prefetched.get(address.getHost()).get(address.id);
		}
		if(n==null)
		{
			NotRecivedDiffNode.add(address);
			throw new RuntimeException("not containging key"+address.id);
		}
		return n;

	}

	private void printRecursiveUP(DiffusionAddress address) {
		String s = Long.toString(address.id, 8);
		for(int i=0;i<s.length();i++)
		{
			s = s.substring(0,s.length()-1);
			long l = Long.parseLong(s, 8);
			OutD.println("contains:"+s+" : "+ this.prefetched.get(address.getHost()).containsKey(l));
		}


	}

	public boolean contains(DiffusionAddress address)
	{
		if(nodes.containsKey(address.id)) return true;
		if(!prefetched.containsKey(address.getHost()))
		{	throw new RuntimeException("remotehostInexistent");

		}
		if(this.prefetched.get(address.getHost()).containsKey(address.id)) return true;
		return false;
	}


	public ArrayList<AbstractDiffusionNode> getDiffusionNodes(ArrayList<Long> adresses)
	{
		ArrayList<AbstractDiffusionNode> res = new ArrayList<AbstractDiffusionNode>();

		for (Long s : adresses) {
			res.add(getDiffusionNode(DiffReg.I().get(s)));
		}
		return res;
	}

	public ArrayList<AbstractDiffusionNode> getLocalDiffusionNodes(ArrayList<Long> adresses)
	{
		ArrayList<AbstractDiffusionNode> res = new ArrayList<AbstractDiffusionNode>();
		for (Long o : adresses) {
			DiffusionAddress s = DiffReg.I().get(o);
			if(s.isLocal())
			{

				res.add(nodes.get(s.id));
			}
		}
		return res;
	}	


	public void applyAddressChanges(ArrayList<DiffusionAddress> adds)
	{
		for (DiffusionAddress diffusionAddress : adds) {
			DiffReg.I().put(diffusionAddress);
		}

	}


	public void applyAddressChanges(DiffusionAddress diffusionAddress)
	{
		DiffReg.I().put(diffusionAddress);
	}

	public boolean establishDiffusionGrid(int maxcount)
	{
		boolean changedsetup=false;

		for (AbstractPartitionManager d : ManagerResolver.I().getLocalPartitions()) {
			changedsetup |= d.establishDiffusionGrid(maxcount);
		}
		return changedsetup;
	}



	public int getDiffusionNodeCount() {
		// TODO Auto-generated method stub
		return this.nodes.size();
	}

	public ArrayList<AbstractDiffusionNode> getLocalDiffusionNodes() {
		return new ArrayList<AbstractDiffusionNode>(nodes.values());
	}


	private HashT<String, DiffusionMargin> margins = new HashT<String, DiffusionMargin>();

	public void generateDiffusionMargin()
	{

		generateDiffusionMargin2();
//		margins = new HashT<String, DiffusionMargin>();
//		nodes.startIteration();
//		for (AbstractDiffusionNode o : nodes.values()) {
//			if(o.getNbours()==null) continue;
//			o.expandNbours();
//			for (DiffusionAddress add : o.getNbours()) {
//
//				if(!Hosts.isOnLocalHost(add.getHost()))
//				{
//					if(!margins.containsKey(add.getHost()))
//					{
//						margins.put(add.getHost(), new DiffusionMargin());
//					}
//					margins.get(add.getHost()).tobesent.put(o.getAddress().id,o);
//				}
//			}
//		}
//		nodes.stopIteration();

	}
	
	private void generateDiffusionMargin2()
	{
		margins = new HashT<String, DiffusionMargin>();
		DiffusionMargin dprev=null;
		DiffusionMargin dnext=null;
		
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		nodes.startIteration();
		
		for (AbstractDiffusionNode o : nodes.values()) {
			if(!o.isLeaf()) continue;
			double curupper = Math.min(o.address.getUpperLeftCornerFront()[0],o.address.getLowerRightCornerBack()[0]);
			if(curupper<min)
			{
				min = curupper;
				dnext = new DiffusionMargin();
				margins.put(Hosts.getNextHost(),dnext);
				
			}
			double curlower = Math.max(o.address.getUpperLeftCornerFront()[0],o.address.getLowerRightCornerBack()[0]);
			if(curlower>max)
			{
				max = curlower;
				dprev = new DiffusionMargin();
				margins.put(Hosts.getPrevHost(),dprev);
			}
		}
		for (AbstractDiffusionNode o : nodes.values()) {
			if(!o.isLeaf()) continue;
			if(Hosts.getNextActive())
			{
				double curupper = Math.min(o.address.getUpperLeftCornerFront()[0],o.address.getLowerRightCornerBack()[0]);
				
				if(Math.abs(curupper -  min)<30)
				{
					dnext.tobesent.put(o.getAddress().id,o);
				}
			}
			if(Hosts.getPrevActive())
			{
				double curlower = Math.max(o.address.getUpperLeftCornerFront()[0],o.address.getLowerRightCornerBack()[0]);
				if(Math.abs(curlower -  max)<30)
				{
					dprev.tobesent.put(o.getAddress().id,o);
				}
			}
		}
		nodes.stopIteration();
	}
	

	public void checkNeighbours()
	{	
		try{
			nodes.startIteration();
			for (AbstractDiffusionNode o : nodes.values()) {
				o.checkNeighbours();
			}
		}
		finally
		{
			nodes.stopIteration();
		}
	}

	public DiffusionMargin getDiffusionMargin(String host)
	{
		if(!margins.containsKey(host))
		{
			DiffusionMargin d = new DiffusionMargin();
			d.tobesent = new HashT<Long, AbstractDiffusionNode>();
			return d;
		}
		return margins.get(host);
	}

	public HashT<String, DiffusionMargin> getDiffusionMargins()
	{
		return margins;
	}


	public void sendDiffusionMargin()
	{
		//generateDiffusionMargin2();
		for (String host : margins.keySet()) {
			DiffusionMargin m = margins.get(host);
			CommandCollector.get(host).diffMargin = m;
			m.prepareSend();
		}
	}


	public void setPrefetched(String s,HashT<Long, AbstractDiffusionNode> o)
	{
		//		if(prefetched.containsKey(s))
		//		{
		//			for (Long l : prefetched.get(s).keySet()) {
		//				DiffReg.I().remove(l);
		//			}
		//		}
		prefetched.put(s, o);
		//		debug_checkForDoubles();
		//		debug_checkForLocalNeighbourConsistensy();
	}

	public void clearPrefetched(String s)
	{
		prefetched.remove(s);
		//debug_checkForDoubles();
	}

	public  ArrayList<AbstractDiffusionNode> getRemoteDiffusionNodes() {
		// TODO Auto-generated method stub
		ArrayList<AbstractDiffusionNode> o = new  ArrayList<AbstractDiffusionNode>();
		for (String s: prefetched.keySet()) {
			o.addAll(prefetched.get(s).values());
		}

		return o;
	}


	public ArrayList<AbstractDiffusionNode> getAllLocalDiffusionNodes() {
		return new ArrayList<AbstractDiffusionNode>(nodes.values());
	}

	public boolean ajustSizeToNeighbours() {

		nodes.startIteration();
		for(AbstractDiffusionNode n1:nodes.values())
		{
			for (DiffusionAddress n2 : n1.getNeighboursAddresses()) {
				if(n1.getVolume()/8>n2.getVolume())
				{
					nodes.stopIteration();
					n1.introduceLayer();

					return  true;
				}
			}
		}
		nodes.stopIteration();
		return false;
	}

	public HashT<Long, AbstractDiffusionNode> getAllLeafesOfHost(String host) {
		HashT<Long, AbstractDiffusionNode> remotenodes = new HashT<Long, AbstractDiffusionNode>();
		if(Hosts.getLocalHost().equals(host))
		{
			try{
				nodes.startIteration();
				for (AbstractDiffusionNode n : this.nodes.values()) {
					if(n.isLeaf())
					{
						remotenodes.put(n.address.id, n);
					}
				}
			}
			finally
			{
				nodes.stopIteration();
			}
		}
		else
		{
			for (AbstractDiffusionNode n : this.prefetched.get(host).values()) {

				remotenodes.put(n.address.id, n);
			}
		}
		return remotenodes;
	}

	public void updatePrefetched(String host,HashT<Long, AbstractDiffusionNode> tobesent) {

		if(!prefetched.containsKey(host))
		{
			prefetched.put(host, new HashT<Long, AbstractDiffusionNode>());
		}
		HashT<Long, AbstractDiffusionNode> temp = prefetched.get(host);
		for (long l : tobesent.keySet()) {
			temp.put(l, tobesent.get(l));
		}
	}

	public HashT<String,DiffusionMargin> gnerateDiffusionMargin(String host,ArrayList<AbstractDiffusionNode> diffNodesToShip) {
		HashT<String,DiffusionMargin> temp = new HashT<String,DiffusionMargin>();
		for(AbstractDiffusionNode n: diffNodesToShip)
		{
			if(!n.isLeaf()) continue;
			for(DiffusionAddress add: n.getNbours())
			{
				if(!add.getHost().equals(host))
				{
					if(!temp.containsKey(add.getHost()))
					{
						temp.put(add.getHost(),new DiffusionMargin());
						temp.get(add.getHost()).host= add.getHost();
						temp.get(add.getHost()).tobesent= new HashT<Long, AbstractDiffusionNode>();
					}
					DiffusionMargin difmarg= temp.get(add.getHost());
					difmarg.tobesent.put(add.id, DiffusionNodeManager.I().getDiffusionNode(add));
				}
			}
		}
		return temp;
	}

	public void setToBeDiffused(Set<String> keySet) {
		substances = new ArrayList<String>(keySet);
		
	}
	
	public ArrayList<String> getSubstances()
	{
		return substances;
	}

	public ArrayList<AbstractDiffusionNode> getAllDiffusionLeafes() {
		// TODO Auto-generated method stub
		ArrayList<AbstractDiffusionNode> nodes = new ArrayList<AbstractDiffusionNode>();
		for(AbstractDiffusionNode n: nodes)
		{
			if(n.isLeaf()) nodes.add(n);
		}
		return nodes;
	}
}

