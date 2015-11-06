package ini.cx3d.utilities;

import org.w3c.dom.Node;


/**
 * This interface will make the implementer able to serialize and deserialize itself from/to xml. 
 * Each class that implements this interface should have a constructer that generates and empty sceleton of object.
 * A factory such as <code>XMLGenRegNetworkFactory</code> should handle the deserialisation 
 * of the objects that implement this interface.
 * 
 * @author andreashauri
 *
 */

public interface XMLSerializable {
	/**
	 * The implementation of this method should return a complete xml representation of the Object.
	 * @param ident how much the current block of xml is to be idented. to make a nice readable structure.
	 * @return The xml string of the object.
	 */
	public StringBuilder toXML(String ident);
	/**
	 * The implementation of this method should take an xml representation of the object and fill an 
	 * empty sceleton of this object with the data contained in the xml
	 * @param xml the XML node that represents the objects content.
	 * @return the deserialized object.
	 */
	public XMLSerializable fromXML(Node xml);
}
