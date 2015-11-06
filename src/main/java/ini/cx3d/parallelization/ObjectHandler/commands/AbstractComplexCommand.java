/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.ObjectHandler.commands;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.communication.ConnectionManager;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.parallelization.communication.Response;
import ini.cx3d.parallelization.communication.ResponseMailbox;
import ini.cx3d.simulation.ECM;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractComplexCommand<T> implements Command{


	private static final long serialVersionUID = 5569474964848997626L;
	transient protected String client;
	private boolean close;
	protected long mailboxID;

	public abstract boolean apply();

	public AbstractComplexCommand() {
		this.mailboxID = ECM.getInstance().getCommandCounter().incrementAndGet();
	}

	public boolean run() {
		return apply();
	}

	public void setClient(String client)
	{
		this.client = client;
	}

	public String getClient()
	{
		return this.client;
	}

	public long getResponseMailboxID() {
		return mailboxID;
	}

	protected void send(Response o)
	{
		try{
			o.setResponseMailbox(this.mailboxID);
			ConnectionManager.getSender(client).send(o);
		}
		catch (Exception e) {			
			OutD.println("AbstractCommand.send(): "+ this.getClass().getCanonicalName() + " could not send answer cause "+e.getMessage());
		}
	}


	public T remoteExecuteAnswer(String clientName) {
		try {
			return remoteExecuteAnswerException(clientName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void remoteExecute(String clientName) {
		try {
			ConnectionManager.getSender(clientName).send(this);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void remoteExecuteOnAllNeighbouringHosts() {

		try {
			for (String host : Hosts.getHosts()) {
				ConnectionManager.getSender(host).send(this);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public T remoteExecuteAnswerException(String clientName) throws Exception {


		while(true)
		{
			try {
				ConnectionManager.getSender(clientName).send(this);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassCastException e) {
				e.printStackTrace();
			}
			try{
				return ((Response<T>)ResponseMailbox.getResponse(getResponseMailboxID())).getResponse();
			}
			catch(TimeoutException e2)
			{
				OutD.println("timed out resending!");
			}

		}
	}
	
	public T remoteExecuteAnswerExceptionTimedOut(String clientName) throws Exception {


		while(true)
		{
			try {
				ConnectionManager.getSender(clientName).send(this);
			} catch (UnknownHostException e) {
				System.out.println(e.getMessage()+" not connected retry");
			} catch (IOException e) {
				System.out.println(e.getMessage()+" not connected retry");
			} catch (ClassCastException e) {
				System.out.println(e.getMessage()+" not connected retry");
			}
			return ((Response<T>)ResponseMailbox.getResponseTimedout(getResponseMailboxID())).getResponse();
			
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public void remoteExecuteAnswerAsyncException(String clientName) throws Exception {
		
			try {
				ConnectionManager.getSender(clientName).send(this);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassCastException e) {
				e.printStackTrace();
			}
	}
	
	public T ReadRemoteAnswerAsyncException() throws TimeoutException, Exception
	{
		return ((Response<T>)ResponseMailbox.getResponse(getResponseMailboxID())).getResponse();
	}
	
	
	
	

	public CommandType commandType()
	{
		return CommandType.complex;
	}

	public String toString() {
		if (this.client != null)
			return new String(this.getClass().getSimpleName()+super.toString());
		else 
			return new String(this.getClass().getSimpleName()+", originating from "+this.client+super.toString());

	}
	

}
