package ini.cx3d.utilities.export;

import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.simulation.ECM;
import ini.cx3d.utilities.Timer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class StatsToCSV implements IExporter {

	private transient String filename;
	private StringBuffer out = new StringBuffer(8*1024*1024);
	public StatsToCSV()
	{
		
	}

	public  void printHeader(){
		
			out.append("pref_"+ ThreadHandler.complexWorkercount+";");
			out.append("phys_"+ ThreadHandler.complexWorkercount+";");
			out.append("bio_"+ ThreadHandler.complexWorkercount+";");
			out.append("apply_"+ ThreadHandler.complexWorkercount+";");
			out.append("no exp"+ ThreadHandler.complexWorkercount+";");
			out.append("tot_"+ ThreadHandler.complexWorkercount+"");
			out.append("\n");
	}

	public void process(){
		if(filename==null )
		{	filename = Hosts.getHostName(Hosts.getLocalHost())+""+Hosts.getPort(Hosts.getLocalHost())+"_c"+ThreadHandler.complexWorkercount+".csv";
			printHeader();
		}
		out.append(Timer.getTimerTime("prefetch")+";");
		out.append(Timer.getTimerTime("physics")+";");
		out.append(Timer.getTimerTime("bio")+";");;
		out.append(Timer.getTimerTime("apply")+";");
		out.append(Timer.getTimerTime("tot_time_noexp")+";");
		out.append(Timer.getLastTotal()+"");
		out.append("\n");
	}
	
	public void writeToFile()
	{
		FileWriter outFile;
		try {
			outFile = new FileWriter(filename,false);
			PrintWriter p = new PrintWriter(outFile);
			p.println(this.out.toString());
			p.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
