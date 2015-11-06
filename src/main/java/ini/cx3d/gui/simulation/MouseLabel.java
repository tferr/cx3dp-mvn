package ini.cx3d.gui.simulation;

	import java.awt.EventQueue;
	import java.awt.BorderLayout;
	import javax.swing.JFrame;
	import javax.swing.JLabel;
	import javax.swing.JScrollPane;
	import javax.swing.JTextArea;
	import java.awt.Color;
	import javax.swing.BorderFactory;
	import javax.swing.border.Border;
	import java.awt.event.MouseListener;
	import java.awt.event.MouseEvent;

	public class MouseLabel {

	    JLabel mouseLabel;
	    JLabel mouseMoveLabel;
	    JTextArea mouseEvents;

	    public static void main(String[] args) {
	     
	         //Use the event dispatch thread for Swing components
	         EventQueue.invokeLater(new Runnable()
	         {
	             
	            @Override
	             public void run()
	             {
	                 
	                 new MouseLabel();         
	             }
	         });
	              
	    }
	    
	    public MouseLabel()
	    {
	        JFrame guiFrame = new JFrame();
	        
	        //make sure the program exits when the frame closes
	        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        guiFrame.setTitle("BorderLayout Example");
	        guiFrame.setSize(700,300);
	      
	        //This will center the JFrame in the middle of the screen
	        guiFrame.setLocationRelativeTo(null);
	        
	        //creating a border to highlight the label areas
	        Border outline = BorderFactory.createLineBorder(Color.black);
	        
	        mouseLabel = new JLabel("Interactive Label", JLabel.CENTER);
	        mouseLabel.setBorder(outline);

	        //code for the MouseListener goes here..
	        
	        
	        mouseLabel.addMouseListener(new MouseListener()
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
	        mouseMoveLabel.setBorder(outline);

	        //code for the MouseMotionListener goes here..

	        mouseEvents = new JTextArea("The Mouse events can be seen here:\n");
	        JScrollPane textScroll = new JScrollPane(mouseEvents);

	        //code for the MouseWheelListener goes here..

	        guiFrame.add(mouseMoveLabel, BorderLayout.WEST);
	        guiFrame.add(mouseLabel, BorderLayout.CENTER);
	        guiFrame.add(textScroll, BorderLayout.EAST);
	        guiFrame.setVisible(true);
	    }
}