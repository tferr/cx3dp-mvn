package ini.cx3d.gui;

import javax.swing.JFrame;

public abstract class ExternalWindow extends JFrame{
	public String name;
	public abstract void updateWindow();
	public void arrangeWindow(int x, int y)
	{
		this.setBounds(x, y, this.getWidth(), this.getHeight());
	}
}
