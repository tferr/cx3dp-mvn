package ini.cx3d.spacialOrganisation.commands;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.spacialOrganisation.AbstractPartitionManager;
import ini.cx3d.spacialOrganisation.ManagedObjectBalancer;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ORR;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.SingleRemotePartitionManager;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;
import ini.cx3d.spacialOrganisation.slot.Slot;
import ini.cx3d.utilities.Compressor;
import ini.cx3d.utilities.HashT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RemoteMarginBox<T> extends AbstractSimpleCommand<Boolean>
{
	private static final long serialVersionUID = 8271520063958624L;

	
	private byte [] data;
	private transient HashT<Long, PhysicalNode> nodes;
	public double stage;
	private double [] max = new double[] {-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE};
	private double [] min = new double[] {Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};

	private boolean remotemarginsupdate;


	public String thishost;
	
	public void add(Long l,PhysicalNode n)
	{
		nodes.put(l, n);
	}

	
	
	public RemoteMarginBox(
			HashT<Long, PhysicalNode> nodes,double stage,String host) {
		
	
		this.nodes = nodes;
		this.stage = stage;
		this.thishost = host;
	}

	

	public void prepareSend()
	{
//		ShowConsoleOutput.println("- prepared to be sent!"+stage);
		ByteArrayOutputStream o=new ByteArrayOutputStream(1000);
		DataOutputStream s = new DataOutputStream(o);
		for(PhysicalNode n:nodes.values())
		{
			
				try {
					
					ObjectReference ref = n.getSoNode().getObjectRef();
					s.writeLong(ref.address);
					max = getMax(max,ref.getPosition());
					min = getMin(min,ref.getPosition());
					s.writeDouble(ref.getPosition()[0]);
					s.writeDouble(ref.getPosition()[1]);
					s.writeDouble(ref.getPosition()[2]);
					s.writeDouble(ref.getRadius());
		
					
					boolean t= (n instanceof PhysicalSphere);
					s.writeBoolean(t);
					n.serialize(s);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
		try {
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
//		byte []  compressed = Compressor.compress(data);
//		byte []  decompressed = Compressor.decompress(data);
		
	}
	
	public void prepareApply()
	{
//		ShowConsoleOutput.println("about to prepare Apply");
		data=Compressor.decompress2(data);
		ByteArrayInputStream o=new ByteArrayInputStream(data);
		DataInputStream s = new DataInputStream(o);
		
		SingleRemotePartitionManager spm = ManagerResolver.I().getHostManager(this.thishost);
		spm.setMin(max,min);
		Slot slot = new Slot(min,max, 30);
		HashT<Long,PhysicalNode> register= new HashT<Long, PhysicalNode>();
		
		int i = 0;
		try {
			while(s.available()>0)
			{
				long id = s.readLong();
				double d[] = new double[3];
				d[0] = s.readDouble();
				d[1] = s.readDouble();
				d[2] = s.readDouble();
				double radius= s.readDouble();
				ObjectReference ref = new ObjectReference(id, d, spm.getAddress().address);
				ref.setRadius(radius);
				ObjectReference ref2 = ORR.I().get(id);
				
				if(ref2!=null)
				{
					AbstractPartitionManager potentialpos = ManagerResolver.I().resolve(ref2.partitionId);
					if(potentialpos!=null)
					{
						PhysicalNode n = potentialpos.getPhysicalNode(ref2);
						if(n!=null && n.getSoNode().isLocal())
						{
							
							boolean t= s.readBoolean();
							PhysicalNode n2 = t? new PhysicalSphere(): new PhysicalCylinder();
							//n.setSoNode(spn);
							n2.deserialize(s);
							continue;
						}
					}
				}
				
					
					
				OutD.println("remoteMargin"+ref.address+" from "+client,"blue");
				ORR.I().put(ref);
				SpaceNodeFacade spn = new SpaceNodeFacade(ref);
	
				boolean t= s.readBoolean();
				PhysicalNode n = t? new PhysicalSphere(): new PhysicalCylinder();
				n.setSoNode(spn);
				n.deserialize(s);
				
				
//				ShowConsoleOutput.println("from remote"+ref.address+" "+ORR.I().get(ref.address).partitionId);
				
				SpaceNodeFacade.checkradiusformax(n.getSoNode().getRadius());
				slot.insert(ref);
				register.put(ref.address, n);
				
				i++;
			}
			s.close();
			o.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		spm.setRemoteNodes(slot,register,stage);
		if(remotemarginsupdate)
		{
			spm.update(stage);
			
		}
		
//		ShowConsoleOutput.println("PartionManager_sendMarginBoxes3.prepareApply()");
//		ShowConsoleOutput.println("added remote  i="+i);
	}


	@Override
	public boolean apply() 
	{
		//ShowConsoleOutput.println("****** applied for "+p.address);
//		TimeToken token = Timer.start("customDeSer");
		prepareApply();
//		Timer.stop(token);
		return false;
	}
	
	public void setUpdateRemoteMargins(boolean b) {
		this.remotemarginsupdate = b;
		
	}



	public void setCurrent() {
		SingleRemotePartitionManager spm = ManagerResolver.I().getHostManager(this.thishost);
		spm.setStage(stage);
	}
	
	
	protected double[] getMin(double[] a, double[] b) {
		double [] temp = new double[3]; 
		temp[0]  = Math.min(a[0], b[0]);
		temp[1]  = Math.min(a[1], b[1]);
		temp[2]  = Math.min(a[2], b[2]);
		return temp;
	}


	protected double[] getMax(double[] a, double[] b) {
		double [] temp = new double[3]; 
		temp[0]  = Math.max(a[0], b[0]);
		temp[1]  = Math.max(a[1], b[1]);
		temp[2]  = Math.max(a[2], b[2]);
		return temp;
	}
}



