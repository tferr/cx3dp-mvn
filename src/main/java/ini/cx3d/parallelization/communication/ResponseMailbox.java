/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.communication;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.ComplexWorker;
import ini.cx3d.utilities.HashT;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ResponseMailbox {

	private static HashT<Long, LinkedBlockingQueue<Response>> mailboxes = new HashT<Long, LinkedBlockingQueue<Response>>(); 
	public static Response getResponse(long responseMailbox) throws TimeoutException
	{
		synchronized (mailboxes) {
			if(!mailboxes.containsKey(responseMailbox))
			{
				mailboxes.put(responseMailbox, new LinkedBlockingQueue<Response>());

			}
		}

		Response<?> r;
		ComplexWorker w = null;
		int i = 1;
		while (true) {
			try {
				r = mailboxes.get(responseMailbox).poll(1000,TimeUnit.MILLISECONDS);
				
//################################We need a proper recovery here!!
				if (r != null) {
					if(w!=null) w.quit();
					mailboxes.remove(responseMailbox);
					return r;
				}
				else
				{
					if(w==null)
					{
						//w = ThreadHandler.introduceNewComplexCommandWorker();
					}
					if(i==1)
					{
						OutD.println("\n ResponseMailbox.getResponse() got no response!!! from "+responseMailbox);
						OutD.println("\n");
					}
					if(i++%100000==0)
					{

						(new Exception()).printStackTrace();

						OutD.println("timed out:-(");
						throw new TimeoutException();
					}
				}
			} catch (InterruptedException e) {
				OutD.println("AbstractCommand.remoteExecuteAnswerException: poll interrupted");

			}
		}

	}
	
	public static Response getResponseTimedout(long responseMailbox) throws TimeoutException
	{
		synchronized (mailboxes) {
			if(!mailboxes.containsKey(responseMailbox))
			{
				mailboxes.put(responseMailbox, new LinkedBlockingQueue<Response>());

			}
		}

		Response<?> r;
		ComplexWorker w = null;
		int i = 1;
		while (true) {
			try {
				r = mailboxes.get(responseMailbox).poll(1000,TimeUnit.MILLISECONDS);
				
//################################We need a proper recovery here!!
				if (r != null) {
					if(w!=null) w.quit();
					mailboxes.remove(responseMailbox);
					return r;
				}
				else
				{
					throw new TimeoutException();
				}
			} catch (InterruptedException e) {
				OutD.println("AbstractCommand.remoteExecuteAnswerException: poll interrupted");

			}
		}

	}
	

	public static void insertIntoMailbox(long responseMailbox,Response s)
	{
		synchronized (mailboxes) {
			if(!mailboxes.containsKey(responseMailbox))
			{
				mailboxes.put(responseMailbox, new LinkedBlockingQueue<Response>());	
			}
		}
		mailboxes.get(responseMailbox).add(s);
	}

}
