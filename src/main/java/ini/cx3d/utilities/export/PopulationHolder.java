package ini.cx3d.utilities.export;



import java.util.ArrayList;


/**
 *  Utility class for passing details of a population to XML.
 * @author Toby Weston & fredericzubler
 *
 */

       
public class PopulationHolder{
	
	private String name;
	private String cellType;
	private ArrayList<InstanceHolder> instances = new ArrayList<InstanceHolder>();
	

	public  PopulationHolder(String name, String cellType){
		 this.name = name;
		 this.cellType = cellType;
	 }
	
	 public  PopulationHolder(String name, String cellType, ArrayList<InstanceHolder> instances){
		 this.name = name;
		 this.cellType = cellType;
		 this.instances = instances;
	 }

	public ArrayList<InstanceHolder> getInstances() {
		return instances;
	}
	
	public void setCells(ArrayList<InstanceHolder> instances) {
		this.instances = instances;
	}

	public String getCellType() {
		return cellType;
	}

	public String getName() {
		return name;
	}

	public void addInstanceHolder(InstanceHolder ih){
		instances.add(ih);
	}
	
public StringBuilder toXML(String ident) {
		StringBuilder sb = new StringBuilder();
		// <population name="jgj" cell_type="tztr">
		sb.append(ident).append("<population name=\"").append(name).append("\" ");
		sb.append("cell_type=\"").append(cellType).append("\">\n");
		// 		<instances size ="14">.........................................
		sb.append(ident+"   ").append("<instances size=\"").append(instances.size()).append("\">\n");
		
		// all connections: 
		for (InstanceHolder ih : instances) {
			sb.append(ih.toXML(ident+"      "));
			sb.append("\n");
		}
		// 		</instances>.........................
		sb.append(ident+"   ").append("</instances>\n");
		// </population>
		sb.append(ident).append("</population>\n");
		return sb;
	}	

	
}