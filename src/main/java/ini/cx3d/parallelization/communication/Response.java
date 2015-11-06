/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.parallelization.communication;

import java.io.Serializable;

public interface Response<T> extends Serializable{
	public long getResponseMailbox();
	public void setResponseMailbox(long l);
	public T getResponse()  throws Exception ; 
	public void setResponse(T resp); 

}
