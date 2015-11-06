package ini.cx3d.gui.physics;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.MonitoringGui;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.PartitionManager;
import ini.cx3d.spacialOrganisation.slot.DirtyLinkedList;
import ini.cx3d.spacialOrganisation.slot.Slot;
import ini.cx3d.utilities.Cuboid;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class SphereSliceDrawer extends Drawer{



	public static Cuboid cube=genCube();
	public static double offset = 0;
	public static double thickness = 5;
	

	public static Cuboid genCube()
	{
		return new Cuboid(new double[]{-4000,-4000,offset-thickness},new double[]{4000,4000,offset+thickness});
	}

	public SphereSliceDrawer(int i)
	{
		this();
		thickness = i;
		cube = genCube();
	}
	
	public SphereSliceDrawer()
	{
		super();
		cube = genCube();
		this.name = "Slice Spheres";
	}
	@Override
	public void draw(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.black);
//		drawCuboid(cube);
		try
		{
		for(PartitionManager p: ManagerResolver.I().getLocalPartitions())
		{
			if(cube.intersects(p.address))
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


							if(cube.intersects(s.getSlotCuboid(i)))
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

			}

		}
		}catch (Exception e) {
			System.out.println("");
		}
		g.setColor(c);
	}


	public void processView(PhysicalSphere s,Graphics g)
	{
		
		if(cube.containsCoordinates(s.getSoNode().getPosition()))
		{
			g.setColor(s.getColor());
			drawCyrcle( new Vector3d(s.getSomaElement().getPhysical().getSoNode().getPosition()), s.getDiameter()/2);
//			g.setColor(Color.black);
//			drawString(new Vector3d(s.getSomaElement().getPhysical().getSoNode().getPosition()),s.getInterObjectForceCoefficient()+":"+s.getCellularElement().getCell().getType());
		}
	}

	@Override
	public void added()
	{
		MonitoringGui.getCurrent().addKeyListener(key);	
	}

	@Override
	public void removed()
	{
		MonitoringGui.getCurrent().removeKeyListener(key);	
	}
	
	public void setThickness(double t) {
		thickness = t;
		cube= genCube();
	}

	public double getThickness() {
		return thickness;
	}

	transient KeyListener  key = new KeyListener() {

		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode()) {
			case 34:
				offset+=thickness*2;
				OutD.println("changed pos"+offset);
				break;
			case 33:
				offset-=thickness*2;
				OutD.println("changed poss"+offset);
				break;
			case 77:
				offset = 0;
				thickness = 5;
				break;
			case 79:
				thickness += 5;
				OutD.println("changed thickness"+thickness);
				break;
			case 80:
				thickness -= 5;
				OutD.println("changed thickness"+thickness);
				break;
			}
			cube = genCube();
			MonitoringGui.getCurrent().repaint();

		}

		public void keyPressed(KeyEvent e) {
			//			ShowConsoleOutput.println(e.getKeyCode());
		}
	};
}
