/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.parallelization.communication;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.Executable;
import ini.cx3d.parallelization.ObjectHandler.Serializer;
import ini.cx3d.parallelization.ObjectHandler.commands.Command;
import ini.cx3d.simulation.ECM;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.utilities.TimeToken;
import ini.cx3d.utilities.Timer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.DeflaterOutputStream;

public class Sender implements Executable {
	protected ObjectOutputStream os = null;
	protected MyBufferedOutputStream bos= null;
	protected OutputStream oos= null;
	protected InputStream is= null;
	protected Socket clientSocket = null;       
	protected Serializer s = new Serializer();
	protected DeflaterOutputStream def;
	protected String host;
	protected long lastsentmailboxid;

	public Sender(Socket clientSocket){
		this.clientSocket=clientSocket;
		try {
			this.clientSocket.setSendBufferSize(MultiThreadScheduler.buffsize);
			OutD.println("buffersize : "+this.clientSocket.getSendBufferSize());
			//this.clientSocket.setPerformancePreferences(0, 1, 10);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.host = clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort();
		OutD.println("sender instantiated!!!!*************************************8");
		try{
//			os = new ObjectOutputStream (clientSocket.getOutputStream());
			oos = clientSocket.getOutputStream();
			is = clientSocket.getInputStream();
		
		 	os = new ObjectOutputStream (bos=new MyBufferedOutputStream(oos,100*1024*1024));
			//bos=new MyBufferedOutputStream(oos,100*1024*1024)
			//os = new ObjectOutputStream (def=new DeflaterOutputStream(oos,new Deflater(Deflater.NO_COMPRESSION)));

			os.flush();
			os.writeInt(Server.getServer().getPort());
			os.flush();
			//def.flush();
			readis();
			lastinc = System.currentTimeMillis();

		}
		catch(IOException e){
			ConnectionManager.remove(host);
			System.err.println(e);
		};

	}

	private double oldtime= -1;
	private int i=0;
	private long timeused;
	private long nbrsend;
	private long lastinc;
	public boolean run() 
	{
		if(lastinc+10*1000<System.currentTimeMillis())
		{
			if(!host.equals(Hosts.getNextHost()) && !host.equals(Hosts.getPrevHost()))
			{
				ConnectionManager.removeSender(host);
				return false;
			}
		}	
		if(toSendQueue.size()==0) return true; 
		
		if(oldtime < ECM.getInstance().getECMtime())
		{
			oldtime= ECM.getInstance().getECMtime();
			try {
//				//for ensuring that the new objects are comming through!!! not the old ones.
//				ShowConsoleOutput.println("reset");
				os.reset();
				if(i++==30)
				{
					bos.resetMeasurements();
					i=0;
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		try{

			if(!toSendQueue.isEmpty())
			{
				
				TimeToken send =  Timer.start("send");
				if(toSendQueue.peek() instanceof Command)
				{
					lastsentmailboxid =  ((Command)toSendQueue.peek()).getResponseMailboxID();
				}
				
				Serializable o= toSendQueue.remove();
				OutD.println("sending "+o.getClass());
				s.serialize(o, os);
				long current = System.nanoTime();
				os.flush();
				readis();
				
				timeused=System.nanoTime()-current;
				this.nbrsend= bos.getNum_bytes();
				bos.resetMeasurements();
				double sek = timeused/1000.0/1000.0/1000.0;
				double megabit = nbrsend/1024.0/1024.0;
//				ShowConsoleOutput.println(o+" sent "+nbrsend+" speed :"+(megabit/sek)+ " mbit/s");
				
				os.reset();
				SimulationState.getLocal().sendcount++;
				Timer.stop(send);
				lastinc = System.currentTimeMillis();
			}
			Thread.yield();
		}
		catch(Exception e){
			System.err.println(e);
			return false;
		}
		return true;
	}

	 

	private void readis()
	{
		try {
		//	ShowConsoleOutput.println(is.available()+" avilable");
			while(is.available()<=0)
			{
				//ShowConsoleOutput.println("watiing for data "+System.currentTimeMillis());
				Thread.yield();
			}
			if(is.read()!=12)
			{
				OutD.println("strange back message!");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected Queue<Serializable> toSendQueue = new ConcurrentLinkedQueue<Serializable>();

	public void send(Command c)
	{ 
		toSendQueue.add(c);
	}

	public synchronized void send(Response c)
	{
		toSendQueue.add(c);
	}


	public  String getRemoteMachineIP() {
		return host;
	}

	public String getLocalMachineIP() {
		return clientSocket.getLocalAddress().getHostAddress();
	}

	public void close()
	{
		try {
			os.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long getLastSentMailboxID()
	{
		return lastsentmailboxid;
	}
	
	public double transmissionSpeed()
	{
		return bos.getTransmitionSpeed();
	}
	
	public double getTransmitted()
	{
		return bos.getNum_bytes()/(1.0+i)/1024.0/1024.0/8.0;
	}

}
