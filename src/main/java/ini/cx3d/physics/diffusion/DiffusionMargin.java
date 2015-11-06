package ini.cx3d.physics.diffusion;

import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.utilities.Compressor;
import ini.cx3d.utilities.HashT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DiffusionMargin extends AbstractSimpleCommand<Boolean> 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7391692906318708044L;
	public transient HashT<Long,AbstractDiffusionNode> tobesent = new HashT<Long,AbstractDiffusionNode>();
	public String host=null;
	private byte[] data;
	
	public DiffusionMargin()
	{
		
	}

	@Override
	public boolean apply() {
		prepareApply();
		if(host !=null)
		{
			DiffusionNodeManager.I().setPrefetched(host, tobesent);
			
		}
		
		return false;
	}
	
	
	public boolean applyUpdate() {
		prepareApply();
		if(host !=null)
		{
			DiffusionNodeManager.I().updatePrefetched(host, tobesent);
			
		}
		
		return false;
	}
	
	@Override
	public String toString()
	{
		return "diff margin";
	}

	
	public void prepareSend()
	{
		
		ByteArrayOutputStream o=new ByteArrayOutputStream(1000);
		DataOutputStream s = new DataOutputStream(o);
		try {
			s.writeInt(tobesent.size());
			for(AbstractDiffusionNode d : tobesent.values())
			{
				d.serialize(s);
				
			}
			s.close();
			o.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.data=o.toByteArray();
		data = Compressor.compress2(data);
//		byte []  compressed = Compressor.compress(data);
//		byte []  decompressed = Compressor.decompress(data);
		
	}
	
	public void prepareApply()
	{
		//data = data);
		tobesent = new HashT<Long, AbstractDiffusionNode>();
		data=Compressor.decompress2(data);
		ByteArrayInputStream o=new ByteArrayInputStream(data);
		DataInputStream s = new DataInputStream(o);
		try {
			ArrayList<DiffusionAddress> diffaddress = new ArrayList<DiffusionAddress>();
			int to = s.readInt();
			for (int i = 0;i<to;i++) {
				AbstractDiffusionNode n= new DiffusionNode();
				n.deserialize(s);
				if(host==null)
				{
					host = n.getAddress().getHost();
				}
				DiffReg.I().put(n.getAddress());
				diffaddress.add(n.getAddress());
				tobesent.put(n.address.id,n);
			}
			DiffusionNodeManager.I().applyAddressChanges(diffaddress);
			s.close();
			o.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}