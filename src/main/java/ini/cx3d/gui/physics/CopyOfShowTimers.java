package ini.cx3d.gui.physics;

import ini.cx3d.gui.ExternalWindow;
import ini.cx3d.gui.simulation.GM;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.Timer;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;




public class CopyOfShowTimers extends ExternalWindow {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JButton loginButton;

	DefaultListModel  shown = new DefaultListModel(); 
	public CopyOfShowTimers() {
		initialize();
		this.name = "Show timers";
	}


	private void initialize() {

		this.setTitle(name);
		this.setSize(310, 400);

	}
	//	JList list = new JList(shown);

	//	JScrollPane scrollPane = new JScrollPane(list);

		 public JPanel createContentPane (){

		        // We create a bottom JPanel to place everything on.
		        JPanel totalGUI = new JPanel();
		        totalGUI.setLayout(null);
		
		JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CopyOfShowTimers.this.setVisible(true);
			}
		});
		
		 // Button for Logging in
        loginButton = new JButton("Find");
        loginButton.setLocation(80, 120);
        loginButton.setSize(150, 30);
        loginButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		
        	}
        });
        
        
        totalGUI.setOpaque(true);    
        return totalGUI;
    }
		
		
//		this.setLayout(new BorderLayout(5, 5));
//		getContentPane().add(scrollPane, BorderLayout.CENTER);
//		getContentPane().add(button, BorderLayout.SOUTH);
	
		 
		 private static void createAndShowGUI() {


		       JFrame frame = new JFrame("CopyOfShowTimers");

		        GM demo = new GM();
		        frame.setContentPane(demo.createContentPane());
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        frame.setSize(310, 400);
		        frame.setVisible(true);
		        //frame.dispose();
		    }
		    
		 
		 
		 
		 
		 

	@Override
	public  void updateWindow() {
		shown.clear();
		shown.addElement(String.format("ECM Time: %4.2f%n", ECM.getInstance().getECMtime()));
		shown.addElement("one round time in milis: "+Timer.getLastTotal());
		shown.addElement("----");
		shown.addElement(loginButton);
	//	for (String t: Timer.getTimerStringInfo()) {
	//		shown.addElement(t);
	//	}
	}

	public void paint(Graphics g)
	{
		try{
			super.paint(g);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			// TODO: handle exception
		}
		catch (Exception e) {

		}
	}





}
