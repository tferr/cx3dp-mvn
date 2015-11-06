package ini.cx3d.gui.simulation;

import ini.cx3d.gui.ExternalWindow;
import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.simulation.ECM;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.Timer;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class ShowObjectCount extends ExternalWindow {

		public static ShowObjectCount current;

		public static int checked;

		public static int checkedIntense;

		public static int taken;

		public static int nieghbours;
	
		DefaultListModel  shown = new DefaultListModel(); 
	    public ShowObjectCount() {
	    	current = this;
	        initialize();
	        this.name = "show object count";
	    }
	    
	    double [] objectcount = new double[30];
	    int i = 0;
	    private double averagedObjectCount(double d)
	    {
	    	
	    	objectcount[i]=d;
	    	i=(i+1)%objectcount.length;
	    	double temp = 0;
	    	for (double o : objectcount) {
				temp+=o;
			}
	    	return temp/objectcount.length;
	    }
	    
	    private void initialize() {
	       
	        this.setTitle("show object count");
	        this.setSize(300, 500);
	     
	        JList list = new JList(shown);
	  
	        JScrollPane scrollPane = new JScrollPane(list);

	        JButton button = new JButton("Close");
	        button.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	               ShowObjectCount.this.setVisible(true);
	               
	            }
	        });
	        JButton button2 = new JButton("Pause");
	        button2.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	ThreadHandler.togglePause();
	            }
	        });
	        
	        
	        
	        this.setLayout(new BorderLayout(5, 5));
	        getContentPane().add(scrollPane, BorderLayout.CENTER);
	        getContentPane().add(button, BorderLayout.NORTH);
	        getContentPane().add(button2, BorderLayout.SOUTH);
	        
	    }

		@Override
		public void updateWindow() {
			shown.clear();
			try{
				ECM ecm = ECM.getInstance();
				shown.addElement("time: "+ecm.getECMtime());
				shown.addElement("Sphere count:"+ecm.getPhysicalSphereList().size());
				shown.addElement("Cylinders count:"+ecm.getPhysicalCylinderList().size());
				shown.addElement("Spacenodes count:"+ManagerResolver.I().getTotalLocalObjectsCount());
				shown.addElement("Partitionmanager count:"+ManagerResolver.I().getLocalPartitions().size());
//				shown.addElement("Remote pm count:"+ManagerResolver.I().getAllRemotePartitionAddresses().size());
				shown.addElement("Diffusionnode count:"+DiffusionNodeManager.I().getDiffusionNodeCount());
				shown.addElement("nodescounter "+ECM.getInstance().getPhysicalNodes());
				shown.addElement("cycle_counter: "+SimulationState.getLocal().cycle_counter);
				shown.addElement("stage counter: "+SimulationState.getLocal().stagecounter);
				shown.addElement("recivecount: "+SimulationState.getLocal().recivecount);
				shown.addElement("sendcount: "+SimulationState.getLocal().sendcount);
				shown.addElement("total count: "+SimulationState.getLocal().totalObjectCount);
				shown.addElement("total global count: "+SimulationState.getTotalGlobalNodeCount());
				shown.addElement("1000*tottime/count"+1000*Timer.getLastTotal()/(1.0*SimulationState.getTotalGlobalNodeCount()));
				shown.addElement("av 1000*tottime/count"+averagedObjectCount(1000*Timer.getLastTotal()/(1.0*SimulationState.getTotalGlobalNodeCount())));
				shown.addElement("Av pT:"+SimulationState.getLocal().avproessingTime_in_ms);
				shown.addElement("checkedtaken:"+taken*1.0/SimulationState.getLocal().totalObjectCount);
				shown.addElement("checked:"+checked*1.0/SimulationState.getLocal().totalObjectCount);
				shown.addElement("intensecheck:"+checkedIntense*1.0/SimulationState.getLocal().totalObjectCount);
				shown.addElement("neighbours:"+nieghbours*1.0/SimulationState.getLocal().totalObjectCount);
				long nexttime = System.currentTimeMillis();
				
				
				shown.addElement("time in milies "+(nexttime-lasttime));
				shown.addElement("next: "+Hosts.getNextHost()+" "+Hosts.isActive(Hosts.getNextHost()));
				shown.addElement("prev: "+Hosts.getPrevHost()+" "+Hosts.isActive(Hosts.getPrevHost()));
//				for (String host : Hosts.getHosts()) {
//					shown.addElement(host+" : "+ConnectionManager.getSender(host).transmissionSpeed()+" mbit/s ");
//					shown.addElement(host+" : "+ConnectionManager.getSender(host).getTransmitted()+" mbyte");
//				}
				lasttime=nexttime;
				//shown.addElement("remote hosts"+Hosts.getHosts().size());
			}
			catch (Exception e) {
				OutD.println("");
			}
		}
		private int max;
		private long lasttime=0;
		private long lasttime2=0;
		private double getAverageNeighbourcount ()
		{
			max = 0;
			double size = 0;
			ArrayAccessHashTable temp = ECM.getInstance().getPhysicalNodes();
			for(int i=0;i<temp.size();i++)
			{
				PhysicalNode s = temp.getIfTobeProcessed(i);
				if(s==null) continue;
				int tempi = s.getNeighboringPhysicalNodes().size();
				max = Math.max(max, tempi);
				size += tempi;
			}
			
			size/=ECM.getInstance().getPhysicalNodes().size();
			return size;
		}
		
		public synchronized void paint(Graphics g)
		{
			try{
				super.paint(g);
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
	
		
		public void printout()
		{
			try{
				ECM ecm = ECM.getInstance();
				long nexttime = System.currentTimeMillis();
				OutD.println("time in milies "+(nexttime-lasttime2));
			//	ShowConsoleOutput.println("Sphere count:"+ecm.getPhysicalSphereList().size());
			//	ShowConsoleOutput.println("Cylinders count:"+ecm.getPhysicalCylinderList().size());
			//	ShowConsoleOutput.println("Spacenodes count:"+ManagerResolver.I().getTotalLocalObjectsCount());
			//	ShowConsoleOutput.println("Partitionmanager count:"+ManagerResolver.I().getAllLocalPartitionManagers().size());
//				shown.addElement("Remote pm count:"+ManagerResolver.I().getAllRemotePartitionAddresses().size());
			//	ShowConsoleOutput.println("Diffusionnode count:"+DiffusionNodeManager.I().getDiffusionNodeCount());
			//	ShowConsoleOutput.println("Localhost name "+Hosts.getLocalHost());
			//	ShowConsoleOutput.println("cycle_counter: "+SimulationState.getLocal().cycle_counter);
			//	ShowConsoleOutput.println("stage counter: "+SimulationState.getLocal().stagecounter);
			//	ShowConsoleOutput.println("recivecount: "+SimulationState.getLocal().recivecount);
			//	ShowConsoleOutput.println("sendcount: "+SimulationState.getLocal().sendcount);
			//	ShowConsoleOutput.println("total count: "+SimulationState.getLocal().totalObjectCount);
				OutD.println("total global count: "+SimulationState.getTotalGlobalNodeCount());
				OutD.println("1000*tottime/count"+1000*Timer.getLastTotal()/(1.0*SimulationState.getTotalGlobalNodeCount()));
				OutD.println("av 1000*tottime/count"+averagedObjectCount(1000*Timer.getLastTotal()/(1.0*SimulationState.getTotalGlobalNodeCount())));
				OutD.println("Average processing Time:"+SimulationState.getLocal().avproessingTime_in_ms);
				
//				ShowConsoleOutput.println("time in milies "+(nexttime-lasttime));
//				ShowConsoleOutput.println("HOSTS:");
//				for (String host : Hosts.getHosts()) {
//					shown.addElement(host);
//				}
//				ShowConsoleOutput.println("HOSTS_N:");
//				for (String host : Hosts.getInactiveHosts()) {
//					shown.addElement(host);
//				}
//				for (String host : Hosts.getHosts()) {
//					shown.addElement(host+" : "+ConnectionManager.getSender(host).transmissionSpeed()+" mbit/s ");
//					shown.addElement(host+" : "+ConnectionManager.getSender(host).getTransmitted()+" mbyte");
//				}
				lasttime2=nexttime;
				//shown.addElement("remote hosts"+Hosts.getHosts().size());
			}
			catch (Exception e) {
				OutD.println("");
			}
			
		}
		
	    
}
