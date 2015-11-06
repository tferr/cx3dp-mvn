package ini.cx3d.gui.simulation;

	
	

import javax.swing.*;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


	public class TextFieldExample implements ActionListener{
		
	    JPanel textPanel, panelForTextFields, completionPanel;
	    JLabel titleLabel, usernameLabel, passwordLabel, userLabel, passLabel;
	    JTextField usernameField, loginField;
	    JButton loginButton, clearButton, pauseButton, closeButton;

	    public JPanel createContentPane (){

	        // We create a bottom JPanel to place everything on.
	        JPanel totalGUI = new JPanel();
	        totalGUI.setLayout(null);

	        titleLabel = new JLabel("caca");//objeto con este nombre
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

	        // Login Label
	        passwordLabel = new JLabel("Password");
	        passwordLabel.setLocation(0, 40);
	        passwordLabel.setSize(70, 40);
	        passwordLabel.setHorizontalAlignment(4);
	        textPanel.add(passwordLabel);

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
	        panelForTextFields.add(usernameField);

	        // Login Textfield
	        loginField = new JTextField(8);
	        loginField.setLocation(0, 40);
	        loginField.setSize(100, 30);
	        panelForTextFields.add(loginField);

	        // Creation of a Panel to contain the completion JLabels
	        completionPanel = new JPanel();
	        completionPanel.setLayout(null);
	        completionPanel.setLocation(240, 35);
	        completionPanel.setSize(70, 80);
	        totalGUI.add(completionPanel);

	        // Username Label
	        userLabel = new JLabel("Wrong");
	        userLabel.setForeground(Color.red);
	        userLabel.setLocation(0, 0);
	        userLabel.setSize(70, 40);
	        completionPanel.add(userLabel);

	        // Login Label
	        passLabel = new JLabel("Wrong");
	        passLabel.setForeground(Color.red);
	        passLabel.setLocation(0, 40);
	        passLabel.setSize(70, 40);
	        completionPanel.add(passLabel);

	        // Button for Logging in
	        loginButton = new JButton("Find Daughters");
	        loginButton.setLocation(80, 120);
	        loginButton.setSize(150, 30);
	        loginButton.addActionListener(this);
	        totalGUI.add(loginButton);
	        
	     // Button for Clear
	        clearButton = new JButton("Clear");
	        clearButton.setLocation(120, 150);//80, 120
	        clearButton.setSize(80, 30);
	        clearButton.addActionListener(this);
	        totalGUI.add(clearButton);
	        
		 // Button for Pause
	        pauseButton = new JButton("Pause");
	        pauseButton.setLocation(120, 180);//80, 120
	        pauseButton.setSize(80, 30);
	        pauseButton.addActionListener(this);
	        totalGUI.add(pauseButton);
	        
	     // Button for Close
	        closeButton = new JButton("Close");
	        closeButton.setLocation(120, 210);
	        closeButton.setSize(80, 30);
	        
	        closeButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {    
                	
	            }
                
	        });
	        
	        totalGUI.add(closeButton);
	        
	                          
	        
	        
	        totalGUI.setOpaque(true);    
	        return totalGUI;
	    }

	    // With this action performed, we simply check to see if the username and 
	    // password match "Bob" as the username and "Robert" as the password.
	    // If they do, we set the labels ajacent to them to "Correct!" and color
	    // them green.
	    // At the end, we check if both labels are green. If they are, we set the
	    // screen to be 'Logging In'.

	    
		

		public void actionPerformed(ActionEvent e) {

	        if(e.getSource() == loginButton)
	        {
	            if(usernameField.getText().trim().compareTo("Bob") == 0)
	            {
	                userLabel.setForeground(Color.green);
	                userLabel.setText("Correct!");
	            }
	            else
	            {
	                userLabel.setForeground(Color.red);
	                userLabel.setText("Wrong!");
	            }

	            if(loginField.getText().trim().compareTo("Robert") == 0)
	            {
	                passLabel.setForeground(Color.green);
	                passLabel.setText("Correct!");
	            }
	            else
	            {
	                passLabel.setForeground(Color.red);
	                passLabel.setText("Wrong!");
	            }

	            if((userLabel.getForeground() == Color.green) 
				&& (passLabel.getForeground() == Color.green))
	            {
	                titleLabel.setText("Finding...");
	                loginButton.setEnabled(false);
	            }
	        }
	        
	        if(e.getSource() == closeButton)
	        {
	        	TextFieldExample.this.setVisible(false);
	        }
	        
	    }
		
		
	

		
		
		


	    private void setVisible(boolean b) {
			
			
		}

		private static void createAndShowGUI() {

	        JFrame.setDefaultLookAndFeelDecorated(true);
	        JFrame frame = new JFrame("Daughter tracker");

	        TextFieldExample demo = new TextFieldExample();
	        frame.setContentPane(demo.createContentPane());
	        
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setSize(310, 400);
	        frame.setVisible(true);
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


