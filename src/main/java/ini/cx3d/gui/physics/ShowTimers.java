package ini.cx3d.gui.physics;

import ini.cx3d.gui.ExternalWindow;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.Timer;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class ShowTimers extends ExternalWindow {

	DefaultListModel  shown = new DefaultListModel(); 
	public ShowTimers() {
		initialize();
		this.name = "Show timers";
	}


	private void initialize() {

		this.setTitle(name);
		this.setSize(300, 500);

		JList list = new JList(shown);

		JScrollPane scrollPane = new JScrollPane(list);

		JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ShowTimers.this.setVisible(true);
			}
		});
		this.setLayout(new BorderLayout(5, 5));
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(button, BorderLayout.SOUTH);
	}

	@Override
	public  void updateWindow() {
		shown.clear();
		shown.addElement(String.format("ECM Time: %4.2f%n", ECM.getInstance().getECMtime()));
		shown.addElement("one round time in milis: "+Timer.getLastTotal());
		shown.addElement("----");
		for (String t: Timer.getTimerStringInfo()) {
			shown.addElement(t);
		}
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
