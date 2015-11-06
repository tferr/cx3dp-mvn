package ini.cx3d.simulation;

import ini.cx3d.parallelization.communication.ISimulationState;
import ini.cx3d.parallelization.communication.SimulationStateManager;
import ini.cx3d.utilities.export.IExporter;

import java.util.Vector;

public class SimulationState implements ISimulationState {
	
	public static int totalGlobalObjectCount;
	
	public int totalObjectCount;
	public double stagecounter = 0;
	public int cycle_counter = 0; 	
	public double simulationTime=0;
	public double avproessingTime_in_ms=0;
	public double sendcount=0;
	public double recivecount=0;
	public int balanceRound = 10;
	public int saveround= 49;
	public double [] center = new double[] {0,0,0};
	
	public boolean artificialWallsForSpheres = false; 
	public boolean artificialWallsForCylinders = false; 
	public double [] minBoundary =  new double []{-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
	public double [] maxBoundary = new double []{Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
	public Vector<IExporter> exporters = new Vector<IExporter>();
	
	
//	public String nexthosts;
//	public String prevhost;
	
	
	private transient double[] tempwaittime = new double[balanceRound-1];
	private int i = 0;

	
	public void setProcessingTime(double waittime)
	{
		getTempwaittime()[i++%getTempwaittime().length] =waittime;
		double temp=0;
		for (double d : getTempwaittime()) {
			temp += d;
		
		}
		avproessingTime_in_ms=temp/getTempwaittime().length;
	}
	
	
	public void apply() {
		SimulationState local = getLocal();
		local.stagecounter = stagecounter;
		local.cycle_counter = cycle_counter;
		local.simulationTime = simulationTime;
		local.balanceRound = balanceRound;
		local.saveround = saveround;
		local.artificialWallsForSpheres = artificialWallsForSpheres; 
		local.artificialWallsForCylinders = artificialWallsForCylinders; 
		local.minBoundary = this.minBoundary.clone() ;
		local.maxBoundary = this.maxBoundary.clone();
		local.exporters = this.exporters;
		tempwaittime = new double[balanceRound-1];
	}
	
	public static SimulationState getLocal()
	{
		if(SimulationStateManager.localSimulationState==null)
			SimulationStateManager.localSimulationState = new SimulationState(); 
		return (SimulationState)SimulationStateManager.localSimulationState;
	}

	public static SimulationState getRemote(String host) {
		return (SimulationState)SimulationStateManager.simulationStates.get(host);
	}

	public static void put(String client, SimulationState d) {
		SimulationStateManager.simulationStates.put(client, d);
		
	}
	
	public static int getTotalGlobalNodeCount()
	{
		
		return totalGlobalObjectCount;
	}


	public SimulationState getCopy() {
		SimulationState s = new SimulationState();
		s.totalObjectCount = this.totalObjectCount;
		s.stagecounter = this.stagecounter;
		s.cycle_counter = this.cycle_counter; 	
		s.simulationTime=this.simulationTime;
		s.balanceRound = this.balanceRound;
		s.saveround = this.saveround;
		s.avproessingTime_in_ms=this.avproessingTime_in_ms;
		s.sendcount=this.sendcount;
		s.recivecount=this.recivecount;
		s.artificialWallsForSpheres = artificialWallsForSpheres; 
		s.artificialWallsForCylinders = artificialWallsForCylinders; 
		s.minBoundary = this.minBoundary.clone() ;
		s.maxBoundary = this.maxBoundary.clone();
		s.exporters = exporters;
		
		
//		s.prevhost = this.prevhost;
//		s.nexthosts = this.nexthosts;
		return s;
	}


	public void clearAverageTime() {
		setTempwaittime(new double[balanceRound-1]);
	}


	private void setTempwaittime(double[] tempwaittime) {
		this.tempwaittime = tempwaittime;
	}


	private double[] getTempwaittime() {
		if(tempwaittime==null)
		{
			tempwaittime=new double[balanceRound-1];
		}
		return tempwaittime;
	}


	
}
