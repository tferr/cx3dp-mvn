/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.spacialOrganisation;

import ini.cx3d.Param;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.physics.PhysicalBond;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.diffusion.AbstractDiffusionNode;
import ini.cx3d.simulation.ECM;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class SpaceNodeFacade implements SpatialOrganizationNode{
	/**
	 * 
	 */
	public static int checked=0;
	public static int checkedIntense=0;
	public static int taken = 0;
	public static int nieghbours = 0;
	private static final long serialVersionUID = -3505291253427014257L;
	private static final int refetchround = 0;
	private long ref;
//	private double radius;
	
	private static double future_maxradius = 0;
	public static double current_max_radius = 0;
	
	public static void checkradiusformax(double d)
	{
		future_maxradius = Math.max(future_maxradius, d);
		current_max_radius =  Math.max(current_max_radius,future_maxradius);
	}
	
	public static void setnewMaxRadius()
	{
		current_max_radius =  future_maxradius;
		future_maxradius = 0;
	}
	
	
	private transient HashMap<Long,PhysicalNode> current_physicalnodes ;
	private transient double lastecmupdateTime = -1;


	public SpaceNodeFacade(ObjectReference ref)
	{
		//		ShowConsoleOutput.println("created an object with ref "+ref.address);
		this.ref= ref.address;
	}


	public long getID() {
		return ref;
	}

	int count=0;
	public Collection<PhysicalNode> getNeighbors() {

		
		if(this.lastecmupdateTime+refetchround*Param.SIMULATION_TIME_STEP <ECM.getInstance().getECMtime() || current_physicalnodes==null)
		{
			//System.out.println("willupdate?");
			updateDependenceiesIgnoreTimesetp();
		
		}

		return current_physicalnodes.values();

	}

	public SpaceNodeFacade getNewInstance(
			double[] position, PhysicalNode userObject)
	{

		if(!Hosts.isOnLocalHost(this.getObjectRef().getPartitionId().getHost()))
		{
			PartitionAddress pad = this.getObjectRef().getPartitionId();
			String host  = pad.getHost();
			String host2 = Hosts.getLocalHost();
			boolean o = Hosts.isOnLocalHost(this.getObjectRef().getPartitionId().getHost());
//			throw new RuntimeException("this should always be called local why is it not?"+host);
		}
		AbstractPartitionManager pm = ManagerResolver.I().resolve(getObjectRef());
		ObjectReference newref =  pm.generateObjectAddress(position);
		SpaceNodeFacade ref =  new SpaceNodeFacade(newref);
		userObject.setSoNode(ref);
		ManagerResolver.I().resolve(getObjectRef()).insert(getObjectRef(), newref,userObject);

		return ref;
	}

	public static SpaceNodeFacade getInitialNode(
			double[] position, PhysicalNode userObject)
	{
		AbstractPartitionManager pm=null;
		pm= ManagerResolver.I().getByCordinate(position);
		ObjectReference newref =  pm.generateObjectAddress(position);
		pm.insert(null, newref,userObject);
		return new SpaceNodeFacade(newref);
	}


	public  double[] getPosition() {
		long id = this.getID();
		double[] temp = null;
		ObjectReference oref = getObjectRef();
		if(oref ==null) 
			return null; 
		temp =  oref.getPosition();
		return temp;
	}


	public  PhysicalNode getUserObject()
	{
		long id = this.getID();
		PhysicalNode temp=null;
		ObjectReference re = getObjectRef();
		AbstractPartitionManager pm = ManagerResolver.I().resolve(re);
		temp =  pm.getPhysicalNode(re);
		if(temp ==null)
		{
			for (AbstractPartitionManager pm2  : ManagerResolver.I().getAllPartitions()) {
				if(pm.getPhysicalNode(re)!=null) 
				{
					System.out.println("here I am!");
				}
			}
			if(temp == null)
			{
				if(ECM.getInstance().physicalCylinderList.containsKey(id))
				{
					System.out.println("containing PhysicalCyl!!!");
				}
				
				if(ECM.getInstance().physicalSphereList.containsKey(id))
				{
					System.out.println("containing PhysicalSphere!!!");
				}
			}
		}
		return temp;
	}


	public  boolean isLocal() {
		long id = this.getID();
		ObjectReference re = getObjectRef();
		if(re==null)
		{
			OutD.println("hmmm");
			ORR.printout(id);
			re = getObjectRef();
		}
		return ManagerResolver.I().isLocal(re.partitionId);
	}


	public  void remove() {
		long id = this.getID();
		ManagerResolver.I().resolve(getObjectRef()).remove(getObjectRef());
	}


	public void setRadius(double radius) {
		long id = this.getID();
//		if(radius*2>Param.MAX_DIAMETER)
//		{
//			ShowConsoleOutput.println("wait a sec!");
//		}
		getObjectRef().setRadius(radius);
//		this.radius = radius;

	}
	
	public double getRadius() {
		return getObjectRef().getRadius();
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof SpatialOrganizationNode)) return false;
		SpatialOrganizationNode s = (SpatialOrganizationNode) o; 
		return s.getID()==this.getID();
	}

	@Override
	public int hashCode()
	{
		return (new Long(ref)).hashCode();
	}


	public  double getExtracellularConcentration(String id) {


		AbstractPartitionManager p = ManagerResolver.I().resolve(getObjectRef());
		double temp = p.getDiffusionNode().getConcentration(id, getObjectRef().getPosition());
		return  temp;

	}


	public  double getExtracellularConcentration(String id, double[] location) {

		AbstractPartitionManager p = ManagerResolver.I().getByCordinate(location);
		double temp = p.getDiffusionNode().getConcentration(id,location);

		return temp;
	}


	public double[] getExtracellularGradient(String id) {
		AbstractPartitionManager p = ManagerResolver.I().resolve(getObjectRef());
		return p.getDiffusionNode().getGradient(id, getObjectRef().getPosition());
	}


	public void modifyExtracellularQuantity(String id, double quantityPerTime) {

		AbstractPartitionManager p = ManagerResolver.I().resolve(getObjectRef());
		double deltaQ = quantityPerTime*Param.SIMULATION_TIME_STEP;
		p.getDiffusionNode().changeSubstanceQuantity(id, deltaQ,getObjectRef().getPosition());

	}


	public  double getVolume() {


		AbstractPartitionManager p = ManagerResolver.I().resolve(getObjectRef());
		double temp = p.getDiffusionNode().getCoordinatesDiffusionNode(getObjectRef().getPosition()).getVolume();

		return temp;
	}

	public  AbstractDiffusionNode getDiffusionNode()
	{

		AbstractPartitionManager p = ManagerResolver.I().resolve(getObjectRef());
		AbstractDiffusionNode temp = p.getDiffusionNode().getCoordinatesDiffusionNode(getObjectRef().getPosition());

		return temp;
	}


	


	public ObjectReference getObjectRef() {
		ObjectReference temp = ORR.I().get(ref);
		if(temp==null)
		{
			OutD.println("hmmm");
			ORR.printout(ref);
			//temp = getObjectRef();
		}
		return temp;
	}


	public void setPosition(double[] pos) {
		ORR.I().put(ManagerResolver.I().resolve(getObjectRef()).move(getObjectRef(),pos));
	}


	@Override
	public void deserialize(DataInputStream is) throws IOException {
		long address;
		long partitionid = -2000;
		double[] pos = new double[3]; 
		
		
		address= is.readLong();
		//partitionid = is.readLong();
		pos[0] = is.readDouble();
		pos[1] = is.readDouble();
		pos[2]= is.readDouble();
		//OutD.println("on deserialize"+address,"blue");
//		ObjectReference o = ORR.I().get(address);
//		if(o !=null)
//		{
//			ORR.I().put(new ObjectReference(address, pos,partitionid));
//		}
		this.ref = address;
		
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
		ObjectReference o = getObjectRef();
		os.writeLong(o.address);
	//	os.writeLong(o.partitionId);
		os.writeDouble(o.getPosition()[0]);
		os.writeDouble(o.getPosition()[1]);
		os.writeDouble(o.getPosition()[2]);
	}
	
	
	public void addedPhysicalBond(PhysicalBond p)
	{
		if(!isLocal())
		{
			SingleRemotePartitionManager pm = (SingleRemotePartitionManager) ManagerResolver.I().resolve(getObjectRef());
			pm.addPysicalBond(p);
		}
	}
	
	public boolean shallIBeProcessed()
	{
		AbstractPartitionManager p = ManagerResolver.I().resolve(getObjectRef());
		return p.shallIBeProcessed(getObjectRef());
	}

	public void updateDependenceiesIgnoreTimesetp() {	
		
		keys = new ArrayList<Long>();
		current_physicalnodes= new HashMap<Long, PhysicalNode>();
		AbstractPartitionManager pm = ManagerResolver.I().resolve(getObjectRef());
		final double radius = getRadius();
		checkradiusformax(radius);
//		TimeToken t = Timer.start("search");
		pm.searchNeigbours(getObjectRef(), current_max_radius+radius, new IObjectReferenceSearchVisitor() {
			public void visit(ObjectReference r) {
				AbstractPartitionManager p = ManagerResolver.I().resolve(r.partitionId);
				PhysicalNode n =  p.getPhysicalNode(r);
			
				if(n==null) 
				{
					OutD.println("temp temp");
					ORR.printout(r.address);
					return;
				}
				keys.add(r.address);
				current_physicalnodes.put(r.address, n);
			}
		});
//		Timer.stop(t);

		nieghbours+=current_physicalnodes.size();
		this.lastecmupdateTime= ECM.getInstance().getECMtime();
		
	}
	private transient ArrayList<Long> keys;
	private transient HashMap<Long,PhysicalNode> current_physicalnodes2 ;
	private transient double lastecmupdateTime2 = -1;
	public void updateDependenceiesIgnoreTimesetp_test() {	
		
		keys = new ArrayList<Long>();
		current_physicalnodes2= new HashMap<Long, PhysicalNode>();
		AbstractPartitionManager pm = ManagerResolver.I().resolve(getObjectRef());
		final double radius = getRadius();
		checkradiusformax(radius);
		pm.searchNeigbours(getObjectRef(), current_max_radius+radius, new IObjectReferenceSearchVisitor() {
			public void visit(ObjectReference r) {

				AbstractPartitionManager p = ManagerResolver.I().resolve(r.partitionId);
				PhysicalNode n =  p.getPhysicalNode(r);
			
				if(n==null) 
				{
					OutD.println("temp temp");
					ORR.printout(r.address);
					return;
				}
				keys.add(r.address);
				current_physicalnodes2.put(r.address, n);
			}
		});


		nieghbours+=current_physicalnodes2.size();
		this.lastecmupdateTime2= ECM.getInstance().getECMtime();
		
	}
	
	public Collection<PhysicalNode> getNeighbors_test() {

		
		if(this.lastecmupdateTime2+refetchround*Param.SIMULATION_TIME_STEP <ECM.getInstance().getECMtime() || current_physicalnodes2==null)
		{
			//System.out.println("willupdate?");
			updateDependenceiesIgnoreTimesetp_test();
		}
		
		return current_physicalnodes2.values();

	}
	
	public ArrayList<Long> getNeighborKeys()
	{
		if(this.lastecmupdateTime+refetchround*Param.SIMULATION_TIME_STEP <ECM.getInstance().getECMtime() || current_physicalnodes==null)
		{
			updateDependenceiesIgnoreTimesetp();
		}
		return keys;
	}
	
	public PhysicalNode getNeigbour(Long k) {
		return current_physicalnodes.get(k);
	}
	
	public AbstractPartitionManager resolveTest()
	{
		AbstractPartitionManager pm = ManagerResolver.I().resolve(getObjectRef());
		return pm;
	}
}
