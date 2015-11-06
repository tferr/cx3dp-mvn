package ini.cx3d.biology;

import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.simulation.ECM;
import ini.cx3d.spacialOrganisation.SpaceNodeFacade;
import ini.cx3d.utilities.Matrix;

import java.util.ArrayList;

/**
 * <code>CellFacory</code> generates a new  <code>Cell</code>, <code>SomaElement</code>, 
 * <code>PhysicalSphere</code> and <code>SpatialOrganizationNode</code>.  We set than the
 * massLocation and cell color.
 * We can generate a single <code>Cell</code> or a list of <code>Cell</code> distributed
 * uniformly.
 * @author rjd/sabina
 *
 */
public class CellFactory {
    

	/**
	 * <code>CellFactory</code> constructor.
	 */
    public CellFactory() { 
    }
    
    /**
     * Generates a single cell at the specified position.
     * @param cellOrigin
     * @return
     */
    public static Cell getCellInstance(double[] cellOrigin) {
    	
    	return getCellInstance(null, cellOrigin);
    }
    
   /**
    * Generates a 2D grid of cells according according to the desired number of cells along 
    * the y and x axes. Cell position can be randomized by increasing the standard deviation of
    * the Gaussian noise distribution. 
    * @param xmin 
    * @param xmax
    * @param ymin
    * @param ymax
    * @param nx: Number of cells along the x axis
    * @param ny: Number of cells along the y axis
    * @param noiseStd: Gaussian noise standard deviation
    * @return cellList
    */
   public static ArrayList<Cell> get2DCellGrid(double xmin, double xmax, double zmin,
		   double zmax, double ypos, int nx, int nz, double noiseStd) {
	   
	   // Insert all generated cells in a vector
	   ArrayList<Cell> cellList = new ArrayList<Cell>();
       double dx = (xmax-xmin)/(1+nx);
       double dz = (zmax-zmin)/(1+nz);
       
       // Generate cells
       for (int i=1; i < nx+1; i++) {
       	for (int j=1; j < nz+1; j++) {
       		double[] newLocation = {
       				xmin+i*dx+ECM.getInstance().getGaussianDouble(0, noiseStd),
       				ypos,
       				zmin+j*dz+ECM.getInstance().getGaussianDouble(0, noiseStd)};
       		Matrix.print(newLocation);
       		Cell cell = getCellInstance(newLocation);
       		cellList.add(cell);
       	}
       }
       return cellList;
   }
   
   /**
    * Generates a 3D grid of cells according to the desired number of cells along 
    * the y, x and z axes. Cell position can be randomized by increasing the standard deviation of
    * the Gaussian noise distribution. 
    * @param xmin 
    * @param xmax
    * @param ymin
    * @param ymax
    * @param zmin
    * @param zmax
    * @param nx: Number of cells along the x axis
    * @param ny: Number of cells along the y axis
    * @param nz: Number of cells along the z axis
    * @param noiseStd: Gaussian noise standard deviation
    * @return cellList
    */
   public static ArrayList<Cell> get3DCellGrid(double xmin, double xmax, double ymin,
		   double ymax, double zmin, double zmax, int nx, int ny, int nz, double noiseXYStd, double noiseZStd) {
	   
	   // Insert all generated cells in a vector
	   ArrayList<Cell> cellList = new ArrayList<Cell>();
       double dx = (xmax-xmin)/(1+nx);
       double dy = (ymax-ymin)/(1+ny);
       double dz = (zmax-zmin)/(1+nz);
       Cell oldcell =null;
       // Generate cells
       for (int i=1; i < nx+1; i++) {
       	for (int j=1; j < ny+1; j++) {
       		for (int k=1; k < nz+1; k++) {
       			double[] newLocation = {
       					xmin+i*dx+ECM.getInstance().getGaussianDouble(0, noiseXYStd), 
       					ymin+j*dy+ECM.getInstance().getGaussianDouble(0, noiseXYStd), 
       					zmin+k*dz+ECM.getInstance().getGaussianDouble(0, noiseZStd)};
       			Cell cell = getCellInstance(oldcell,newLocation);
       			oldcell = cell;
       			cellList.add(cell);
       		}
       	}
       }
       return cellList;
   }

private static Cell getCellInstance(Cell old, double[] cellOrigin) {
	// TODO Auto-generated method stub
	// Create new cell
    Cell cell = new Cell();
    SomaElement soma = new SomaElement();
           
    PhysicalSphere ps = new PhysicalSphere(); 
   
    SpaceNodeFacade son = null;
    if(old!=null)
    {
		son = old.getSomaElement().getPhysical().getSoNode().getNewInstance(cellOrigin, ps);
    }
    else
    {
    	son = ECM.getInstance().getSpatialOrganizationNodeInstance(cellOrigin.clone(), ps);
    }
    ps.setSoNode(son);
    soma.setPhysicalAndInstall(ps);
    cell.setSomaElement(soma);
   // ps.installLocally();
    // Add cell to ECM instance
    
    // Set cell properties
    ps.setMassLocation(cellOrigin);
    ps.setColor(ECM.getInstance().cellTypeColor(cell.getType()));
    return cell;
}


}