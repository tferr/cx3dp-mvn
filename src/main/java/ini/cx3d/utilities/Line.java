package ini.cx3d.utilities;
import static ini.cx3d.utilities.Matrix.dot;
import static ini.cx3d.utilities.Matrix.norm;
import static ini.cx3d.utilities.Matrix.normalize;
import static ini.cx3d.utilities.Matrix.subtract;

public class Line
{
	private double [] b;
	private double [] a;
	public Line(double [] a, double [] b)
	{
		this.a = a;
		this.b = b;
	}
	
	private boolean isParall(double [] a, double b[])
	{
		a = normalize(a);
		b = normalize(b);
		if(Math.abs(dot(a,b)) == 1) return true;
		if(norm(a) ==0 || norm(b)==0) return true;
		return false;
	}
	
	
	public boolean contains(Line line)
	{	
		double[] vec1 = subtract(a, b);
		double[] vec2 = subtract(a,line.a);
		double[] vec3 = subtract(a,line.b);
		
		if(!isParall(vec1,vec2)) return false;
		if(!isParall(vec1, vec3)) return false;
		
		boolean contained = true;
		double[] m1 =  getMax(a,b);
		double[] m2 = getMin(a,b);
		contained &= isOnLine(m1,m2,line.a);
		contained &= isOnLine(m1,m2,line.b);
		return contained;
	}
	
	public boolean isOnLine(double [] a,double [] b, double[] corrds)
	{
		boolean temp = true;
		temp &= a[0] >= corrds[0] && corrds[0]  >= b[0];
		temp &= a[1] >= corrds[1] && corrds[1]  >= b[1];
		temp &= a[2] >= corrds[2] && corrds[2]  >= b[2];
		return temp;
	}
	
	protected double[] getMin(double[] a, double[] b) {
		double [] temp = new double[3]; 
		temp[0]  = Math.min(a[0], b[0]);
		temp[1]  = Math.min(a[1], b[1]);
		temp[2]  = Math.min(a[2], b[2]);
		return temp;
	}


	protected double[] getMax(double[] a, double[] b) {
		double [] temp = new double[3]; 
		temp[0]  = Math.max(a[0], b[0]);
		temp[1]  = Math.max(a[1], b[1]);
		temp[2]  = Math.max(a[2], b[2]);
		return temp;
	}
	
	public double [] getCenter()
	{
		return Matrix.scalarMult(0.5,Matrix.add(a, b));
	}

	public void setA(double [] a) {
		this.a = a;
	}

	public double [] getA() {
		return a;
	}

	public void setB(double [] b) {
		this.b = b;
	}

	public double [] getB() {
		return b;
	}

}

