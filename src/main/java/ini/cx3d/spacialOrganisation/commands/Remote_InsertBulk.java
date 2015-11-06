package ini.cx3d.spacialOrganisation.commands;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.physics.PhysicalBond;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.simulation.ECM;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.spacialOrganisation.AbstractPartitionManager;
import ini.cx3d.spacialOrganisation.InsertionHandler;
import ini.cx3d.spacialOrganisation.ManagedObjectBalancer;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ORR;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.PartitionAddress;
import ini.cx3d.spacialOrganisation.PartitionManager;
import ini.cx3d.spacialOrganisation.SingleRemotePartitionManager;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.Compressor;
import ini.cx3d.utilities.HashT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Remote_InsertBulk<T> extends AbstractSimpleCommand<ObjectReference>
{
	/**
	 * 
	 */
	private byte [] data;
	private static final long serialVersionUID = 55023711300408764L;
	private transient ArrayList<PhysicalNode> obs;
	private transient ArrayList<PhysicalNode> dependents = new ArrayList<PhysicalNode>();
	private transient HashT<Long,ObjectReference> ors= new HashT<Long,ObjectReference>();
	private transient HashT<Long,ObjectReference> orsdep= new HashT<Long,ObjectReference>();
	private transient ArrayList<PhysicalBond> physicalBondToinsert;
	private double stage;

	public Remote_InsertBulk(PartitionAddress p, ArrayList<PhysicalNode> obs,ArrayList<PhysicalBond> physicalBondToinsert, double stage)
	{
		this.stage = stage;
		this.obs = obs;
		this.physicalBondToinsert = physicalBondToinsert;
		for (PhysicalNode n : obs ) {
			dependents.addAll(n.getDependingNodes());
			ors.put(n.getSoNode().getID(),n.getSoNode().getObjectRef());
		}
		
		for (PhysicalBond n : physicalBondToinsert) {
			if(n.getFirstSoNode().isLocal())
			{
				dependents.add(n.getFirstPhysicalObject());
			}
			if(n.getSecondSoNode().isLocal())
			{
				dependents.add(n.getSecondPhysicalObject());
			}
		}
		
		
		for (PhysicalNode n : dependents ) {
			n.updateDependenciesIgnoreTimestep();
			orsdep.put(n.getSoNode().getID(),n.getSoNode().getObjectRef());
		}
		
		prepareSend();
		
		
		
	
	}
	
	private void prepareSend() {
		ByteArrayOutputStream o=new ByteArrayOutputStream(1000);
		DataOutputStream s = new DataOutputStream(o);
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(s);
			oos.writeObject(dependents);
			oos.writeObject(obs);
			oos.writeObject(ors);
			oos.writeObject(orsdep);
			oos.writeObject(physicalBondToinsert);
			oos.close();
			s.close();
			o.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.data=o.toByteArray();
		data = Compressor.compress2(data);
		if(data==null)
		{
			OutD.println("data was null why","pink");
		}
		
	}
	
	private void prepareApply() {
		data=Compressor.decompress2(data);
		ByteArrayInputStream o=new ByteArrayInputStream(data);
		DataInputStream s = new DataInputStream(o);
		ObjectInputStream ino;
		try {
			ino = new ObjectInputStream(s);
			dependents = (ArrayList<PhysicalNode>) ino.readObject(); 
			obs = (ArrayList<PhysicalNode>) ino.readObject();
			ors = (HashT<Long, ObjectReference>) ino.readObject();
			orsdep= (HashT<Long, ObjectReference>) ino.readObject(); 
			physicalBondToinsert= (ArrayList<PhysicalBond>) ino.readObject(); 
			ino.close();
			s.close();
			o.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean apply() {
		prepareApply();
		return !InsertionHandler.addTobeInserted(this);
	}
	public boolean execute() {	
//		TimeToken token = Timer.start("insert");
		ECM.getInstance().nodeListLock();
		
		for (PhysicalNode n : dependents ) {
			ObjectReference ref =  orsdep.get(n.getSoNode().getID());
			SingleRemotePartitionManager  pm = ManagerResolver.I().getHostManager(client);
			ref.setPartitionId(pm.getAddress());
			OutD.println("Bulkinsert "+ref.address+" "+ref.partitionId, "red");
			ORR.I().put(ref);
			pm.addToMargin(n, ref);
		}
		
		
		for (PhysicalNode n : obs) {
			ObjectReference ref =  ors.get(n.getSoNode().getID());
			AbstractPartitionManager  pm = ManagerResolver.I().getByCordinate(ref.getPosition());
			ref.setPartitionId(pm.getAddress());
			OutD.println("Bulkinsert "+ref.address+" "+ref.partitionId, "red");
			ORR.I().put(ref);
			
			if(pm instanceof PartitionManager)
			{
//				ObjectReference ref2 = ORR.I().get(ref.address);
//				ShowConsoleOutput.println("insert int n"+ref2.address+" "+ManagerResolver.I().resolve(ref2.partitionId).getAddress().getHost() +" "+ref2.partitionId,"green");
				pm.insert(null,ref , n);
		
//				ref2 = ORR.I().get(ref.address);
			//	OutD.println("insert int n"+ref2.address+" "+ManagerResolver.I().resolve(ref2.partitionId).getAddress().getHost() +" "+ref2.partitionId,"green");
				n.installLocally();
				
			}
			else
			{
				((SingleRemotePartitionManager)pm).addToMargin(n,ref);
				ref.setPartitionId(pm.getAddress());
		//		OutD.println("Bulkinsert2 "+ref.address,"yellow");
				ORR.I().put(ref);
		//		OutD.println("added change to margin"+ref.address,"green");
			}
		}
		ECM.getInstance().nodeListunLock();
		
		for (PhysicalBond n : physicalBondToinsert) {
			if(n.getFirstSoNode().isLocal())
			{
				n.getFirstPhysicalObject().addPhysicalBond(n);
			}
			if(n.getSecondSoNode().isLocal())
			{
				n.getSecondPhysicalObject().addPhysicalBond(n);
			}
			
		}
		
		
		
		for (PhysicalNode n : obs) {
			n.updateDependenciesIgnoreTimestep();
		}
		
		
		
//		ShowConsoleOutput.println("end insert");
//		Timer.stop(token);
		return false;
	}	
	
	public String toString()
	{
		return "objects "+this.obs.size()+" references ";
	}

	public double getStage() {
		
		return this.stage;
	}
}

