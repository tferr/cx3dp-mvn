/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.communication;

import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.VecT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectionManager {

	private static HashT<String, Sender> senders = new HashT<String, Sender>(); 
	private static HashT<String, Reciver> recivers = new HashT<String, Reciver>();

	
	static
	{
		Thread t = new Thread( new Runnable() {

			public void run() {
				while(true)
				{
					VecT<String> keys = null;;
					while(true)
					{
						try{
						
							keys = new VecT<String>(senders.keySet());
							break;
						}
						catch (Exception e) {
							// TODO: handle exception
						}
					}
					for(String i : keys)
					{
						Sender sen = senders.get(i);
						sen.run();
					}

					try {
						Thread.sleep(0);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});
		t.start();
		t.setName("sender-Thread");
	}
	
	public static Sender getSender(String host) throws UnknownHostException, IOException
	{
		
		if(!senders.containsKey(host))
		{
			synchronized (senders) {
				if(!senders.containsKey(host))
				{
					Socket clientSocket = new Socket(Hosts.getHostName(host),Hosts.getPort(host)); 
					Sender client = new Sender(clientSocket);
					senders.put(host, client);
				}
			}
		}

		return senders.get(host);
	}



	public static void addReceiver(String host,final Reciver reciver)
	{

		synchronized (recivers) {
			if(recivers.containsKey(host))
			{ 

				recivers.remove(host);
			}
			recivers.put(host, reciver);
			Thread t = new Thread(new Runnable() {

				public void run() {
					while(true)
					{
						if(!reciver.run()) break;
						try {
							Thread.sleep(0);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			});
			//    t.setPriority(2);
			t.start();
			t.setName("recive for "+host);

		}
	}

	public static void remove(String host)
	{
		senders.remove(host);
		recivers.remove(host);
	}

	public static void removeSender(String host)
	{
		senders.remove(host);
	}

	public static void removeReciver(String host)
	{
		recivers.remove(host);
	}

	public static SentRecivedPackage getSentAndRecived()
	{
		HashT<String, Long> recived = new HashT<String, Long>();
		HashT<String, Long> sent = new HashT<String, Long>();
		for (String host : Hosts.getHosts()) {
			recived.put(host, recivers.get(host).getLastRecivedMailboxID());
		}
		for (String host : Hosts.getHosts()) {
			sent.put(host, senders.get(host).getLastSentMailboxID());
		}
		String local=null;
		try {
			local = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return new SentRecivedPackage(recived,sent,local);
	}

}

