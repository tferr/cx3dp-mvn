package ini.cx3d.gui.simulation;

import javax.swing.*;
import java.awt.Color.*;
import ini.cx3d.gui.ExternalWindow;

public class GMGUI extends ExternalWindow{
	
	public JPanel createContentPane (){

        // We create a bottom JPanel to place everything on.
        JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);

        // Creation of a Panel to contain the title labels
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(null);
        titlePanel.setLocation(10, 0);
        titlePanel.setSize(250, 30);
        totalGUI.add(titlePanel);
	
     // Creation of a Panel to contain the score labels.
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(null);
        scorePanel.setLocation(10, 40);
        scorePanel.setSize(250, 30);
        totalGUI.add(scorePanel);
	
        totalGUI.setOpaque(true);
        return totalGUI;
	
	
	
}

	@Override
	public void updateWindow() {
		// TODO Auto-generated method stub
		
	}
}