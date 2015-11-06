package ini.cx3d.biology;

import java.io.Serializable;

/**
 * 
 * Classes implementing this interface can be added to a <code>Cell</code>, and be run.
 * They represent the biological model that CX3D is simulating.
 * 
 * @author fredericzubler
 *
 */
public interface CellModule extends Serializable{
	
	/** Run the simulation*/
	public void run();

	/** @return the <code>Cell</code> this module leaves in*/
	public Cell getCell();

	/**@param cell the <code>Cell</code> this module lives in*/
	public void setCell(Cell cell);
	
	/** Get a copy */
	public CellModule getCopy();
	
	/** If returns <code>true</code>, this module is copied during cell division.*/
	public boolean isCopiedWhenCellDivides();
}
