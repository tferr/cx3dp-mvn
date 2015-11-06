package ini.cx3d.gui.electrophysiology;

import ini.cx3d.electrophysiology.model.Token;
import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.PhysicalBond;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.PartitionManager;
import ini.cx3d.spacialOrganisation.slot.DirtyLinkedList;
import ini.cx3d.spacialOrganisation.slot.Slot;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;


public class ElTokensDrawer extends Drawer{

	public ElTokensDrawer()
	{
		super();
		this.name = "EL Tokens";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.black);
		try
		{
			for(PartitionManager p: ManagerResolver.I().getLocalPartitions())
			{
				Slot s = (Slot)p.list;
				if(s ==null) continue;
				int [] k=s.res;
				int [] i = new int[3];
				for(i[0]=0;i[0]<k[0];i[0]++)
				{
					for(i[1]=0;i[1]<k[1];i[1]++)
					{
						for(i[2]=0;i[2]<k[2];i[2]++)
						{
							try
							{
								if(s.doesSlotContain(i))
								{
									DirtyLinkedList<ObjectReference> temp = s.getSlotContent(i);
									for(int f=0;f<temp.size();f++)
									{
										try
										{
											PhysicalNode node = p.getPhysicalNode(temp.get(f));
											processView(node,g);
											
										}catch (Exception e) {

										}

									}
								}
							}
							catch(Exception e)
							{
								System.out.println("fail");
							}

						}

					}
				}


			}
		}catch (Exception e) {
			System.out.println("");
		}
		g.setColor(c);
	}


	public void processView(PhysicalNode s,Graphics g)
	{
		ArrayList<Token> tokens;
		if(s instanceof PhysicalSphere)
		{
			PhysicalSphere sp = (PhysicalSphere)s;
			tokens  = sp.getElectroPhysiolgy().getTokens();
			if(tokens.size()>0)
			{
				g.setColor(tokens.get(0).getColor());
				try{
					drawCyrcle(new Vector3d(sp.massLocation), sp.getDiameter()/2);
				}
				catch (Exception e) {
					System.out.println("");
				}
			}
			
		}
		else if(s instanceof PhysicalCylinder)
		{
			PhysicalCylinder sp = (PhysicalCylinder)s;
			tokens  = sp.getElectroPhysiolgy().getTokens();
			if(tokens.size()>0)
			{
				g.setColor(tokens.get(0).getColor());
				try{
					drawLine(new Vector3d(sp.distalEnd()), new Vector3d(sp.proximalEnd()));
				}
				catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
		
		for(PhysicalBond n: ((PhysicalObject)s).getPhysicalBonds())
		{
			tokens = n.getElectroPhysiolgy().getTokens();
			
			if(tokens.size()>0)
			{
				g.setColor(tokens.get(0).getColor());
				try{
					
					
					drawLine(new Vector3d(n.getFirstEndLocation()), new Vector3d(n.getSecondEndLocation()));
				}
				catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}
	


}
