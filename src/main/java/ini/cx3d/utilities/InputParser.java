package ini.cx3d.utilities;

import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.simulation.ECM;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.simulation.SimulationState;

public class InputParser {

	public static HashT<String, Action> actions = new HashT<String, Action>(); 
	public static void interpeteArguments(String [] args)
	{
		boolean sucess =true;
		for(int i=0;i<args.length;i+=2)
		{
			sucess&=check(args[i],args[i+1]);
		}
		if(sucess==false)
		{
			showall();
		}
	}
	private static void showall() {
		System.out.println("possible options are:");
		for (String s : actions.keySet()) {
			System.out.println(s);

		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private static boolean check(String string, String string2) {

		if(actions.containsKey(string))
		{
			actions.get(string).execute(string2);
			return true;
		}
		return false;
	}


	public static void fillStandard()
	{
		actions.put("-Cworkers", new Action() {

			@Override
			public void execute(String opts) {
				int i = Integer.parseInt(opts);
				ThreadHandler.complexWorkercount =i;
			}
		});

		actions.put("-Sworkers", new Action() {

			@Override
			public void execute(String opts) {
				int i = Integer.parseInt(opts);
				ThreadHandler.simpleWorkercount =i;
			}
		});


		actions.put("-Mhost", new Action() {

			@Override
			public void execute(String opts) {
				Hosts.setNext(opts);
			}
		});


		actions.put("-PMmax", new Action() {

			@Override
			public void execute(String opts) {
				int i = Integer.parseInt(opts);
				MultiThreadScheduler.maxnodesperPM=i;
			}
		});

		actions.put("-Globalmin", new Action() {

			@Override
			public void execute(String opts) {
				int i = Integer.parseInt(opts);
				MultiThreadScheduler.globalmin=i;
			}
		});

		actions.put("-Balanceround", new Action() {

			@Override
			public void execute(String opts) {
				int i = Integer.parseInt(opts);
				SimulationState.getLocal().balanceRound=i;
			}
		});

		actions.put("-Waitdiff", new Action() {

			@Override
			public void execute(String opts) {
				int i = Integer.parseInt(opts);
				MultiThreadScheduler.diffWatingTime=i;
			}
		});

		actions.put("-Buffsize", new Action() {

			@Override
			public void execute(String opts) {
				int i = Integer.parseInt(opts);
				MultiThreadScheduler.buffsize=i;
			}
		});

		actions.put("-LoadFile", new Action() {

			@Override
			public void execute(String opts) {
				ECM.setToBeLoaded(opts);
				MultiThreadScheduler.active = true;
			}
		});

		actions.put("-SaveRound", new Action() {

			@Override
			public void execute(String opts) {
				int i = Integer.parseInt(opts);
				SimulationState.getLocal().saveround=i;
			}
		});

		actions.put("-SaveName", new Action() {

			@Override
			public void execute(String opts) {
				MultiThreadScheduler.savename=opts;
			}
		});

	}

}

