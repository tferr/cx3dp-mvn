/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.parallelization.communication;

import ini.cx3d.utilities.HashT;

import java.io.Serializable;

public class SentRecivedPackage implements Serializable
{
	private String origin;
	private HashT<String, Long> recived;
	private HashT<String, Long> sent;
	public SentRecivedPackage( HashT<String, Long> recived,  HashT<String, Long> sent, String origin)
	{
		this.recived = recived;
		this.sent = sent;
		this.origin = origin;
	}
	
	public boolean CheckConsitency(SentRecivedPackage otherServer)
	{
		 boolean temp = recived.get(otherServer.origin) ==  otherServer.sent.get(origin);
		 temp &= recived.get(otherServer.origin) ==  otherServer.sent.get(origin);
		 return temp;
	}
	 
}