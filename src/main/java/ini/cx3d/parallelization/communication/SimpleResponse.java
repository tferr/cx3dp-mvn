/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.communication;


public class SimpleResponse<T> implements Response<T>{
	private static final long serialVersionUID = -3580862521775342955L;

	long mailbox;
	
	T response;

	private boolean close;

	public SimpleResponse(long mailbox, T response) {
		this.mailbox = mailbox;
		this.response = response;
	}
	
	public SimpleResponse() {
		this.mailbox = -1;
		this.response = null;
	}
	
	
	public String toString() {
		return response+"";
	}
	
	public T getResponse() throws Exception {
		if(response instanceof Exception)
		{
			throw (Exception)response;
		}
		return response;
	}

	public long getResponseMailbox() {
		return mailbox;
	}

	public void setResponse(T response) {
		this.response = response;		
	}

	public void setResponseMailbox(long s) {
		mailbox = s;
		
	}
	
}
