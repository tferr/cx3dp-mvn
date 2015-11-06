package ini.cx3d.utilities.export;

import ini.cx3d.biology.Cell;
import ini.cx3d.biology.CellFactory;
import ini.cx3d.gui.simulation.OutD;

/**
 * Utility class for storing information on a Cell for the XML export
 * @author fredericzubler
 *
 */
public class InstanceHolder {
	
	private Cell c;
	private int id = 0;
	
	public InstanceHolder() {}
	
	public InstanceHolder(Cell c, int id) {
		super();
		this.c = c;
		this.id = id;
	}

	public void setCell(Cell cell) {
		this.c = cell;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public static void main(String[] args) {
		Cell c = CellFactory.getCellInstance(new double[] {.2, -236.474745, Math.PI});
		InstanceHolder ch = new InstanceHolder();
		ch.c = c;
		OutD.println(ch.toXML("    "));
	}
	
	
	public StringBuilder toXML(String ident) {
		double[] location = c.getSomaElement().getLocation();
		StringBuilder sb = new StringBuilder();
		sb.append(ident).append("<instance id=\"").append(id).append("\">\n");    // <instance id = "234">
		sb.append(ident).append(ident).append("<location x=\"").append(location[0]).append("\" ");
		sb.append("y=\"").append(location[1]).append("\" ");
		sb.append("z=\"").append(location[2]).append("\"/>\n");
		sb.append(ident).append("</instance>");    
		return sb;
	}	
	
}
