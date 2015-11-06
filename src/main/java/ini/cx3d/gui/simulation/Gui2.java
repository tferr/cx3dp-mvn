package ini.cx3d.gui.simulation;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

public class Gui2 extends JFrame {
 JFrame frame = new JFrame();
 MyDrawPanel drawpanel = new MyDrawPanel();

 public static void main(String[] args) {
  Gui2 gui = new Gui2();
  gui.go();
 }

 public void go() {

  frame.getContentPane().add(drawpanel);
  // frame.addMouseListener(this);

  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  frame.setSize(300, 300);
  frame.setVisible(true);

 }

}

class MyDrawPanel extends JComponent implements MouseListener {

 public void paintComponent(Graphics g) {

  int red = (int) (Math.random() * 255);
  int green = (int) (Math.random() * 255);
  int blue = (int) (Math.random() * 255);
  Color startrandomColor = new Color(red, green, blue);

  red = (int) (Math.random() * 255);
  green = (int) (Math.random() * 255);
  blue = (int) (Math.random() * 255);
  Color endrandomColor = new Color(red, green, blue);

  Graphics2D g2d = (Graphics2D) g;
  this.addMouseListener(this);
  GradientPaint gradient = new GradientPaint(70, 70, startrandomColor,
    150, 150, endrandomColor);

  g2d.setPaint(gradient);
  g2d.fillOval(70, 70, 100, 100);

 }

 @Override
 public void mouseClicked(MouseEvent e) {
  if ((e.getButton() == 1)
    && (e.getX() >= 70 && e.getX() <= 170 && e.getY() >= 70 && e
      .getY() <= 170)) {
   this.repaint();
   // JOptionPane.showMessageDialog(null,e.getX()+ "\n" + e.getY());
  }

 }

 @Override
 public void mouseEntered(MouseEvent e) {
  // TODO Auto-generated method stub

 }

 @Override
 public void mouseExited(MouseEvent e) {
  // TODO Auto-generated method stub

 }

 @Override
 public void mousePressed(MouseEvent e) {
  // TODO Auto-generated method stub

 }

 @Override
 public void mouseReleased(MouseEvent e) {
  // TODO Auto-generated method stub

 }

}