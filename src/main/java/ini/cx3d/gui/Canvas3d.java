package ini.cx3d.gui;
import static ini.cx3d.utilities.Matrix.mult;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.Cuboid;
import ini.cx3d.utilities.Matrix;

import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;




public class Canvas3d extends Component implements Serializable{
	
	public static double basescale = 2;
	public int width  = 800;
	public int centerx;
	public int height = 800;
	public int centery;
	private ArrayList<Drawer> drawers_applied;
	protected double scale = basescale;
	
	private double [][] worldMatrix = new double [][]{new double[]{1,0,0}, 
													  new double[]{0,1,0},
													  new double[]{0,0,1}
													};

	private double [][] VrotZ = new double [][]{new double[]{1,0,0}, 
			  new double[]{0,1,0},
			  new double[]{0,0,1}
			};
	private double [][] VrotY = new double [][]{new double[]{1,0,0}, 
			  new double[]{0,1,0},
			  new double[]{0,0,1}
			};
	private double [][] VrotX = new double [][]{new double[]{1,0,0}, 
			  new double[]{0,1,0},
			  new double[]{0,0,1}
			};
	private double alphaAroundZ;
	private double alphaAroundX;
	private double alphaAroundY;
	
	
	public void resetView()
	{
		VrotZ = new double [][]{new double[]{1,0,0}, 
				  new double[]{0,1,0},
				  new double[]{0,0,1}
				};
		VrotX = new double [][]{new double[]{1,0,0}, 
				  new double[]{0,1,0},
				  new double[]{0,0,1}
				};
		VrotY = new double [][]{new double[]{1,0,0}, 
				  new double[]{0,1,0},
				  new double[]{0,0,1}
				};
		worldMatrix = new double [][]{new double[]{1,0,0}, 
				  new double[]{0,1,0},
				  new double[]{0,0,1}
				};
		alphaAroundZ=0;
		alphaAroundY=0;
		alphaAroundX=0;
		centerx=0;
		centery =0;
		scale = basescale;
	}
	
	public void showTop()
	{
		resetView();
		double scale = this.scale;
		setRotationAroundX(Math.PI/2);
		this.scale = scale;

	}
	
	public void showSide()
	{
		resetView();
		double scale = this.scale;
		setRotationAroundY(Math.PI/2);
		
		this.scale = scale;
	}

	
	
	public Canvas3d()
	{

	}
	
	@Override
	public void paint(Graphics g)
	{
		try{
			synchronized (g) {
				super.paint(g);
				for (Drawer d : drawers_applied) {
					d.paint(g);
				}
			}
		}catch(Exception e)
			{
			OutD.println("failed!!!! paint:"+e.getMessage());
			e.printStackTrace();
		}
	}


	public void setCenterx(int centerx) {
		this.centerx = centerx;
	}

	public int getCenterx() {
		return centerx;
	}

	public void setCentery(int centery) {
		this.centery = centery;
	}

	public int getCentery() {
		return centery;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public double getScale() {
		return scale;
	}


	
	public void setRotationAroundY(double a){
		alphaAroundY = a;
		// ShowConsoleOutput.println("alpa y: "+alphaAroundZ/Math.PI*180);
		VrotZ[0][0] = Math.cos(a);		VrotZ[0][1] = 0;		VrotZ[0][2] = Math.sin(a);
		VrotZ[1][0] = 0;				VrotZ[1][1] = 1;		VrotZ[1][2] = 0;
		VrotZ[2][0] = -VrotZ[0][2];		VrotZ[2][1] = 0;		VrotZ[2][2] = VrotZ[0][0];
		setWorldMatrix(mult(mult(VrotX,VrotY),VrotZ));
	}
	
	public void setRotationAroundZ(double a){
		alphaAroundZ = a;
		// ShowConsoleOutput.println("alpa z: "+alphaAroundY/Math.PI*180);
		VrotY[0][0] = Math.cos(a);		VrotY[0][1] = -Math.sin(a); VrotY[0][2] = 0; 
		VrotY[1][0] = Math.sin(a);   	VrotY[1][1] = Math.cos(a);  VrotY[1][2] = 0;
		VrotY[2][0] = 0;		        VrotY[2][1] = 0;		    VrotY[2][2] = 1;
		setWorldMatrix(mult(mult(VrotX,VrotY),VrotZ));

	}

	public void setRotationAroundX(double a){
	    alphaAroundX = a;
	   // ShowConsoleOutput.println("alpa x: "+alphaAroundX/Math.PI*180);
		VrotX[0][0] = 1;		VrotX[0][1] = 0;							VrotX[0][2] = 0;
		VrotX[1][0] = 0;		VrotX[1][1] = Math.cos(alphaAroundX);		VrotX[1][2] = Math.sin(alphaAroundX);
		VrotX[2][0] = 0;		VrotX[2][1] = -Math.sin(alphaAroundX);		VrotX[2][2] = Math.cos(alphaAroundX);;
		setWorldMatrix(mult(mult(VrotX,VrotY),VrotZ));

	}
	
	public void rotateAroundZ(double a)
	{	
		
		setRotationAroundZ(alphaAroundZ+a);
	}
	
	public void rotateAroundY(double a)
	{	
		
		setRotationAroundY(alphaAroundY+a);
	}
	
	public void rotateAroundX(double a)
	{
		setRotationAroundX(alphaAroundX+a);
	}

	public void setWorldMatrix(double [][] worldMatrix) {
		this.worldMatrix = worldMatrix;
	}

	public double [][] getWorldMatrix() {
		return worldMatrix;
	}
	
	
	private Cuboid roi;
	public void setRoi(Cuboid roi)
	{
		this.roi = roi;
	}

	public void setViewingSize(int width, int height)
	{
		this.width = width;
		this.height = height;

	}
	
	
	public void optimizeViewToRoi()
	{
		
		double [] max = getRoi().getUpperLeftCornerFront();
		double [] min = getRoi().getLowerRightCornerBack();
		double [] diff = Matrix.subtract(max, min);
		
		double [] unscaledindraw = toDrawSpaceUnscaled(diff);
		
		double xscale = (width*1)/(unscaledindraw[0]+1);
		double yscale = (height*1)/(unscaledindraw[1]+1);
		this.scale = Math.min(xscale, yscale);
		double [] center =  roi.getCenter();
		center = toDrawSpaceUnscaled(center);
		this.centerx = ((int)(center[0]*scale));
		this.centery = -((int)(center[1]*scale));
		
		
	}
	
	private double [] toDrawSpaceUnscaled(double [] p)
	{
		Vector3d start = new Vector3d(p); 
		start= start.applyTransform(this.getWorldMatrix());
		return new double[] {start.getX() , start.getY()};
	}

	public Cuboid getRoi() {
		if(roi ==null)
		{
			roi = new Cuboid(ECM.getInstance().getArtificialWalllMin(),ECM.getInstance().getArtificialWalllMax()); 
		}
		return roi;
	}

	public void setDrawers_applied(ArrayList<Drawer> drawers_applied) {
		this.drawers_applied = drawers_applied;
	}

	public ArrayList<Drawer> getDrawers_applied() {
		return drawers_applied;
	}

}
