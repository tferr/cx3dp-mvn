package ini.cx3d.gui;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.simulation.ECM;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;




public class ViewMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	ECM ecm = ECM.getInstance();
	PhysicalNode physicalSpace;
	Canvas3d view;
	boolean aNodeIsSelected;
	int lastX = 0, lastY = 0;
	boolean rightMouseButtonPressed = false;
	boolean leftMouseButtonPressed = false;
	int x0, x1, x2, z0, z1, z2, y0;

	
	public ViewMouseListener(Canvas3d view){
		this.view = view;
	}
	
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount()>=2)
		{
			totalclicks = 0;
			view.setScale(Math.pow(2, totalclicks));
			lastX = 0;
			lastY = 0;
			view.getParent().repaint();
			view.getParent().getParent().repaint();	
		}
	}

	
	
	public void mousePressed(MouseEvent e) {
//		ShowConsoleOutput.println("ViewMouseListener.mousePressed()");
		if (e.getButton() == MouseEvent.BUTTON3) {
			lastX = e.getX();
			lastY = e.getY();
			rightMouseButtonPressed = true;
		}
		if (e.getButton() == MouseEvent.BUTTON1) {
			lastX = e.getX();
			lastY = e.getY();
			leftMouseButtonPressed = true;
		}
		
	} 

	public void mouseReleased(MouseEvent e) {
//		ShowConsoleOutput.println("ViewMouseListener.mouseReleased()");
		if (e.getButton() == MouseEvent.BUTTON3) {
			rightMouseButtonPressed = false;
		}
		if (e.getButton() == MouseEvent.BUTTON1) {
			leftMouseButtonPressed = false;
		}
		
		view.getParent().repaint();
		
	}

	
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void mouseDragged(MouseEvent e) {
		double xd = e.getX() - lastX;
		double yd = e.getY() - lastY;
		if (rightMouseButtonPressed ) {
			view.rotateAroundY(xd * 0.005);
			view.rotateAroundX(-yd * 0.005);
		}
		if (leftMouseButtonPressed) {
			view.centery -= yd;
			view.centerx -= xd;
		}
		lastX = e.getX();
		lastY = e.getY();
		view.getParent().repaint();
	}

	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
	}

   
    int totalclicks=0;
	public void mouseWheelMoved(MouseWheelEvent e) {
		int clicks =e.getWheelRotation();
		if (clicks < 0)
			for (int i = 0; i < Math.abs(clicks); i++)
			{
				totalclicks--;
				view.setScale(Math.pow(2, totalclicks));
		
			}	
		else {
			for (int i = 0; i < Math.abs(clicks); i++)
			{	
				totalclicks++;
				view.setScale(Math.pow(2, totalclicks));
			
			}
			
		}
		view.getParent().repaint();
	}

	public void keyPressed(KeyEvent e) {
		OutD.println(e.getKeyCode());
		switch (e.getKeyCode()) {
		case 107:
			view.rotateAroundZ(-0.05);
			break;
		case 109:
			view.rotateAroundZ(0.05);
			break;
		case 37:
			view.rotateAroundY(-0.05);
			break;
		case 39:
			view.rotateAroundY(0.05);
			break;
		case 38:
			view.rotateAroundX(0.05);
			break;
		case 40:
			view.rotateAroundX(-0.05);
			break;
		case 45:
		case 49:
			totalclicks--;
			view.setScale(Math.pow(2, totalclicks));
			view.getParent().repaint();
			break;
		case 61:
		case 50:
			totalclicks++;
			view.setScale(Math.pow(2, totalclicks));
			view.getParent().repaint();
			break;
		default:
			OutD.println(e.getKeyCode());
			break;
		}
		
	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	

}
