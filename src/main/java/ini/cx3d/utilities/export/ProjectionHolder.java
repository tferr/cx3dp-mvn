package ini.cx3d.utilities.export;

import ini.cx3d.gui.simulation.OutD;

import java.util.ArrayList;


/**
 * Utility class for storing details on a bunch of connections for the XML export. 
 * @author Toby Weston & fredericzubler
 *
 */


public class ProjectionHolder {

	String name;
	String source;
	String target;
	String synapse_type;
	String prop_delay = "5.0e-3";
	double weight = 1.0;
	
	ArrayList<ConnectionHolder> connections = new ArrayList<ConnectionHolder>(); 
	
	ProjectionHolder(){}

	public ProjectionHolder(String name, String source, String target, String synapse_type) {
		super();
		this.name = name;
		this.source = source;
		this.target = target;
		this.synapse_type = synapse_type;
	}
	
	public void addConnectionHolder(ConnectionHolder ch){
		connections.add(ch);
	}
	
	public StringBuilder toXML(String ident) {
		
		StringBuilder sb = new StringBuilder();
		// <projection name="jgj" source="tztr" target="fds">
		sb.append(ident).append("<projection name=\"").append(name).append("\" ");
		sb.append("source=\"").append(source).append("\" ");
		sb.append("target=\"").append(target).append("\">\n");
		// 		<synapse_props synapse_type="jgj" prop_delay="tztr" weight="fds">
		// 		</synapse_props
		sb.append(ident+"   ").append("<synapse_props synapse_type=\"").append(synapse_type).append("\" ");
		sb.append("prop_delay=\"").append(prop_delay).append("\" ");
		sb.append("weight=\"").append(weight).append("\">\n");
		sb.append(ident+"   ").append("</synapse_props>\n");
		// 		<connections>.........................
		sb.append(ident+"   ").append("<connections>\n");
		// all connections: 
		for (ConnectionHolder ch : connections) {
			sb.append(ch.toXML(ident+"      "));
			sb.append("\n");
		}
		// 		</connections>.........................
		sb.append(ident).append(ident).append("</connections>\n");
		// </projections>
		sb.append(ident).append("</projection>\n");
		return sb;
	}	
	
	public static void main(String[] args) {
		ProjectionHolder ph = new ProjectionHolder("Excite_to_Excit", "inhibitory_cells", "Excitatory_cells", "Inh_Syn");
		ph.connections.add(new ConnectionHolder(14,60));
		ph.connections.add(new ConnectionHolder(32,4));
		OutD.println(ph.toXML("  "));
	}
	
	// Roman Bauer: Implement getters and setters to calculate connectivity pattern
	public ArrayList<ConnectionHolder> getConnections() {
		return this.connections;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setConnections(ArrayList<ConnectionHolder> c) {
		this.connections = c;
	}
	
	public void setName(String s) {
		this.name = s;
	}
	
	public int getNrOfConnections() {
		return connections.size();
	}
	// Roman Bauer

}