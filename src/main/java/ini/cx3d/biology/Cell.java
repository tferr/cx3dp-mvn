package ini.cx3d.biology;

import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.serialisation.CustomSerializable;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class <code>Cell</code> implements the cell at biological level. Every cell is characterized
 * by a unique cellId, cellType (cell state), <code>LyonCellCycle</code> (cell cycle) and are eventually
 * organized in a cell lineage tree (<code>CellLinNode</code>).
 * This class contains the genome (for now a list of <code>Gene</code>), a list of <code>GeneSubstance</code>
 * (seen as the product of the genes in the Gene vector), and is characterized by a cell type (defined by the
 * relative concentrations of the GeneSubstances.

 * @author sabina & RJD & fredericzubler
 *
 */
public class Cell  implements Serializable, CustomSerializable{

	/* Unique identification for this CellElement instance.*/
	int ID = 0;
	

			
	/* List of all cell modules that are run at each time step*/
	public ArrayList<CellModule> cellModules = new ArrayList<CellModule>();

	/* List of the SomaElements belonging to the cell */
	SomaElement somaElement = null;

	/* List of the first Neurite of all Nurites belonging to the cell */
	ArrayList<NeuriteElement> neuriteRootList = new ArrayList<NeuriteElement>(); // TODO: not working yet

	/** Represents inhibitory type of cell in the NeuroML export*/
	public static final String InhibitoryCell = "Inhibitory_cells";
	
	/** Represents excitatory type of cell in the NeuroML export*/
	public static final String ExcitatoryCell = "Excitatory_cells";
	
	/* The electrophsiology type of this cell */
	private String neuroMLType = ExcitatoryCell;
	
	/* Some convenient way to store properties of  for cells. 
	 * Should not be confused with neuroMLType. */
	
	/**
	 * Generate <code>Cell</code>. and registers the <code>Cell</code> to <code>ECM<</code>. 
	 * Every cell is identified by a unique cellID number.
	 */
	public Cell() {
		ID = ECM.getInstance().getCellidCounter().incrementAndGet();
	}

	/**
	 * Run Cell: run <code>Gene</code>, run <code>LyonCellCycle</code>, run Conditions, run EnergyProduction.
	 * We move one step further in the simulation by running the <code>Gene</code>, <code>GeneSubstances</code>,
	 * the <code>LyonCellCycle</code>, EnergyProduction and than we test conditions with ConditionTester. 
	 */
	public void run() {
		
		// Run all the CellModules
		// Important : the vector might be modified during the loop (for instance if a module deletes itself)
		for (int j = 0; j < cellModules.size(); j++) {
			CellModule module = cellModules.get(j);
			module.run();
		}
		
	}

	// *************************************************************************************
	// *      METHODS FOR DIVISION                                                         *
	// *************************************************************************************

	/**
	 * Divide the cell. Of the two daughter cells, one is this one (but smaller, with half GeneSubstances etc.),
	 * and the other one is instantiated de novo and is returned. Both cells have more or less the same volume, 
	 * the axis of division is random.
	 * @return the other daughter cell.
	 */
	public Cell divide() { 
		// find a volume ration close to 1;
		return divide(0.4 + 0.2*ECM.getInstance().getRandomDouble());
	}
	
	/**
	 * Divide the cell. Of the two daughter cells, one is this one (but smaller, with half GeneSubstances etc.),
	 * and the other one is instantiated de novo and is returned. The axis of division is random.
	 * @param volumeRatio the ratio (Volume daughter 1)/(Volume daughter 2). 1.0 gives equal cells.
	 * @return the second daughter cell.
	 */
	public Cell divide(double volumeRatio){
			// find random point on sphere (based on : http://www.cs.cmu.edu/~mws/rpos.html)
			double z = - 1 + 2*ECM.getInstance().getRandomDouble();
			double phi = Math.asin(z);
			double theta = 6.28318531*ECM.getInstance().getRandomDouble();
			PhysicalSphere sphere = somaElement.getPhysicalSphere();
			double [] randomdir  = sphere.transformCoordinatesPolarToLocal(new double[]{z,phi,theta});
			return divide(volumeRatio,randomdir);
	}
	
	public Cell divide(double[] axisOfDivision) {
		PhysicalSphere sphere = somaElement.getPhysicalSphere();
		double[] polarcoord = sphere.transformCoordinatesGlobalToPolar(axisOfDivision);
		return divide(0.4 + 0.2*ECM.getInstance().getRandomDouble(), axisOfDivision);
	}
	
	/**
	 * Divide mother cell in two daughter cells by coping <code>Cell</code>, <code>SomaElement</code>, 
	 * <code>PhysicalSpehre</code>, list of <code>CellModules</code>.
	 * <code>CellSubstances</code> are dispatched in the two cells.
	 * The <code>CellClock</code>  and cell lineage, if present, are also copied..
	 * When mother cell divides, by definition:
	 * 1) the mother cell becomes the 1st daughter cell
	 * 2) the new cell becomes the 2nd daughter cell and inherits a equal or bigger volume than the 1st
	 *    daughter cell, which means that this cell will eventually inherit more differentiating factors
	 *    and will be recorded in the left side of the lineage tree.  
	 *    
	 * @return the second daughter cell 
	 */
	public Cell divide(double volumeRatio, double[] dir) {
		
		// 1) Create a new daughter cell. The mother cell and the 1st daughter cell are the same java object instance!
		Cell newCell = new Cell();	
//		this.ID = idCounter.incrementAndGet();
		
		// 2) Copy the CellModules that have to be copied
		for (CellModule module : cellModules) {
			if(module.isCopiedWhenCellDivides()){
				newCell.addCellModule(module.getCopy());
			}
		}
		
		// 3) Also divide the LocalBiologyLayer 
		newCell.setSomaElement(this.somaElement.divide(volumeRatio, dir));				

		return newCell;
	}


	// *************************************************************************************
	// *      METHODS FOR CELL MODULES                                                     *
	// *************************************************************************************

	/**
	 * Adds a <code>CellModule</code> that will be run at each time step.
	 * @param m
	 */
	public void addCellModule(CellModule m){
		cellModules.add(m);
		m.setCell(this);
	}
	/**
	 * Removes a particular <code>CellModule</code> from this <code>Cell</code>.
	 * It will therefore not be run anymore.
	 * @param m
	 */
	public void removeCellModule(CellModule m){
		cellModules.remove(m);
	}

	  /** Removes all the <code>CellModule</code> in this <code>Cell</code>.*/
	public void cleanAllCellModules() {
		cellModules.clear();
	}


	// *************************************************************************************
	// *      GETTERS & SETTERS                                                            *
	// *************************************************************************************

	/** Currently, there are two types of cells : Inhibitory_cells and Excitatory_cells.*/
	public void setNeuroMLType(String neuroMLType) {
		this.neuroMLType = neuroMLType;
	}
	
	/** Currently, there are two types of cells :  <code>Inhibitory_cells</code> and  <code>Excitatory_cells</code>.*/
	public String getNeuroMLType() {
		return neuroMLType;
	}
	
	/** Returns the cell type. This is just a convenient way to store some property for the cell. 
	 * Should not be confused with NeuroMLType. 
	 */
	public String getType() {
		return somaElement.getPropertiy("cellType");
	}

	/** Sets the cell type. This is just a convenient way to store some property for the cell. 
	 * Should not be confused with NeuroMLType. 
	 */
	public void setType(String type) {
		somaElement.setPropertiy("cellType", type);
	}

	
	public SomaElement getSomaElement() {
		return somaElement;
	}

	public void setSomaElement(SomaElement somaElement) {
		this.somaElement = somaElement;
		somaElement.setCell(this);
	}

	public int getID(){
		return this.ID;
	}


	/**
	 * Sets the color for all the <code>PhysicalObjects</code> associated with the 
	 * <code>CellElements</code> of this Cell..
	 * @param color
	 */
	public void setColor(Color color) {
			somaElement.getPhysical().setColor(color);
//			for (NeuriteElement ne : getNeuriteElements()) {
//				ne.getPhysical().setColor(color);
//			}
	}

	/** Returns the list of all the CellModules.*/
	public ArrayList<CellModule> getCellModules() {
		return cellModules;
	}

	/**
	 * @return a <code>VecT</code> containing all the <code>NeuriteElement</code>s of this cell.
	 */
	public ArrayList<NeuriteElement> getNeuriteElements() {
		ArrayList<NeuriteElement> allTheNeuriteElements = new ArrayList<NeuriteElement>();
		for (NeuriteElement ne : somaElement.getNeuriteList()) {
			ne.AddYourselfAndDistalNeuriteElements(allTheNeuriteElements);
		}
		return allTheNeuriteElements;
	}

	@Override
	public void serialize(DataOutputStream os) throws IOException {
//		os.writeUTF(somaElement.getPropertiy("cellType"));
	}

	@Override
	public void deserialize(DataInputStream is) throws IOException {
		// TODO Auto-generated method stub
//		String type = is.readUTF();
//		somaElement.setPropertiy("cellType", type);
	}

}

