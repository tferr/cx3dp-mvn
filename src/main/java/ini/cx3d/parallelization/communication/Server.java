/**
 * Cleaned by Andreas Hauri 01.06.2010
 */
package ini.cx3d.parallelization.communication;

import ini.cx3d.gui.simulation.OutD;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{

 
	private static Server current= new Server();
 
    public static Server getServer()
    {
    	
    	return current ;
    }
    
    
    private  Socket clientSocket = null;
    private  ServerSocket serverSocket = null; 
    private  int port;
    private boolean foundPort = false;
    public void AcceptConnections() throws IOException
    {
    	
	    
		serverSocket = new ServerSocket(port);
		Object o = new Object();
		while(true){
		    try {
		    		//OutD.println("waiting for inc con! on port "+port);
		    		
		    		clientSocket = serverSocket.accept(); 
		    		OutD.println("got one!");
		    		synchronized (o) {
		    			try{
		    				Reciver client = new Reciver(clientSocket);
		    				ConnectionManager.addReceiver(client.host, client);
		    			}
		    			catch (Exception e) {
							System.err.println(e);
						}
		    		}
		    }
		    catch (IOException e) {
		    	OutD.println(e);
			}
		    Thread.yield();
		}
    }
    
	public void run() {
		boolean open = false;
		int s = 2222;
		while(!open)
		{
			try{
				this.port = s;
				AcceptConnections();
				open = true;
			}
			catch (IOException e) {
				s++;
				OutD.println("port: "+this.port+ " taken");
			}
		}
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getPort() {
		return port;
	}
	
	public boolean isBound()
	{
		if(serverSocket==null) return false;
		return serverSocket.isBound();
	}

} 




