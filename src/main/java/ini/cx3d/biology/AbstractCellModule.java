package ini.cx3d.biology;

public abstract class AbstractCellModule implements CellModule {

	protected Cell cell; // and not "private", so that subclass can access it
	
	public Cell getCell() {
		return cell;
	}

	public void setCell(Cell cell) {
		this.cell = cell;
	}
	
	public abstract CellModule getCopy();
	
	/** By default returns true.*/
	public boolean isCopiedWhenCellDivides() {
		return true;
	}

	public abstract void run();


}
