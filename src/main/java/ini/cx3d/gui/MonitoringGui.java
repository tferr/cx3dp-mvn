package ini.cx3d.gui;
import ini.cx3d.gui.electrophysiology.ElTokensDrawer;
import ini.cx3d.gui.physics.BoundariesDrawer;
import ini.cx3d.gui.physics.CylinderDrawer;
import ini.cx3d.gui.physics.ForceDrawer;
import ini.cx3d.gui.physics.InternalSubstanceDrawer;
import ini.cx3d.gui.physics.PhyiscalBondDrawer;
import ini.cx3d.gui.physics.ShowNeighbors;
import ini.cx3d.gui.physics.ShowTimers;
import ini.cx3d.gui.physics.SphereSliceDrawer;
import ini.cx3d.gui.physics.diffusion.ConcentrationDrawer;
import ini.cx3d.gui.physics.diffusion.ConcentrationDrawerNumbers;
import ini.cx3d.gui.physics.diffusion.DrawLocalDiffusionNeighbours;
import ini.cx3d.gui.physics.diffusion.DrawRemoteDiffusionNeighbours;
import ini.cx3d.gui.physics.diffusion.GradientDrawer;
import ini.cx3d.gui.physics.diffusion.NotRecivedDiffNode;
import ini.cx3d.gui.physics.diffusion.OctreeDrawer;
import ini.cx3d.gui.physics.diffusion.RemoteDiffusionNodeDrawer;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.gui.simulation.ShowObjectCount;
import ini.cx3d.gui.spacialOrganisation.DrawNodesToExchange;
import ini.cx3d.gui.spacialOrganisation.DrawSlotGrid;
import ini.cx3d.gui.spacialOrganisation.PartitionDrawer;
import ini.cx3d.gui.spacialOrganisation.PartitionDrawerDistribution;
import ini.cx3d.gui.spacialOrganisation.PartitionNeighbourDrawer;
import ini.cx3d.gui.spacialOrganisation.PartitionNumberDrawer;
import ini.cx3d.gui.spacialOrganisation.RemoteMarginDrawer;
import ini.cx3d.gui.spacialOrganisation.RemotePartitionDrawer;
import ini.cx3d.gui.spacialOrganisation.SpacenodeDrawer;
import ini.cx3d.gui.spacialOrganisation.WhatTheHellAreYouWaitingFor;
import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.parallelization.communication.Server;
import ini.cx3d.simulation.ECM;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.utilities.dynamicLoading.LoadDrawers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MonitoringGui extends JFrame {

	private JPanel jxypane;
	private JTextField marking = new JTextField() ;
	public ArrayList<Drawer> drawers_applied = new ArrayList<Drawer>(); 
	public ArrayList<ExternalWindow> dialogs = new ArrayList<ExternalWindow>(); 
	private JMenu fileMenu;
	private JMenu editMenu;
	private JMenu optionsMenu;
	public Canvas3d component;
	private JMenu perspectiveMenu;
	private JMenu recording;

	private static MonitoringGui current;
	public static boolean isSet()
	{
		return current!=null;
	}
	public static MonitoringGui getCurrent()
	{
		if(current ==null) current = getStandardGUI();
		return current;
	}

	public MonitoringGui() 
	{
		this(500,500);
	}
	
	public MonitoringGui(int width,int height) {
		current = this;
		MultiThreadScheduler.setGui(this);
		//this.setLayout(new )
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.add(getMarking());
		this.add(drawboard());
		setTitle("Cx3dp "+Hosts.getLocalHost());
		setSize(width, height);
		int y =0;
		int x =0;
		int serv = Server.getServer().getPort()-2222;
		if(serv%4==0)
		{
			x = 0;
			y = 0;
		}
		else if(serv%4==1)
		{
			x = 0;
			y = height;
		}
		else if(serv%4==2)
		{
			x = width;
			y = 0;
		}
		else if(serv%4==3)
		{
			x = width;
			y = height;
		}
		this.setLocation(x, y);

		// Creates a menubar for a JFrame
		JMenuBar menuBar = new JMenuBar();

		// Add the menubar to the frame
		setJMenuBar(menuBar);

		// Define and add two drop down menu to the menubar
		fileMenu = new JMenu("Draw");
		editMenu = new JMenu("ShowDialog");
		optionsMenu = new JMenu("Options");
		perspectiveMenu = new JMenu("Perspective");
		recording = new JMenu("Video Recording");
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(optionsMenu);
		menuBar.add(perspectiveMenu);
		menuBar.add(recording);
		JMenuItem pause = new JMenuItem("Pause");
		pause.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ThreadHandler.togglePause();

			}
		});
		optionsMenu.add(pause);

		JMenuItem temp1 = new JMenuItem("Add ComplexWorker");
		temp1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// 				ThreadHandler.complexWorkercount ++;
				// 				ThreadHandler.introduceNewComplexCommandWorker();

			}
		});
		optionsMenu.add(temp1);

		temp1 = new JMenuItem("Add SimpleWorker");
		temp1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// 				ThreadHandler.simpleWorkercount ++;
				// 				ThreadHandler.introduceNewSimpleCommandWorker();

			}
		});
		optionsMenu.add(temp1);

		JMenuItem temp = new JMenuItem("Reset");
		temp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				component.resetView();
				component.getParent().repaint();

			}
		});
		perspectiveMenu.add(temp);
		temp = new JMenuItem("Side");
		temp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				component.showSide();
				component.getParent().repaint();

			}
		});



		perspectiveMenu.add(temp);
		temp = new JMenuItem("Top");
		temp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				component.showTop();
				component.getParent().repaint();
			}
		});

		perspectiveMenu.add(temp);

		temp = new JMenuItem("Optimize for Roi");
		temp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				component.optimizeViewToRoi();
				component.getParent().repaint();
			}
		});
		perspectiveMenu.add(temp);

		temp = new JMenuItem("Start");
		temp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				MultiThreadScheduler.startVideoRecording("tempvid_"+getDateTime()+".avi");
				MultiThreadScheduler.startVideo();

			}
		});
		recording.add(temp);

		temp = new JMenuItem("stop");
		temp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				MultiThreadScheduler.stopVideo();

			}
		});
		recording.add(temp);


	}

	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public JTextField getMarking() {

		marking.setPreferredSize(new Dimension(100,20));
		marking.setVisible(false);
		return marking;
	}

	public void addDialog(final ExternalWindow ew)
	{
		addDialog(ew,false);
	}

	public void addDialog(final ExternalWindow ew,boolean show)
	{
		JMenuItem newAction = new JMenuItem(ew.name);
		newAction.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ew.setVisible(!ew.isVisible());
				if(ew.isVisible())
				{
					dialogs.add(ew);

					ew.addWindowListener(new WindowListener() {

						public void windowOpened(WindowEvent arg0) {
						}
						public void windowIconified(WindowEvent arg0) {}
						public void windowDeiconified(WindowEvent arg0) {}
						public void windowDeactivated(WindowEvent arg0) {}
						public void windowClosing(WindowEvent arg0) {if(!arg0.getWindow().isVisible()){							dialogs.remove(ew);}}
						public void windowClosed(WindowEvent arg0) {
						}
						public void windowActivated(WindowEvent arg0) {}
					});
				}else
				{
					dialogs.remove(ew);
				}
			}
		});
		if(show)
		{
			ew.setVisible(show);
			dialogs.add(ew);
		}
		editMenu.add(newAction);
	}

	public Rectangle getDrawingDimension()
	{
		return component.getBounds();
	}

	public void addDrawer(final Drawer d,boolean show)
	{
		final JMenuItem newAction = new JMenuItem(d.name);
		d.setCanvas(this.component);
		newAction.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				if(!drawers_applied.contains(d))
				{
					drawers_applied.add(d);
					d.added();
					newAction.setText(d.name+" *");
				}
				else
				{
					drawers_applied.remove(d);
					d.removed();
					newAction.setText(d.name);
				}
				
				MonitoringGui.this.repaint();
			}


		});
		if(show)
		{
			drawers_applied.add(d);
			d.added();
			newAction.setText(d.name+" *");
		}
		component.setDrawers_applied(drawers_applied);
		fileMenu.add(newAction);
	}

	@Override
	public synchronized void repaint() {
		try{
			//addAllIntracellularSubstances();
			super.repaint();
		}
		catch (Exception e) {

		}
	}
	private  boolean repaintdone = false;
	public boolean repaintdone()
	{
		if(repaintdone)
		{
			repaintdone = false;
			return true;
		}
		return false;
	}
	@Override
	public synchronized void paint(Graphics g)
	{
		loadNewDynamicDrawers();
		try{
			super.paint(g);
			for (ExternalWindow i : dialogs) {
				if(i.isVisible())
				{
					i.updateWindow();
					i.repaint();
				}
			}

		}
		catch(Exception e){

		}
		repaintdone = true;
	}

	private void loadNewDynamicDrawers() {
		try {

			for (Drawer d : LoadDrawers.findNewDrawers()) {
				addDrawer(d, true);
			} 
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public JPanel drawboard() {
		if (jxypane == null) {
			component = new Canvas3d();

			jxypane = new JPanel();

			jxypane.setBackground(Color.white);
			jxypane.setPreferredSize(new Dimension(800,800));
			jxypane.setVisible(true);
			jxypane.setLayout(new BorderLayout());
			jxypane.add(marking,BorderLayout.NORTH);
			jxypane.add(component,BorderLayout.CENTER);

			ViewMouseListener m = new ViewMouseListener((Canvas3d)component);
			jxypane.addMouseMotionListener(m);
			jxypane.addMouseListener(m);
			jxypane.addMouseWheelListener(m);
			this.addKeyListener(m);
		}
		return jxypane;
	}

	public static MonitoringGui getStandardGUI()
	{
		MonitoringGui t = new MonitoringGui();
		t.addDrawer(new XDir(),false);
		t.addDrawer(new YDir(),false);
		t.addDrawer(new ZDir(),false);


		t.addDrawer(new DrawLocalDiffusionNeighbours(),false);
		t.addDrawer(new DrawRemoteDiffusionNeighbours(),false);
		t.addDrawer(new OctreeDrawer(),false);
		t.addDrawer(new RemoteDiffusionNodeDrawer(),false);
		t.addDrawer(new NotRecivedDiffNode(),false);
		//	t.addDrawer(new OctreeSequenceDrawer(),false);

		//	t.addDrawer(new OutCastPositionDrawer(),true);
		//		t.addDrawer(new NotRecivedPMs(),false);
		//	t.addDrawer(new MarginDrawer(),false);
		//	t.addDrawer(new RemoteMarginDrawer(), false);

		t.addDrawer(new PartitionNumberDrawer(),false);
		
		//	t.addDrawer(new SpacenodeDrawerLines(), false);
		//	t.addDrawer(new SpacenodeCornerDrawer(), false);
		t.addDrawer(new ShowNeighbors(), true);
		//t.addDrawer(new SphereDrawer(), true);
		t.addDrawer(new CylinderDrawer(), false);
		//	t.addDrawer(new CellModuleDrawer(), false);
		//	t.addDrawer(new LocalBiologyModuleDrawer(), false);
		//	t.addDrawer(new RemoteOctreeDrawer(), false);
		t.addDrawer(new PartitionNeighbourDrawer(), false);
		t.addDrawer(new BoundariesDrawer(), true);
		t.addDrawer(new SphereSliceDrawer(), true);
		t.addDrawer(new SpacenodeDrawer(), true);
		t.addDrawer(new ForceDrawer(), false);
		t.addDrawer(new PhyiscalBondDrawer(), false);
		t.addDrawer(new RemoteMarginDrawer(), false);
		t.addDrawer(new DrawNodesToExchange(), false);
		t.addDrawer(new RemotePartitionDrawer(),false);
		t.addDrawer(new DrawSlotGrid(),false);
		t.addDrawer(new PartitionDrawer(),false);
		t.addDrawer(new PartitionDrawerDistribution(),false);
		t.addDrawer(new RemoteMarginDrawer(), false);
		t.addDrawer(new ElTokensDrawer(), true);
		
		//t.addDrawer(new sphere(),true);


		//	t.addDialog(new LockInfoWindow(),false);
		t.addDialog(new ShowObjectCount(),true);
		//	t.addDialog(new LockNodeInfoWindow(),false);
		t.addDialog(new ShowTimers(),true);
		t.addDialog(OutD.current,true);//false
		t.addDialog(WhatTheHellAreYouWaitingFor.current,true);
		return t;
	}
	private ArrayList<String> substances = new ArrayList<String>();
	public void addAllIntracellularSubstances()
	{
		for (String s : ECM.getInstance().getIntracelularSubstanceTemplates().keySet()) {
			if(!substances.contains(s))
			{
				substances.add(s);
				this.addDrawer(new InternalSubstanceDrawer(s), false);
			}
		}
	}


	public void arrangeOnScreen(int x, int y, int w, int h) {

		//OutD.println("cmd"+jxypane.getPreferredSize());
		//		this.jxypane.setBounds(0, 0, w, h);
//		//		this.component.setBounds(0, 0, w,h);
//		int i = x;
//		for (ExternalWindow k : dialogs) {
//			k.arrangeWindow(i+w, y);
//			i+=200;
//		}
//		component.setViewingSize(this.getWidth(), this.getHeight());
//		this.setVisible(true);

	}
	
	public void arrangeDialogsOnScreen()
	{
			int i = this.getX();
			for (ExternalWindow k : dialogs) {
				k.arrangeWindow(i+this.getWidth(), this.getY());
				i+=250;
			}
	}

	public void setViewingSize720p()
	{
		component.getParent().setPreferredSize(new Dimension( 1280, 720));
		component.setViewingSize( 1280, 720);
		this.setBounds(0,0,1280, 720+40);
	}
	
	public void setViewingSize1080p()
	{
		component.getParent().setPreferredSize(new Dimension(1920, 1080));
		component.setViewingSize( 1920, 1080);
		this.setBounds(0,0,1920, 1080+40);
	}
	
	public void setViewingSizePal()
	{
		component.getParent().setPreferredSize(new Dimension(640,480));
		component.setViewingSize( 640, 480);
		this.setBounds(0,0,640,480+40);
	}


}