package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.Param;
import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.MonitoringGui;
import ini.cx3d.gui.Vector3d;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ORR;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.PartitionManager;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;
import ini.cx3d.spacialOrganisation.slot.Slot;
import ini.cx3d.utilities.Cuboid;
import ini.cx3d.utilities.Matrix;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;



public class DrawSlotGrid extends Drawer{

	
	private static  SpaceNodeFacade spacenode;
	public DrawSlotGrid()
	{
		super();
		this.name = "Slot drawer";
		
		
	}
	@Override
	public void draw(Graphics g) {

			//		try
//		{
			PartitionManager m=null;
			try{
//				ObjectReference temp =ORR.I().get(14000449L);
				ObjectReference temp =ORR.I().get(1001443L);
				m = (PartitionManager)ManagerResolver.I().resolve(temp.partitionId);
				PhysicalNode n = m.getPhysicalNode(temp);
				spacenode = n.getSoNode();
			}
			catch (Exception e) {
				// TODO: handle exception
			}
			if(m == null) return;
			synchronized (g) {
				
		
				Color c = g.getColor();
				g.setColor(Color.gray);
//				for (PartitionManager pm : ManagerResolver.I().getLocalPartitions()) {
//					recursiveDrawTree(g,pm);
//				//	drawString(new Vector3d(pm.getCenter()),pm.address+"");
//				}
				for (PartitionManager pm : m.getSerachPartitions()) {
//				for (PartitionManager pm : ManagerResolver.I().getLocalPartitions()) {
					if(pm.getAddress().intersectsWithSphere(spacenode.getPosition(),  getSpacenode().getRadius()+SpaceNodeFacade.current_max_radius))
					{
						recursiveDrawInRange(g,pm);
					}
				//	drawString(new Vector3d(pm.getCenter()),pm.address+"");
				}
				
				drawNeighbours(g);
				drawNode(g);
				g.setColor(c);
			}
//		}
//		catch (Exception e) {
//			ShowConsoleOutput.println("cause???");
//			
//		}
	}
	
	public void drawColoredSlot(Slot s,int x,int y, int z)
	{
		
		double [] start = Matrix.add(s.getLowerRightCornerBack(),
				new double[]{x*s.unit[0],y*s.unit[1],z*s.unit[2]});
		double [] end = Matrix.add(start,new double[]{s.unit[0],s.unit[1],s.unit[2]});
		Cuboid c = new Cuboid(start,end);
		drawCuboid(c);
		drawString(new Vector3d(c.getCenter()),x+" "+y+" "+z);
		
	}
	
	
	
//	private void recursiveDrawTree(Graphics g,PartitionManager d2) {
//			Slot s= (Slot)d2.list;
//			g.setColor(Color.gray);
//			for(int x = 0;x<s.slots.length;x++)
//			{
//				for(int y = 0;y<s.slots.length;y++)
//				{
//					for(int z = 0;z<s.slots.length;z++)
//					{
//						drawColoredSlot(s,x,y,z);
//					}
//				}
//			}
//		
//			
//	}
	
	
	
	private void recursiveDrawInRange(Graphics g,PartitionManager d2) {

		if(getSpacenode() == null) return;
		Slot s= (Slot)d2.list;
		g.setColor(Color.green);
		s.searchRange(getSpacenode().getPosition(), getSpacenode().getRadius()+SpaceNodeFacade.current_max_radius, this);
		
	
	}
	
	
	private void drawNode(Graphics g)
	{
		if(spacenode==null) return;
		g.setColor(Param.X_LIGHT_BLUE);
		drawCyrcle(new Vector3d(getSpacenode().getPosition()), 2);
		g.setColor(Param.X_LIGHT_BLUE);
		Vector3d start = new Vector3d(getSpacenode().getPosition());
		double r = getSpacenode().getRadius()+SpaceNodeFacade.current_max_radius;
		Vector3d end = start.plus(new Vector3d(r,r,r));
		start = start.minus(new  Vector3d(r,r,r));
		Cuboid c = new Cuboid(start.toArray(),end.toArray());
		drawCuboid(c);
	}
	
	private void drawNeighbours(Graphics g)
	{
		if(spacenode==null) return;
		g.setColor(Color.black);
		
		for (PhysicalNode n :spacenode.getNeighbors()) {
			drawLine(new Vector3d( n.getSoNode().getPosition()),new Vector3d(spacenode.getPosition()));
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
	public static void setSpacenode(SpaceNodeFacade spacenode) {
		if(DrawSlotGrid.spacenode==null)
		{
			DrawSlotGrid.spacenode = spacenode;
		}
		if(Matrix.norm(spacenode.getPosition())< Matrix.norm(DrawSlotGrid.spacenode.getPosition()))
		{
			DrawSlotGrid.spacenode = spacenode;
		}
//		MonitoringGui.getCurrent().repaint();
	}
	public static SpaceNodeFacade getSpacenode() {
		return spacenode;
	}
	KeyListener key = new KeyListener() {
		
		@Override
		public void keyTyped(KeyEvent e) {
		
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode()) {
//			case KeyEvent.VK_5:
//				coords[0] += 1;
//				
//				break;
//			case KeyEvent.VK_6:
//				coords[0] -= 1;
//				
//				break;
//			case KeyEvent.VK_7:
//				coords[1] += 1;
//				
//				break;
//			case KeyEvent.VK_8:
//				coords[1] -= 1;
//				break;
//			
			}
			MonitoringGui.getCurrent().repaint();
			
		}
		
		public void keyPressed(KeyEvent e) {
//			ShowConsoleOutput.println(e.getKeyCode());
		}
	};
	
}
