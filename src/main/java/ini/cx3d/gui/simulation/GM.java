package ini.cx3d.gui.simulation;

	
	
import javax.swing.*;


import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;




	public class GM implements ActionListener {
		
		
		
		
	    JPanel textPanel, panelForTextFields, completionPanel;
	    JLabel titleLabel, usernameLabel, passwordLabel, userLabel, passLabel;
	    JTextField usernameField, loginField;
	    JButton loginButton, clearButton, pauseButton, closingButton;
	    
	    
	    
	    public JPanel createContentPane (){

	        // We create a bottom JPanel to place everything on.
	        JPanel totalGUI = new JPanel();
	        totalGUI.setLayout(null);
	       

	        titleLabel = new JLabel("Daughter Tracker");
	        titleLabel.setLocation(0,0);
	        titleLabel.setSize(290, 30);
	        titleLabel.setHorizontalAlignment(0);
	        totalGUI.add(titleLabel);

	        // Creation of a Panel to contain the JLabels
	        textPanel = new JPanel();
	        textPanel.setLayout(null);
	        textPanel.setLocation(10, 35);
	        textPanel.setSize(70, 80);
	        totalGUI.add(textPanel);

	        // Username Label
	        usernameLabel = new JLabel("Input");
	        usernameLabel.setLocation(0, 0);
	        usernameLabel.setSize(70, 40);
	        usernameLabel.setHorizontalAlignment(4);
	        textPanel.add(usernameLabel);

	        // TextFields Panel Container
	        panelForTextFields = new JPanel();
	        panelForTextFields.setLayout(null);
	        panelForTextFields.setLocation(110, 40);
	        panelForTextFields.setSize(100, 70);
	        totalGUI.add(panelForTextFields);

	        // Username Textfield
	        usernameField = new JTextField(8);
	        usernameField.setLocation(0, 0);
	        usernameField.setSize(100, 30);
	        totalGUI.add(usernameField);


	        // Creation of a Panel to contain the completion JLabels
	        completionPanel = new JPanel();
	        completionPanel.setLayout(null);
	        completionPanel.setLocation(240, 35);
	        completionPanel.setSize(70, 80);
	        totalGUI.add(completionPanel);


	        // Button for Logging in
	        loginButton = new JButton("Find");
	        loginButton.setLocation(80, 120);
	        loginButton.setSize(150, 30);
	        loginButton.addActionListener(new ActionListener(){
	        	public void actionPerformed(ActionEvent e){
	        		
	        	}
	        });
	        
	        totalGUI.add(loginButton);
	        
	        
	        // Button for Clearing
	        clearButton = new JButton("Clear");
	        clearButton.setLocation(80, 150);
	        clearButton.setSize(150, 30);
	        clearButton.addActionListener(new ActionListener(){
	        	public void actionPerformed(ActionEvent e){
	        		usernameField.setText(null);
	        	}
	        });
	       
	        totalGUI.add(clearButton);
	        
	        
	        // Button for Pausing
	        pauseButton = new JButton("Pause");
	        pauseButton.setLocation(80, 180);
	        pauseButton.setSize(150, 30);
	        pauseButton.addActionListener(new ActionListener(){
	        	public void actionPerformed(ActionEvent e){
	        		ThreadHandler.togglePause();
	        	}
	        });
	        
	        totalGUI.add(pauseButton);
	        
	    //    JFrame frame = new JFrame("Daughter Finder");
	        
	        // Button for Closing
	        closingButton = new JButton("Close");
	        closingButton.setLocation(80, 210);
	        closingButton.setSize(150, 30);
	        closingButton.addActionListener(new ActionListener()
	        {
	        	public void actionPerformed(ActionEvent e)
	        	{
	     //   	setVisible(false);
	     //   	dispose();
	        	}
	        });
	        
	        totalGUI.add(closingButton);
	        
	       
	        
	        
	        totalGUI.setOpaque(true);    
	        return totalGUI;
	    }
	    
	    public void actionPerformed(ActionEvent e) {
            
            
            if(e.getSource() == closingButton)
            {
     //       frame.setVisible(false);
     //       frame.dispose();
            }
        }
	    
	    
	   
	    private static void createAndShowGUI() {


	       JFrame frame = new JFrame("Daughter Finder");

	        GM demo = new GM();
	        frame.setContentPane(demo.createContentPane());
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setSize(310, 400);
	        frame.setVisible(true);
	        //frame.dispose();
	    }
	    
	    
	    
	   

		
	        
		
		
		

		
		
		
		
	    public static void main(String[] args) {
	        //Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                createAndShowGUI();
	            }
	        });
	    }

		
	    
	    
	    
		
	}


