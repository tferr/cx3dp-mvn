package ini.cx3d.gui;


import ini.cx3d.utilities.Cuboid;
import ini.cx3d.utilities.Matrix;
import ini.cx3d.utilities.Rectangle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.io.Serializable;
import java.util.ArrayList;

public abstract class Drawer implements Serializable{
	public String name;
	protected Canvas3d c;
	private transient Graphics g;

	public abstract void draw(Graphics g);

	public void added()
	{

	}

	public void removed()
	{}

	public void setCanvas(Canvas3d c) {
		this.c = c;
	}


	public synchronized void paint(Graphics g)
	{
		this.g = g;
		draw(g);
	}
	
	protected void setColor(Color c) {
		g.setColor(c);
	}

	
	
	
	protected void drawLine(Vector3d start, Vector3d end)
	{

		start = start.applyTransform(c.getWorldMatrix());
		end  = end.applyTransform(c.getWorldMatrix());
		int x1 = 0,x2=0,y1=0,y2=0;
		x1 = (int)(start.getX()*c.getScale());
		y1 = (int)(start.getY()*c.getScale());
		x2 = (int)(end.getX()*c.getScale());
		y2 = (int)(end.getY()*c.getScale());
		
		x1 = x1+c.width/2-c.getCenterx();
		x2 = x2+c.width/2-c.getCenterx();
		y1 =  -(y1-c.height/2)-c.getCentery();
		y2 = -(y2-c.height/2)-c.getCentery();
		
		g.drawLine(x1,y1,x2 ,y2 );
	}

	protected void drawLine(Vector3d start, Vector3d end,double thickness)
	{
		thickness *=c.getScale();
		((Graphics2D) g).setStroke( new BasicStroke((float)(thickness)));
		drawLine(start, end);
//		start = start.applyTransform(c.getWorldMatrix());
//		end  = end.applyTransform(c.getWorldMatrix());
//		int x1 = 0,x2=0,y1=0,y2=0;
//		x1 = (int)(start.getX()*c.getScale());
//		y1 = (int)(start.getY()*c.getScale());
//		x2 = (int)(end.getX()*c.getScale());
//		y2 = (int)(end.getY()*c.getScale());
//
//		this.drawThickLine(g, (x1+c.width/2)-c.getCenterx(),
//				-(y1-c.height/2)-c.getCentery(),
//				x2+c.width/2-c.getCenterx(),
//				-(y2-c.height/2)-c.getCentery(),(int)thickness
//		);
	}

	
	
	
	protected void drawRect(Vector3d start, Vector3d end)
	{

		int x1 = 0,x2=0,y1=0,y2=0;
		start = start.applyTransform(c.getWorldMatrix());
		end  = end.applyTransform(c.getWorldMatrix());
		x1 = (int)(start.getX()*c.getScale());
		y1 = (int)(start.getY()*c.getScale());
		x2 = (int)(end.getX()*c.getScale());
		y2 = (int)(end.getY()*c.getScale());
		int x = Math.min(x1, x2);
		int y = Math.max(y1, y2);

		g.drawRect(x+c.width/2-c.getCenterx(), -(y-c.height/2)-c.getCentery(), Math.abs(x1-x2), Math.abs(y1-y2));
	}


	protected void fillRect(Vector3d start, Vector3d end)
	{

		int x1 = 0,x2=0,y1=0,y2=0;
		start = start.applyTransform(c.getWorldMatrix());
		end  = end.applyTransform(c.getWorldMatrix());
		x1 = (int)(start.getX()*c.getScale());
		y1 = (int)(start.getY()*c.getScale());
		x2 = (int)(end.getX()*c.getScale());
		y2 = (int)(end.getY()*c.getScale());
		int x = Math.min(x1, x2);
		int y = Math.max(y1, y2);

		g.fillRect(x+c.width/2-c.getCenterx(), -(y-c.height/2)-c.getCentery(), Math.abs(x1-x2), Math.abs(y1-y2));
	}

	protected void drawOval(Vector3d start, Vector3d end)
	{

		int x1 = 0,x2=0,y1=0,y2=0;
		start = start.applyTransform(c.getWorldMatrix());
		end  = end.applyTransform(c.getWorldMatrix());
		x1 = (int)(start.getX()*c.getScale());
		y1 = (int)(start.getY()*c.getScale());
		x2 = (int)(end.getX()*c.getScale());
		y2 = (int)(end.getY()*c.getScale());
		int x = Math.min(x1, x2);
		int y = Math.max(y1, y2);

		g.drawOval(x+c.width/2-c.getCenterx(), -(y-c.height/2)-c.getCentery(), Math.abs(x1-x2), Math.abs(y1-y2));
	}

	protected void fillOval(Vector3d start, Vector3d end)
	{

		int x1 = 0,x2=0,y1=0,y2=0;
		start = start.applyTransform(c.getWorldMatrix());
		end  = end.applyTransform(c.getWorldMatrix());
		x1 = (int)(start.getX()*c.getScale());
		y1 = (int)(start.getY()*c.getScale());
		x2 = (int)(end.getX()*c.getScale());
		y2 = (int)(end.getY()*c.getScale());
		int x = Math.min(x1, x2);
		int y = Math.max(y1, y2);

		g.fillOval(x+c.width/2-c.getCenterx(), -(y-c.height/2)-c.getCentery(), Math.abs(x1-x2), Math.abs(y1-y2));
	}
	
	protected void drawCyrcle(Vector3d start,double radius)
	{
		int x1 = 0,x2=0,y1=0,y2=0;
		radius = Math.max(radius*c.getScale(),2);
		start = start.applyTransform(c.getWorldMatrix());
		x1 = (int)(start.getX()*c.getScale()-radius);
		y1 = (int)(start.getY()*c.getScale()-radius);
		x2 = (int)(start.getX()*c.getScale()+radius);
		y2 = (int)(start.getY()*c.getScale()+radius);
		int x = Math.min(x1, x2);
		int y = Math.max(y1, y2);

		g.fillOval(x+c.width/2-c.getCenterx(), -(y-c.height/2)-c.getCentery(), Math.abs(x1-x2), Math.abs(y1-y2));
	}
	
	protected void drawCyrcleEmpty(Vector3d start,double radius)
	{
		int x1 = 0,x2=0,y1=0,y2=0;
		radius = Math.max(radius*c.getScale(),2);
		start = start.applyTransform(c.getWorldMatrix());
		x1 = (int)(start.getX()*c.getScale()-radius);
		y1 = (int)(start.getY()*c.getScale()-radius);
		x2 = (int)(start.getX()*c.getScale()+radius);
		y2 = (int)(start.getY()*c.getScale()+radius);
		int x = Math.min(x1, x2);
		int y = Math.max(y1, y2);

		g.drawOval(x+c.width/2-c.getCenterx(), -(y-c.height/2)-c.getCentery(), Math.abs(x1-x2), Math.abs(y1-y2));
	}
	
	protected void drawCyrcleUnsacaled(Vector3d start,double radius)
	{
		int x1 = 0,x2=0,y1=0,y2=0;
		start = start.applyTransform(c.getWorldMatrix());
		x1 = (int)(start.getX()*c.getScale()-radius/2);
		y1 = (int)(start.getY()*c.getScale()-radius/2);
		x2 = (int)(start.getX()*c.getScale()+radius/2);
		y2 = (int)(start.getY()*c.getScale()+radius/2);
		int x = Math.min(x1, x2);
		int y = Math.max(y1, y2);

		g.fillOval(x+c.width/2-c.getCenterx(), -(y-c.height/2)-c.getCentery(), Math.abs(x1-x2), Math.abs(y1-y2));
	}
	
	protected void drawCyrcle(Vector3d start,double radius,int offsetx)
	{
		int x1 = 0,x2=0,y1=0,y2=0;
		radius = Math.max(radius*c.getScale(),2);
		start = start.applyTransform(c.getWorldMatrix());
		x1 = (int)(start.getX()*c.getScale()-radius/2);
		y1 = (int)(start.getY()*c.getScale()-radius/2);
		x2 = (int)(start.getX()*c.getScale()+radius/2);
		y2 = (int)(start.getY()*c.getScale()+radius/2);
		int x = Math.min(x1, x2);
		int y = Math.max(y1, y2);

		g.fillOval(x+c.width/2-c.getCenterx()+offsetx, -(y-c.height/2)-c.getCentery(), Math.abs(x1-x2), Math.abs(y1-y2));
	}

	protected void drawString(Vector3d start,String str)
	{
		drawString(start, str,0);
	}
	
	protected void drawString(Vector3d start,String str,int offset)
	{
		start = start.applyTransform(c.getWorldMatrix());
		int x = (int)(start.getX()*c.getScale());
		int y = (int)(start.getY()*c.getScale());

		g.drawString(str, x+c.width/2-c.getCenterx(), -(y-c.height/2)-c.getCentery()+offset);
	}

	private void drawThickLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
		// The thick line is in fact a filled polygon
		int dX = x2 - x1;
		int dY = y2 - y1;
		// line length
		double lineLength = Math.sqrt(dX * dX + dY * dY);

		double scale = (double)(thickness) / (2 * lineLength);

		// The x,y increments from an endpoint needed to create a rectangle...
		double ddx = -scale * (double)dY;
		double ddy = scale * (double)dX;
		ddx += (ddx > 0) ? 0.5 : -0.5;
		ddy += (ddy > 0) ? 0.5 : -0.5;
		int dx = (int)ddx;
		int dy = (int)ddy;

		// Now we can compute the corner points...
		int xPoints[] = new int[4];
		int yPoints[] = new int[4];

		xPoints[0] = x1 + dx; yPoints[0] = y1 + dy;
		xPoints[1] = x1 - dx; yPoints[1] = y1 - dy;
		xPoints[2] = x2 - dx; yPoints[2] = y2 - dy;
		xPoints[3] = x2 + dx; yPoints[3] = y2 + dy;

		g.fillPolygon(xPoints, yPoints, 4);
	}

	protected void drawArrow(Graphics g,Vector3d pos, Vector3d dir) {

		//if(Matrix.norm(dir.toArray())<0.01) return; 
		int x = 0,xdir=0,y=0,ydir=0;
		pos = pos.applyTransform(c.getWorldMatrix());
		dir  = dir.applyTransform(c.getWorldMatrix());
		x = (int)(pos.getX()*c.getScale());
		y = (int)(pos.getY()*c.getScale());
	
		
		
		xdir = (int)(dir.getX()*c.getScale());
		ydir = (int)(dir.getY()*c.getScale());
		double [] normdir = Matrix.normalize(dir.toArray());
		double [] perp = Matrix.crossProduct(normdir,new double[]{0,0,1});
		int perpx= (int) (perp[0]*10);
		int perpy = (int) (perp[1]*10);
		int normdirx = (int) (normdir[0]*10*Math.signum(Matrix.norm(dir.toArray())));
		int normdiry = (int) (normdir[1]*10*Math.signum(Matrix.norm(dir.toArray())));
	
		
		Graphics2D g2d = (Graphics2D)g;
		double aDir=Math.atan2(xdir,ydir);
		g2d.setColor(Color.black);
		int x2 = xdir+x; 
		int y2 = ydir+y;
		x = x+c.width/2-c.getCenterx();
		y = -(y-c.height/2)-c.getCentery();
		x2= x2+c.width/2-c.getCenterx();
		y2=-(y2-c.height/2)-c.getCentery();
		g.drawLine(x,y ,x2 ,y2 );
		g2d.setStroke(new BasicStroke(1f));					// make the arrow head solid even if dash pattern has been specified
		Polygon tmpPoly=new Polygon();
		int stroke = 2;
		int i1=12+(int)(stroke*2);
		int i2=6+(int)stroke;							// make the arrow head the same size regardless of the length length
		tmpPoly.addPoint(x,y);							// arrow tip
		tmpPoly.addPoint(x+perpx-normdirx,y+perpy-normdiry);
//		tmpPoly.addPoint(x2+xCor(i2,aDir),y2+yCor(i2,aDir));
		tmpPoly.addPoint(x-perpx-normdirx,y-perpy-normdiry);
		tmpPoly.addPoint(x,y);							// arrow tip
		g2d.drawPolygon(tmpPoly);
		//g2d.fillPolygon(tmpPoly);						// remove this line to leave arrow head unpainted
	}
	
	protected void drawCuboid(Cuboid d2) {
		for (Rectangle r : d2.getSides()) {
			ArrayList<Vector3d> temp = r.edges();
			drawLine(temp.get(0), temp.get(1));
			drawLine(temp.get(0), temp.get(2));
			drawLine(temp.get(3), temp.get(1));
			drawLine(temp.get(3), temp.get(2));
		}	
	}

	private Color hold;
	
	protected void setColorHalfIntensity()
	{
		this.hold = this.g.getColor();
		Color c = new Color(hold.getRed(),hold.getGreen(),hold.getBlue(),hold.getAlpha()/2);
		this.setColor(c);
	}
	
	protected void setColorOriginalIntensity()
	{
		this.setColor(hold);
	}
	
	
	
}
