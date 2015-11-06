package ini.cx3d.gui.spacialOrganisation;

import ini.cx3d.gui.ExternalWindow;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;
import ini.cx3d.utilities.HashT;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class WhatTheHellAreYouWaitingFor extends ExternalWindow {

		public static WhatTheHellAreYouWaitingFor current;
		
		public HashT<String, String> waitings = new HashT<String, String>();
	
		static {
			current = new WhatTheHellAreYouWaitingFor();
		}
		
		DefaultListModel  shown = new DefaultListModel();
		
	    WhatTheHellAreYouWaitingFor() {
	    	
	        initialize();
	        this.name = "Waiting for What?";
	    }
	    
	    
	  
	    public static void waitfor(String key,String s)
	    {
	    	if(!current.waitings.containsKey(key))
	    	{
	    		current.waitings.put(key, s);
	    	}
	    }
	    
	    public static void notAnymore(String key)
	    {
	    	
	    	current.waitings.remove(key);
	    	
	    }
	    
	    
	    private void initialize() {
	       
	        this.setTitle("show object count");
	        this.setSize(300, 500);
	     
	        JList list = new JList(shown);
	  
	        JScrollPane scrollPane = new JScrollPane(list);

	        JButton button = new JButton("Close");
	        button.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	               WhatTheHellAreYouWaitingFor.this.setVisible(true);
	               
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
				for(String s:waitings.keySet())
				{
					shown.addElement(waitings.get(s));
				}
			}
			catch (Exception e) {
				OutD.println("");
			}
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
		
	    
}
