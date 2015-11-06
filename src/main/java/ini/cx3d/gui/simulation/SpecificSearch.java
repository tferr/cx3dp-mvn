package ini.cx3d.gui.simulation;


import ini.cx3d.Param;
import ini.cx3d.fcode.helper.Constants;
//import ini.cx3d.fcode.highLevelMachines.helper.CtxConstants;

import java.util.Random;
import java.awt.Color;
import java.awt.Color;

import ini.cx3d.gui.Drawer;
import ini.cx3d.gui.MonitoringGui;
import ini.cx3d.gui.Vector3d;

import ini.cx3d.biology.Cell;
import ini.cx3d.biology.CellElement;
import ini.cx3d.biology.CellFactory;
import ini.cx3d.biology.LocalBiologyModule;
import java.util.ArrayList; 

import ini.cx3d.gui.ExternalWindow;
import ini.cx3d.gui.Drawer;

//import ini.cx3d.lineage.DivisionModule;
import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.physics.diffusion.DiffusionNodeManager;
import ini.cx3d.simulation.ECM;
import ini.cx3d.simulation.SimulationState;
//import ini.cx3d.simulations.fred.ctx.Launch;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.spacialOrganisation.slot.DirtyLinkedList;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.Matrix;
import ini.cx3d.utilities.Timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

//import for mouse activity
import javax.swing.JTextArea;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

//import for regular expression
import java.util.regex.*;


/** 
 * Creates new Window Frame that will be used as a control panel 
 * to track daughter cells
 *  
 * @author gabrielamichel
 *
 */
public class SpecificSearch extends ExternalWindow implements MouseListener {

		public static SpecificSearch current;

		public static int checked;

		public static int checkedIntense;

		public static int taken;

		public static int nieghbours;
	
		DefaultListModel  shown = new DefaultListModel(); 
	    public SpecificSearch() {
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
	    
	    JPanel completionPanel;
	    JList list;
	    JScrollPane scrollPane; 
	    JButton button, buttonS, button2, button3, button4, button5;
	    JTextField searchString, searchString1, searchString2;
	    JLabel titleLabel, daughterLabel, numberLabel, IDLabel, cellLabel, mouseLabel, mouseMoveLabel, stringLabel, selectionLabel;
	    JTextArea mouseEvents;
	    
	    private void initialize() {
	       
	    	// Prepare Frame properties
	        this.setTitle("Specific Daughter Finder");
	        this.setSize(190, 450);
	        this.setLayout(null); // Don't use any preset layout, define object positions manually
	     
	        
	        // Use a Scroll Pane to add contents of list
	        list = new JList(shown);	  
	        scrollPane = new JScrollPane(list);
	        scrollPane.setLocation(10, 340);//90, 310
	        scrollPane.setSize(170, 60);
	        getContentPane().add(scrollPane); 
	        
	     
	        // Add first lines
	        titleLabel = new JLabel("Type mother number");//object name
	        titleLabel.setLocation(20,10);
	        titleLabel.setSize(210, 30);
	        getContentPane().add(titleLabel);
	          
	        
	        // Number Label
	        daughterLabel = new JLabel("Daughter count");//
	        daughterLabel.setLocation(30, 130);
	        daughterLabel.setSize(210, 30);
	        getContentPane().add(daughterLabel);
	        
	        
	        // Username Label
	        numberLabel = new JLabel("");
	        numberLabel.setForeground(new Color(50,205,50));
	        numberLabel.setLocation(80, 160);
	        numberLabel.setSize(70, 40);
	        getContentPane().add(numberLabel);
	        
	        
	        //searchString
	        searchString = new JTextField(8);
	        searchString.setLocation(10, 40);
	        searchString.setSize(150, 30);
	        searchString.addActionListener(new ActionListener() {
	        	public void actionPerformed(ActionEvent e) {
	        		
	        	}
	        });
	        getContentPane().add(searchString);
	        
	        //searchString2
		     searchString2 = new JTextField(8);
		     searchString2.setLocation(190, 40);
		     searchString2.setSize(150, 30);
		     searchString2.addActionListener(new ActionListener() {
		        	public void actionPerformed(ActionEvent e) {	
		        	}
		        });
		   //     getContentPane().add(searchString2);
	        
		        
	        // Button 
	        // First define properties of button
	        button = new JButton("Color Daughters");
	        button.setLocation(10, 70);
	        button.setSize(150, 30);
	        button.addActionListener(new ActionListener() {
	        	public void actionPerformed(ActionEvent e) {
	        		
	        		ECM.selectionType = 0;//&3;
					ECM.drawDaughterTracker = true;
					//ECM.searchInt = Integer.parseInt(searchString.getText().trim());
					ECM.searchRegex = searchString.getText().trim();
					ECM.searchIntLength = searchString.getText().length();
			//		searchString.replace(("x"), ("0|1"));
				//	ThreadHandler.togglePause();
					numberLabel.setText(""+ECM.daughterCount);
					MonitoringGui.getCurrent().repaint();
					
	        	}
	        });
	        getContentPane().add(button);
					
				
	     // ButtonS 
	        // First define properties of button
	        buttonS = new JButton("Color HSV");
	        buttonS.setLocation(10, 100);
	        buttonS.setSize(150, 30);
	        buttonS.addActionListener(new ActionListener() {
	        	public void actionPerformed(ActionEvent e) {
	        		
	        		ECM.selectionType = 1;
					ECM.drawDaughterTracker = true;
					//ECM.searchInt = Integer.parseInt(searchString.getText().trim());
					ECM.searchRegex = searchString.getText().trim();
					ECM.searchIntLength = searchString.getText().length();
				//	ThreadHandler.togglePause();
					numberLabel.setText(""+ECM.daughterCount);
					MonitoringGui.getCurrent().repaint();
					
	        	}
	        });
	        getContentPane().add(buttonS);
	       
	        
	        
	        
	     // Button2 (Clear button)
	        button2 = new JButton("Clear");
	        button2.setLocation(45, 200);//135, 170
	        button2.setSize(80, 30);
	        button2.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	{
	            	searchString.setText("");
	            	ECM.drawDaughterTracker = false;
	            	numberLabel.setText("");
	            	}
	            }
	        });
	        getContentPane().add(button2); 

			
	     // Button3 
	        button3 = new JButton("Pause");
	        button3.setLocation(45, 230);//135 200
	        button3.setSize(80, 30);
	        button3.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	ThreadHandler.togglePause();
	            }
	        });
	        getContentPane().add(button3);
	        
	        
	     // Button4 
	        button4 = new JButton("Close");
	        button4.setLocation(45, 260);//135 230
	        button4.setSize(80, 30);
	        button4.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	               SpecificSearch.this.setVisible(false);	     //close Daughter Finder         
	            }
	        });
	        // Add configured button to Frame
	        getContentPane().add(button4);
	        
	         
	        // Button5 
	        button5 = new JButton("Restart");
	        button5.setLocation(45, 290);//135, 260
	        button5.setSize(80, 30);
	        button5.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	             //  DaughterTracker.this.setVisible(false);	     //close Daughter Finder         
	            }
	        });
	        // Add configured button to Frame
	        getContentPane().add(button5);
	        
	        
	        
	 /*    // ID Label
	        IDLabel = new JLabel("cell ID");//
	        IDLabel.setLocation(70, 360);
	        IDLabel.setSize(170, 30);
	        IDLabel.setHorizontalAlignment(0);
	        getContentPane().add(IDLabel);
	        
	        
	      // Cell Label
	        cellLabel = new JLabel("XXX");
	        cellLabel.setForeground(Color.red);
	        cellLabel.setLocation(140, 390);
	        cellLabel.setSize(80, 30);
	        getContentPane().add(cellLabel);
	        
	 
	      // Mouse Label
	        mouseLabel = new JLabel("Interactive Label");
	        mouseLabel.setForeground(Color.blue);
	        mouseLabel.setLocation(140, 410);
	        mouseLabel.setSize(80,30);
	        getContentPane().add(mouseLabel);*/

	    }
	      //  mouseLabel.addMouseListener(new MouseListener()
	    	        
	    	     
	  /*      
	    	 
	        {
	                    
	            @Override
	            public void mouseClicked(MouseEvent e)
	            {              
	                mouseLabel.setText("I've been clicked!");
	                mouseEvents.append("MouseClicked Event");
	                mouseEvents.append(e.getClickCount() + " click(s)\n");
	                mouseEvents.append("Xpos: " + e.getX() + " Ypos: " + e.getY() + "\n");              
	             }

	            
	             public void mousePressed(MouseEvent e)
	             {
	                        
	                mouseLabel.setText("You're holding the mouse button aren't you?");
	                mouseEvents.append("MousePressed Event\n");
	             }
	                    
	             @Override
	             public void mouseExited(MouseEvent e)
	             {
	                mouseLabel.setText("The mouse has run away!");
	                mouseEvents.append("MouseExited Event\n");
	             }
	                    
	             @Override
	             public void mouseEntered(MouseEvent e)
	             {   
	                mouseLabel.setText("I can feel the presence of the Mouse");
	                mouseEvents.append("MouseEntered Event\n");
	             }
	                    
	             @Override
	             public void mouseReleased(MouseEvent e)
	             {        
	                mouseLabel.setText("You've let go of the mouse button");
	                mouseEvents.append("MouseReleased Event\n");
	             }
	                    
	        });
	        
	        mouseMoveLabel = new JLabel("Drag the Mouse!");
	       

	        //code for the MouseMotionListener goes here..

	        mouseEvents = new JTextArea("The Mouse events can be seen here:\n");
	        JScrollPane textScroll = new JScrollPane(mouseEvents);

	        //code for the MouseWheelListener goes here..

   // 	        guiFrame.add(mouseMoveLabel, BorderLayout.WEST);
   // 	        guiFrame.add(mouseLabel, BorderLayout.CENTER);
   // 	        guiFrame.add(textScroll, BorderLayout.EAST);
   // 	        guiFrame.setVisible(true);
	    }
//	    	}
*/ 
	      
	        
	    	        

		@Override
		public void updateWindow() {
			shown.clear();
			try{
				ECM ecm = ECM.getInstance();
				
				
				shown.addElement("Cells count:"+ecm.getPhysicalSphereList().size());
				shown.addElement("Daughter count: "+ECM.daughterCount);
				numberLabel.setText(""+ECM.daughterCount);
				shown.addElement(" generation 0:"+ECM.generationOCounter);
				shown.addElement(" generation 1:"+ECM.generation1Counter);
				shown.addElement(" generation 2:"+ECM.generation2Counter);
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
		
				OutD.println("total global count: "+SimulationState.getTotalGlobalNodeCount());
				OutD.println("1000*tottime/count"+1000*Timer.getLastTotal()/(1.0*SimulationState.getTotalGlobalNodeCount()));
				OutD.println("av 1000*tottime/count"+averagedObjectCount(1000*Timer.getLastTotal()/(1.0*SimulationState.getTotalGlobalNodeCount())));
				OutD.println("Average processing Time:"+SimulationState.getLocal().avproessingTime_in_ms);
				

			}
			catch (Exception e) {
				OutD.println("");
			}
			
		}
		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	    
}
