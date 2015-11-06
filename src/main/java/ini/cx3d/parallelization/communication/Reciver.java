/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.communication;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.Executable;
import ini.cx3d.parallelization.ObjectHandler.Serializer;
import ini.cx3d.parallelization.ObjectHandler.commands.Command;
import ini.cx3d.parallelization.ObjectHandler.commands.CommandManager;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.simulation.SimulationState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Reciver implements Executable {
	
	protected ObjectInputStream is = null;
	protected Socket clientSocket = null;
	protected OutputStream os = null;
	protected Serializer s = new Serializer();
    protected String host;
    protected long lastrecivedmailbox;
    private double lastinc ;
    
    public Reciver(Socket clientSocket){
    	this.clientSocket=clientSocket;
    	try {
			this.clientSocket.setReceiveBufferSize(MultiThreadScheduler.buffsize);
			OutD.println("buffersize : "+this.clientSocket.getReceiveBufferSize());
			//this.clientSocket.setPerformancePreferences(0, 1, 10);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	OutD.println("Reciver instantiated!!!!*************************************");
    	int port=0;
    	try{
			//	is = new ObjectInputStream(clientSocket.getInputStream());
    			os = clientSocket.getOutputStream();
				//is = new ObjectInputStream(new InflaterInputStream(new MyBufferedInputStream(clientSocket.getInputStream(),100*1024*1024),new Inflater()));
				is = new ObjectInputStream(new MyBufferedInputStream(clientSocket.getInputStream(),100*1024*1024));
				port = is.readInt();
				sendconfirm();
    	}
    	catch(IOException e){
			System.err.println("Reciver.Reciver()");
			System.err.println(e);
			ConnectionManager.remove(host);
		};
		lastinc = System.currentTimeMillis();
		OutD.println("--> port is"+port);
		this.host = clientSocket.getInetAddress().getHostAddress()+":"+port;
	    
    }

	private void sendconfirm() throws IOException {
		os.write(12);
		os.flush();
//		ShowConsoleOutput.println("sent confirm");
	}
    
    public boolean run() 
    {
    	
		try{
			if(lastinc+10*1000<System.currentTimeMillis())
			{
				if(!host.equals(Hosts.getNextHost()) && !host.equals(Hosts.getPrevHost()))
				{
					clientSocket.close();
				}
			}
			if(clientSocket.isClosed()) return false;
	    	while(clientSocket.getInputStream().available()>0)
	    	{	
//	    		ShowConsoleOutput.println("got something");
	    		try {
	    			
	    			Object object = s.deserialize(is);
	    			sendconfirm();
	    			
	    			SimulationState.getLocal().recivecount++;
//	    			ShowConsoleOutput.println("revived!   "+object.getClass()+ " nbr ="+SimulationState.getLocal().recivecount);
	    			if(object instanceof Response)
	    			{
	    				Response resp = (Response) object;
	    				if(resp.getResponseMailbox() ==-1)
	    				{
	    					throw new RuntimeException("what mailbox is this?");
	    				}
	    				else
	    				{
	    					ResponseMailbox.insertIntoMailbox(resp.getResponseMailbox(),resp);
	    				}
	    			}
	    			else if(object instanceof Command)
	    			{
	    				lastrecivedmailbox =  ((Command)object).getResponseMailboxID();
	    				Command com = (Command)object;
	    				if(com.getClient()==null)
	    				{
	    					com.setClient(host);
	    				}
	    				CommandManager.addCommandToQueue(com);
	    			}
	    			
	    			lastinc = System.currentTimeMillis();
				} catch (ClassNotFoundException e) {
					System.err.println("Reciver.run()");
					e.printStackTrace();
				}
				
				
	    	}
	    	
		}
		catch(IOException e){
			debug_wirteStream(e);
			return false;
		}
		return true;
    }
    
     
    
    private void checkConnectionClose(Object o) {

	}
    
	private void debug_wirteStream(IOException e) {
		ConnectionManager.remove(host);
		OutD.println("Reciver.run() 2");
		e.printStackTrace();
		System.err.println(e);
		try {
			OutD.println(clientSocket.getInputStream().available());
			byte [] b = new byte[clientSocket.getInputStream().available()];
			clientSocket.getInputStream().read(b);
			for (int i =0; i<b.length ; i++) {
				OutD.print(((char)b[i]) +"");
				if(i%100==0)OutD.println();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
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
			is.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	public long getLastRecivedMailboxID()
	{
		return lastrecivedmailbox;
	}
	
}
