/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.spacialOrganisation;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.gui.spacialOrganisation.OutCastPositionDrawer;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.physics.diffusion.DiffusionAddress;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.simulation.ECM;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.spacialOrganisation.slot.Iterator;
import ini.cx3d.spacialOrganisation.slot.Slot;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.Matrix;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;


public class PartitionManager extends AbstractPartitionManager {




	/**
	 * 
	 */
	private static final long serialVersionUID = -8470577077069094084L;
	public static final int gridDepth=1;
	private AtomicLong idgenerator = new AtomicLong(); 
	private SpaceNodeRegistry registry;
	public ISpacialRepesentation  list;
	public PartitionAddress address;
	private DiffusionAddress diffusionnode;
	int counterz= 0;

	private void init()
	{
		if(!isInitialized())
		{
			synchronized (this) {
				if(!isInitialized())
				{	
					list = new Slot(address.getUpperLeftCornerFront(), address.getLowerRightCornerBack(), MultiThreadScheduler.slotresolution);
					registry = new SpaceNodeRegistry();
				}
			}
		}
	}


	public PartitionManager(PartitionAddress add) {
		this.address = add;
		//list = new LinkedList();

	}

	public ObjectReference generateObjectAddress(double [] coordinates)
	{
		ObjectReference temp = new ObjectReference((address.address*1000000)+ idgenerator.incrementAndGet(),coordinates,address);
		//OutD.println("generatedObjectAdress "+temp.address,"yellow");
		ORR.I().put(temp);
		return temp;
	}


	public void insert(ObjectReference closepoint,ObjectReference toinsert,PhysicalNode n)
	{
		if(this.address.containsCoordinates(toinsert.getPosition()))
		{
			
			toinsert.setPartitionId(this.address);
			ORR.I().put(toinsert);
			ORR.addHistory(toinsert, "inserting into "+address.address);
			ObjectReference lockref = ORR.I().get(toinsert.address);

			synchronized (lockref){
				try{
					insertTogether(closepoint,toinsert,n);
				}
				catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}
			}

			ObjectReference r = ORR.I().get(toinsert.address);

			if(toinsert.getPosition()[0]!=r.getPosition()[0] ||
					toinsert.getPosition()[1]!=r.getPosition()[1] ||
					toinsert.getPosition()[2]!=r.getPosition()[2]
			)
			{
				OutD.println("wrong wrong wrong");
			}




		}
		else
		{
			AbstractPartitionManager pm = ManagerResolver.I().getByCordinate(toinsert.getPosition());
			pm.insert(null, toinsert, n);

		}

	}

	public void checkDiffusionHirarchie()
	{
		DiffusionNodeManager.I().getDiffusionNode(this.diffusionnode).checkDiffusionHirarchie();
	}

	public void searchNeigbours(ObjectReference middle,double radius,IObjectReferenceSearchVisitor visitor)
	{
		this.getAllInRangeOnThisManager(middle,radius,visitor);
		if(address.encloses(middle.getPosition(),radius))
			return;
		if(searchpartitions==null){

			gatherImportantLocalNeighbours();
		}
		for (PartitionManager p :searchpartitions) {
			p.getAllInRangeOnThisManager(middle,radius, visitor);
		}
		for (SingleRemotePartitionManager p : ManagerResolver.I().getRemotePartitions()) {
			p.getAllInRangeOnThisManager(middle,radius, visitor);
		}
	}

	public void getAllInRangeOnThisManager(ObjectReference middle,double radius,IObjectReferenceSearchVisitor visitor)
	{	
		if(!isInitialized()) return;
		if(address.intersectsWithSphere(middle.getPosition(), radius))
		{
			list.searchRange(middle, radius, visitor);
		}

	}



	@Override
	public int count() {
		if(!isInitialized()) return 0;
		return list.count();
	}

	@Override
	public PartitionAddress getAddress() {
		return address;
	}



	@Override
	public ObjectReference move(ObjectReference ref, double[] new_coordinates) {


		if(this.getAddress().containsCoordinates(new_coordinates))
		{
		

			ORR.addHistory(ref, "move no change partition "+this.address.address);
			ref = moveTogether(ref, new_coordinates);

			ObjectReference r = ORR.I().get(ref.address);
			//			if(ref2.getPosition()[0]!=r.getPosition()[0] ||
			//					ref2.getPosition()[1]!=r.getPosition()[1] ||
			//					ref2.getPosition()[2]!=r.getPosition()[2]
			//			)
			//			{
			//				OutD.println("wrong wrong wrong");
			//			} 
			//			if(new_coordinates[0]!=r.getPosition()[0] ||
			//					new_coordinates[1]!=r.getPosition()[1] ||
			//					new_coordinates[2]!=r.getPosition()[2]
			//			)
			//			{
			//				OutD.println("wrong wrong wrong");
			//			}

		}
		else
		{
			init();
			ORR.addHistory(ref, "move to another partition "+address.address);
			ObjectReference lockref = ORR.I().get(ref.address);
			synchronized (lockref){
				if(!registry.containsKey(ref))
				{
					OutD.println("squeeeeekkkk");
					registry.containsKey(ref);
					//					throw new RuntimeException("whoot not in there!");
				}
			}


			ObjectReference old = ref.getCopy();


			AbstractPartitionManager newplace = ManagerResolver.I().getByCordinate(new_coordinates);
			if(newplace == null)
			{
				OutCastPositionDrawer.outcast = new_coordinates;
				ManagerResolver.I().getByCordinate(new_coordinates);
				//				System.out.println("wold boundaries reched not doing anything now!!!");
				return ref;

			}
			if(newplace.getAddress().address == this.address.address) 
			{

				throw new RuntimeException();
			}

			ref = new ObjectReference(old.address,new_coordinates.clone(),newplace.getAddress());
			newplace.insert(null,ref,registry.resolve(ref));
			this.remove(old);			

		}

		return ref;
	}


	@Override
	public void remove(ObjectReference ref) {
		ObjectReference lockref = ORR.I().get(ref.address);
//		synchronized (lockref){
			removeTogether(ref);
//		}
	}


	private void removeTogether(ObjectReference ref)
	{
		init();
		DebugCheckSameContents(ref);
		this.list.remove(ref);
		this.registry.remove(ref);
		DebugCheckSameContents(ref);

	}

	private void insertTogether(ObjectReference closePoint,ObjectReference toins,PhysicalNode n)
	{
		init();
		DebugCheckSameContents(toins);
		this.list.insertNode(closePoint, toins);
		this.registry.register(toins, n);
		DebugCheckSameContents(toins);

	}

	private ObjectReference moveTogether(ObjectReference ref,double[] new_coordinates)
	{
		init();
		DebugCheckSameContents(ref);
		ObjectReference ref2 =  this.list.move(ref, new_coordinates);
		PhysicalNode n = registry.resolve(ref2);
		this.registry.register(ref2, n);
		DebugCheckSameContents(ref);
		return ref2;


	}

	public ArrayList<PartitionManager> split()
	{
		//		debug_checkDependancy();
		ArrayList<PartitionManager> newmanagers= new ArrayList<PartitionManager>();
		getDiffusionnode().introduceLayersToDepth(1);
		//DiffusionNodeManager.I().generateDiffusionMargin2();
		String temp ="";
		for(int i =0;i<8;i++)
		{
			AbstractDiffusionNode diffnode =this.getDiffusionnode().getSubnode(i);
			PartitionManager p = (PartitionManager) ManagerResolver.I().createPartitionManager(diffnode.getAddress().getUpperLeftCornerFront().clone(),diffnode.getAddress().getLowerRightCornerBack().clone(),this.address.address,i,diffnode);
			newmanagers.add(p);
			temp+= p.address.address+",";

		}
		OutD.println("we remove "+this.address.address+" replace "+temp,"yellow");

		ManagerResolver.I().remove(this);
		OutD.println("we remove "+this.address.address,"yellow");

		Iiterator iter = getIterator();
		if(!isInitialized()) return newmanagers;

		iter.next();
		if(list.count()!=registry.count())
		{
			OutD.println("asdf");
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int i1= list.count();
			int i2 = registry.count();
			throw new RuntimeException("this pm is not sane!!");

		}

		OutD.println("count "+list.count());
		int counter = 0;

		while(!iter.isAtEnd())
		{
			ObjectReference l= iter.getCurrent();
			boolean t = false;
			int doublecount = 0;
			for (PartitionManager m : newmanagers) {

				if(m.address.containsCoordinates(l.getPosition()))
				{
					counter++;
					doublecount++;
					m.insert(null, l, registry.resolve(l));
					t = true;
					if(doublecount>1)
					{
						System.out.println("whooootttttttt");
					}
				}
			}
			if(this.address.address==ORR.I().get(l.address).address)
			{
				throw new RuntimeException("wrong placed");
			}

			if(!t)
			{

				if(this.address.containsCoordinates(l.getPosition()))
				{
					System.out.println("not event this contains cords!!!");

				}
				OutD.println(l);
				OutD.println();
				OutD.println("my addr"+this.address+" contains "+this.address.containsCoordinates(l.getPosition()));
				for(PartitionManager m : newmanagers)
				{
					OutD.println(m.address);
				}

				System.out.println("we have not found!!!!");
			}
			//counter++;
			iter.next();
		}
		if(counter!=list.count())
		{

			System.out.println("not the same damn!");
		}
		int first= 0;
		for (PartitionManager pma : newmanagers) {
			OutD.println("we count "+pma.address.address+" "+pma.count(),"yellow");
			first+=pma.count();
			AbstractPartitionManager temps = ManagerResolver.I().resolve(pma.address.address);
			if(temps==null)
			{
				System.out.println("wee its null damn!");
			}
		}
		if(counter!=first)
		{
			System.out.println("the counters are not the same!!!");
		}
		this.registry = new SpaceNodeRegistry();
		this.list = new Slot(address.getUpperLeftCornerFront(), address.getLowerRightCornerBack(), MultiThreadScheduler.slotresolution);
		OutD.println("PartitionManager.split()");
		OutD.println("Split complete!!!"+ManagerResolver.I().getLocalPartitionAddresses().size());
		OutD.println("PartitionManager.split() END\n\n");
		//		ORR.I().debug_checkConsisitency();
		//		debug_checkIfRefAtTheRightPlace();
		//		System.out.println("---- check if depenancies are still right after split");
		//		debug_checkDependancy();
		//		System.out.println("--- check finished---");
		return newmanagers;
	}

	public void debug_checkDependancy()
	{
		for(PhysicalCylinder n:ECM.getInstance().physicalCylinderList.values())
		{	
			long a = n.getSoNode().getID();
			ObjectReference r = ORR.I().get(a);
			PartitionAddress partitionid =null;
			try{
				partitionid= r.getPartitionId();
			}
			catch(NullPointerException e) 
			{
				ManagerResolver.I().debug_print();
			}
			AbstractPartitionManager m =  ManagerResolver.I().resolve(partitionid);
			if(m==null )
			{
				System.out.println("no pm "+m);
			}
			PhysicalNode temp = m.getPhysicalNode(r);
			if(temp==null)
			{
				System.out.println("no physicalnode "+r.address);
			}

		}
	}


	public Iiterator getIterator() {
		if(!isInitialized()) return null;
		return list.getIterator();
	}

	public void setDiffusionnode(AbstractDiffusionNode diffusionnode) {
		this.diffusionnode = diffusionnode.getAddress();
	}


	public AbstractDiffusionNode getDiffusionnode() {

		return DiffusionNodeManager.I().getDiffusionNode(diffusionnode);

	}

	public void generateSendToRemote(HashT<String,HashT<Long,PhysicalNode>> sendToRemote)
	{
		if(!isInitialized()) return;
		for (PhysicalNode n : registry.getAll()) {
			n.checkNeighbors(sendToRemote,address.getHost());
		}
	}


	@Override
	public PhysicalNode getPhysicalNode(ObjectReference ref) {

		//		PhysicalNode n =  registry.resolve(ref);
		//		if(n==null){
		//			if(registry.containsKey(ref)) ShowConsoleOutput.println("but I do contain key! damn"+ref.address);
		//			ShowConsoleOutput.println("why is this null? "+ref.address);
		//		}
		//		return n;
		if(!isInitialized())
		{
			return null;
		}
		return registry.resolve(ref);
	}

	@Override
	public void removeLocaly() {
		if(!isInitialized()) return;
		for (PhysicalNode p : registry.getAll() ) {
			if(p!=null)
			{
				//	OutD.println("removing"+p.getSoNode().getID(), "gray");
				p.removeLocally();
			}
		}
	}
	@Override
	public void installLocaly() {
		this.address.setHost(Hosts.getLocalHost());
		this.diffusionnode.setHost(Hosts.getLocalHost());
		if(isInitialized())
		{
			synchronized (this) {
				registry.installLocaly();
				list.installLocally();
			}
			for (PhysicalNode p : registry.getAll() ) {
				if(p!=null)
				{
					p.installLocally();
				}
			}
			DebugCheckAllThere();
		}
	}

	public ArrayList<DiffusionAddress> getAllDiffusionAdresses() {
		ArrayList<DiffusionAddress> adresses= new ArrayList<DiffusionAddress>();
		this.getDiffusionnode().getAllAddresses(adresses);
		return adresses;
	}


	@Override
	public boolean establishDiffusionGrid(int maxcount) {
		if(!isInitialized()) return false;
		boolean haschanged =  establishDiffusionGrid(maxcount,registry.getAllObjectReferences(),this.getDiffusionnode());

		while(DiffusionNodeManager.I().ajustSizeToNeighbours())
		{
			haschanged = true;
		}
		return haschanged;
	}


	private boolean establishDiffusionGrid(int maxcount, ArrayList<ObjectReference> o,AbstractDiffusionNode n)
	{
		boolean haschanged = false;
		if(o.size()<maxcount) return haschanged;
		ArrayList<AbstractDiffusionNode> newnodes; 

		if(n.isLeaf())
		{
			OutD.println("->introduceLayer "+n.getAddress().id);
			newnodes= n.introduceLayer();
			haschanged = true;
		}
		else
		{
			newnodes = n.getSubnodes(); 
		}

		ArrayList<ArrayList<ObjectReference>> newos = new ArrayList<ArrayList<ObjectReference>>();
		for(int i =0;i<newnodes.size();i++)
		{
			newos.add(new ArrayList<ObjectReference>());
		}
		for(ObjectReference r: o)
		{
			for (int i =0;i< newnodes.size();i++) {
				if(newnodes.get(i).contains(r.getPosition()))
				{
					newos.get(i).add(r);
				}
			}
		}
		for(int i =0;i<newnodes.size();i++)
		{
			haschanged |=establishDiffusionGrid(maxcount, newos.get(i), newnodes.get(i));
		}
		return haschanged;
	}





	@Override
	public AbstractDiffusionNode getDiffusionNode() {
		return getDiffusionnode();
	}

	@Override
	public void changeAddress(ArrayList<DiffusionAddress> adds) {
		for (DiffusionAddress s: adds) {
			this.diffusionnode.checkchanged(s);
		}
		for ( DiffusionAddress d: adds) {
			this.diffusionnode.checkchanged(d);
		}

	}



	@Override
	public void clearCash() {

	}



	public boolean contains(ObjectReference objectReference) {
		if(!isInitialized()) return false;
		return registry.containsKey(objectReference);
	}



	@Override
	public boolean prefetch() {

		return true;
	}




	public void getAllneighbours(ArrayList<PartitionAddress> neighbouringpartitions,ArrayList<PartitionManager> tobesent) {

		for (PartitionAddress p : address.getNeighbours()) {

			if(!p.getHost().equals(address.getHost()))
			{
				neighbouringpartitions.add(p);
			}

		}
	}


	public void debug_checkList() {
		if(!isInitialized()) return;
		registry.debug_checkifListcontained(list);

	}

	@Override
	public void debug_print() {
		if(!isInitialized()) return;
		list.debug_print();
	}

	public void debug_checkLocalityOfPhyisics()
	{
		if(!isInitialized()) return;
		if(!Hosts.isOnLocalHost(this.getAddress().getHost())) throw new RuntimeException("this should be local!");
		for (PhysicalNode n : registry.getAll()) {
			if(!n.getSoNode().isLocal())
				throw new RuntimeException("all of this should be local!");
		}
	}

	public void debug_checkIfRefAtTheRightPlace()
	{
		Iiterator iter = getIterator();
		if(!isInitialized()) return;
		iter.next();

		int counter = 0;

		while(!iter.isAtEnd())
		{
			ObjectReference l= iter.getCurrent();
			counter++;
			if(!this.address.containsCoordinates(l.getPosition()))
			{
				OutD.println(this.address);
				OutD.print("");
				OutD.println(l);
				OutD.println(ORR.I().get(l.address));
				AbstractPartitionManager temp2 = ManagerResolver.I().resolve(l.partitionId);
				if(!temp2.getAddress().containsCoordinates(l.getPosition()))
				{
					System.out.println("not event this contains cords222!!!");
				}
				System.out.println("not event this contains cords!!!");

			}
			iter.next();
		}
		if(counter!=list.count())
		{
			System.out.println("not the same damn!");
		}
	}

	public static void debug_checkright()
	{
		for (PartitionManager pm : ManagerResolver.I().getLocalPartitions()) {
			pm.debug_checkIfRefAtTheRightPlace();
		}
	}

	public String toString()
	{
		return "pma = "+this.address+"";
	}




	public boolean shallIBeProcessed(ObjectReference r)
	{
		if(!isInitialized()) return false;
		return list.shallIBeProcessed(r);
	}

	public void setDependeciesOnHosts() {
		if(!isInitialized()) return;
		for (PhysicalNode p : registry.getAll() ) {
			p.checkHostDependence();

		}

	}

	public int countRemoteReferences(String mostBooredHost) {
		int i = 0;
		if(!isInitialized()) return 0;
		for (PhysicalNode p : registry.getAll() ) {
			if(p.getDependingHosts().contains(mostBooredHost))
			{
				i++;
			}
		}
		return i;
	}

	public transient ArrayList<PartitionManager> searchpartitions; 
	public void gatherImportantLocalNeighbours()
	{
		searchpartitions = new ArrayList<PartitionManager>();
		for(PartitionManager p: ManagerResolver.I().getLocalPartitions())
		{
			//	OutD.println(p.address+"still checking", "red");
			double d1[] =  p.getAddress().getCenter();
			double d2[] = this.getAddress().getCenter();
			double absdist[] = Matrix.subtract(d1,d2);
			double b1[] = Matrix.subtract(p.getAddress().getCenter(),p.getAddress().getLowerRightCornerBack());
			double b2[] =  Matrix.subtract(getAddress().getCenter(),getAddress().getLowerRightCornerBack());

			//are the two cuboids closer then 3x radius if so discard!
			absdist[0] = Math.abs(absdist[0])-b1[0]-b2[0]-SpaceNodeFacade.current_max_radius*2.5;
			if(absdist[0]>0)continue;
			absdist[1] = Math.abs(absdist[1])-b1[1]-b2[1]-SpaceNodeFacade.current_max_radius*2.5;
			if(absdist[1]>0)continue;
			absdist[2] = Math.abs(absdist[2])-b1[2]-b2[2]-SpaceNodeFacade.current_max_radius*2.5;
			if(absdist[2]>0)continue;	
			searchpartitions.add(p);
		}
		//		ShowConsoleOutput.println("size of neighborus : "+searchpartitions.size());
	}

	private boolean isInitialized()
	{
		return list!=null && list!=null;
	}


	public  ArrayList<PartitionManager> getSerachPartitions() {
		// TODO Auto-generated method stub
		return searchpartitions;
	}


	public void DebugCheckallNodes() {
		if(registry == null) return;
		registry.DebugCheckAllNodes_afterRemoval();
		list.DebugCheckAllNodes_afterRemoval();
		ArrayAccessHashTable nodes = MultiThreadScheduler.getNodesToProcess();
		for(int i = 0;i<nodes.size();i++)
		{
			PhysicalNode s = nodes.get(i);
			if(s==null) continue;
			ObjectReference ref =s.getSoNode().getObjectRef();

			if(ref.partitionId!=this.address.address) continue;
			if(!registry.containsKey(ref))
			{
				System.out.println("missing!");
			}
			if(!list.contains(ref))
			{
				System.out.println("missing!");
			}
		}


	}
	
	public void DebugCheckSameContents(ObjectReference r)
	{
		boolean tested = false;
		int i =0;
		while(!tested)
		{
			i++;
			try{
				if(i>100)
				{
					System.out.println("bigger 100");
				}
				if(registry.containsKey(r)!=this.list.contains(r))
				{
					Boolean a =registry.containsKey(r);
					Boolean b = this.list.contains(r);
					System.out.println("error");
				}
				tested = true;
			}
			catch (Exception e) {
				System.out.println("exception ignore...");
			}
		}
	}
	
	private void DebugCheckAllThere()
	{
		for (PhysicalNode p : registry.getAll() ) {
			if(p!=null)
			{
				ObjectReference ref = p.getSoNode().getObjectRef();
				DebugCheckSameContents(ref);
				//	OutD.println("installing "+ref.address+" locally"+ref.getPartitionId().getHost(),"blue");
				//	p.getSoNode().getObjectRef();
			}
		}
		Iiterator iter = list.getIterator();
		while(!iter.isAtEnd()) {
			ObjectReference p = iter.getCurrent();
			if(p!=null)
			{
				DebugCheckSameContents(p);
				//	OutD.println("installing "+ref.address+" locally"+ref.getPartitionId().getHost(),"blue");
				//	p.getSoNode().getObjectRef();
			}
			iter.next();
		}

	}
	
}


