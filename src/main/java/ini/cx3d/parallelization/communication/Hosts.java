/**
 * Cleaned by Andreas Hauri 01.06.2010
 */

package ini.cx3d.parallelization.communication;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.utilities.VecT;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class Hosts {

	private static String prev=""; 
	private static boolean prevactive;
	private static String next="";
	private static boolean nextactive;
	private static String localip="";
	public static ArrayList<AbstractSimpleCommand> additionalInitCommands= new ArrayList<AbstractSimpleCommand>();

	public static void addAdditionalInitCommand(AbstractSimpleCommand c)
	{
		additionalInitCommands.add(c);
	}
	
	
	public static VecT<String> getHosts() {
		VecT<String> temp =   new VecT<String>();
		if(!prev.equals(""))
		{
			temp.add(prev);
		}
		if(!next.equals(""))
		{
			temp.add(next);
		}
		return temp ;
	}

	public static String getNextHost() {

		if(next.equals("")) return null;
		return next;
	}

	public static String getPrevHost() {

		if(prev.equals("")) return null;
		return prev;
	}


	public static boolean getNextActive() {

		return nextactive;
	}

	public static boolean getPrevActive() {

		return prevactive;
	}


	public static boolean isActive(String host)
	{
		if(host==null) return false;
		if(host.equals(next)) return nextactive;
		if(host.equals(prev)) return prevactive;
		return false;
	}

	public static void setActive(String host)
	{
		if(host.equals(next)) nextactive=true;
		if(host.equals(prev)) prevactive=true;

	}

	public static String getLocalHost() {
		while(!Server.getServer().isBound())
		{Thread.yield();}
		if(localip!="") return localip+":"+Server.getServer().getPort();
		try {
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			localip = addr.getHostAddress();
			return localip+":"+Server.getServer().getPort();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;

	}

	private static String getCannonicalName(String host) {

		try {
			InetAddress addr;
			addr = InetAddress.getByName(host);
			return addr.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";

	}


	public static void RegisterAtNextHost()
	{

		String t = getNextHost();
		while(!t.equals(""))
		{

			t =	new HostInserter<String>(getLocalHost()).remoteExecuteAnswer(t);
			if(t.equals("")) continue;
			//  String host = getCannonicalName(masterhost)+":"+masterport;
			//  t  = getHostName(t);
			setNext(t);
		}
	}

	public static void setNext(String temp) {
		String t  = getCannonicalName(getHostName(temp));
		t += ":"+getPort(temp);
		next = t;
	}

	public static void setPrev(String temp) {
		String t  = getCannonicalName(getHostName(temp));
		t += ":"+getPort(temp);
		prev = t;
	}

	public static void avtivateHost(String host)
	{

		if(!isActive(host))
		{
			setActive(host);
			new ActivateHost<Boolean>(SimulationState.getLocal(),additionalInitCommands).remoteExecute(host);
		}
	}


	public static String getHostName(String hostNport)
	{
		return hostNport.substring(0, hostNport.indexOf(":"));
	}

	public static int getPort(String hostNport)
	{
		String h = hostNport.substring(hostNport.indexOf(":")+1,hostNport.length());
		return Integer.parseInt(h);
	}


	public static boolean isOnLocalHost(String host) {
		return getLocalHost().equals(host);
	}

	public static void write(ObjectOutputStream outputStream) throws IOException {
		outputStream.writeBoolean(prevactive);
		if(prevactive)
		{
			outputStream.writeUTF(prev);
		}
		else
		{
			outputStream.writeUTF("");
		}
		outputStream.writeBoolean(nextactive);
		if(nextactive)
		{
			outputStream.writeUTF(next);
		}
		else
		{
			outputStream.writeUTF("");
		}

		outputStream.writeUTF(localip);
		outputStream.writeObject(additionalInitCommands);

	}

	public static void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
		prevactive = in.readBoolean();
		prev = in.readUTF();
		nextactive= in.readBoolean();
		next = in.readUTF();
		localip = in.readUTF();
		additionalInitCommands = (ArrayList<AbstractSimpleCommand>) in.readObject();
		if(prevactive)
		{
			ping(prev);
		}
		if(nextactive)
		{
			ping(next);
		}
		boolean s = MultiThreadScheduler.active;
		
		OutD.print("waited for all pings"+s);
	}

	private static void ping(String host)
	{
		boolean answer = false;
		while(!answer)
		{
			try{
				answer = (new Ping()).remoteExecuteAnswerExceptionTimedOut(host);
			}
			catch (Exception e) {
				answer = false;
			}
		}
	}
	
	public static void executeAdditionalCommands()
	{
		for (int i=0;i<additionalInitCommands.size();i++) {
			AbstractSimpleCommand s = additionalInitCommands.get(i);
			s.apply();
		}
	}
	

}


class Ping extends AbstractSimpleCommand<Boolean>
{

	@Override
	public boolean apply() {
		
		send(new SimpleResponse<Boolean>(mailboxID,true));
		
		return false;
	}
}

class HostInserter<T> extends AbstractSimpleCommand<String>
{

	public boolean apply() {
		OutD.println("register host");
		String prev = Hosts.getPrevHost();
		if(prev!=null)
		{
			SimpleResponse<String> s =new SimpleResponse<String>(mailboxID,prev);
			send(s);
			return false;
		}
		Hosts.setPrev(hostNport);
		send( new SimpleResponse<String>(mailboxID,""));
		return false;
	}

	String hostNport;

	public  HostInserter(String hostNport)
	{
		this.hostNport = hostNport;
	}

}

class ActivateHost<Boolean> extends AbstractSimpleCommand<Boolean>
{
	private ISimulationState s;
	private ArrayList<AbstractSimpleCommand> additionalInitC = new ArrayList<AbstractSimpleCommand>();
	public ActivateHost(ISimulationState s,ArrayList<AbstractSimpleCommand> additionalInitC)
	{
		this.additionalInitC = additionalInitC;
		this.s = s;
	}

	public boolean apply() {

		for (AbstractSimpleCommand s : additionalInitC) {
			s.apply();
			Hosts.addAdditionalInitCommand(s);
		}
		s.apply();
		SimulationStateManager.simulationStates.put(client, s);
		return false;
	}

}
