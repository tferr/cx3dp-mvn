package ini.cx3d.gui.physics;

import ini.cx3d.Param;
import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.spacialOrganisation.AbstractPartitionManager;
import ini.cx3d.spacialOrganisation.Iiterator;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.PartitionManager;
import ini.cx3d.spacialOrganisation.slot.DirtyLinkedList;
import ini.cx3d.spacialOrganisation.slot.Slot;

import java.awt.Color;
import java.awt.Graphics;



public class SphereDrawer extends Drawer{

	public SphereDrawer()
	{
		super();
		this.name = "Physical Spheres";
	}

	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.black);
		//		drawCuboid(cube);
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
								//								g.setColor(Color.gray);
								//								drawCuboid(s.getSlotCuboid(i));
								if(s.doesSlotContain(i))
								{

									DirtyLinkedList<ObjectReference> temp = s.getSlotContent(i);
									for(int f=0;f<temp.size();f++)
									{
										try
										{
											PhysicalNode node = p.getPhysicalNode(temp.get(f));
											if(node instanceof PhysicalSphere)
											{
												processView((PhysicalSphere)node,g);
											}
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

	public void processView(PhysicalSphere s,Graphics g)
	{
		g.setColor(s.getColor());
		drawCyrcle( new Vector3d(s.getSomaElement().getPhysical().getSoNode().getPosition()), s.getDiameter()/2);
	}	

}
