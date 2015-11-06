package ini.cx3d.electrophysiology;

import ini.cx3d.gui.MonitoringGui;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.parallelization.ObjectHandler.ThreadHandler;
import ini.cx3d.parallelization.ObjectHandler.commands.AbstractComplexCommand;
import ini.cx3d.parallelization.ObjectHandler.commands.CommandManager;
import ini.cx3d.physics.PhysicalBond;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.simulation.ECM;
import ini.cx3d.simulation.MultiThreadScheduler;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.utilities.ArrayAccessHashTable;
import ini.cx3d.utilities.TimeToken;
import ini.cx3d.utilities.Timer;

public class ElectroPhysiologySheduler {

	
	public static double currentTime = 0; 
	public static void prepareForRun()
	{
		currentTime = 0;
		ArrayAccessHashTable nodes = ECM.getInstance().getPhysicalNodes();
		for(int i =0;i<nodes.size();i++)
		{
			PhysicalNode n = nodes.get(i);
			populateElectrophysiology((PhysicalObject)n);

		}
	}

	private static void populateElectrophysiology(PhysicalObject n)
	{
		MultiThreadScheduler.execute_prefetch();
		ThreadHandler.WaitForStoping();
		if(n ==null) return;
		if(n instanceof PhysicalCylinder)
		{
			PhysicalCylinder c = ((PhysicalCylinder)n);
			if(c.getElectroPhysiolgy() !=null)
			{
				c.getElectroPhysiolgy().reset();
			}
			else
			{
				if(c.getNeuriteElement().isAnAxon())
				{
					new ElectroPhysiolgyAxon().setParrent(c);
				}
				else
				{
					new ElectroPhysiolgyDendrite().setParrent(c);
				}


			}


		}
		else
		{
			PhysicalSphere c = ((PhysicalSphere)n);
			if(c.getElectroPhysiolgy() !=null)
			{
				c.getElectroPhysiolgy().reset();
			}
			else
			{
				new ElectroPhysiolgySoma().setParrent(c);
			}
		}


		for (PhysicalBond b : n.getPhysicalBonds()) {

			if(b.getSynapse()!=null)
			{
				if(b.getElectroPhysiolgy() !=null)
				{
					b.getElectroPhysiolgy().reset();
				}
				else
				{
					new ElectroPhysiolgySynapse().setParent(b);
				}
			}
		}
	}

	public static void simulateOneStep()
	{
		execute_output();
		runAxons();
		ThreadHandler.WaitForStoping();
		runDendrites();
		ThreadHandler.WaitForStoping();
		runSoma();
		ThreadHandler.WaitForStoping();
		applyDendrites();
		applyAxons();
		applySoma();
		ThreadHandler.WaitForStoping();
		if(MonitoringGui.isSet())
		{
			MonitoringGui.getCurrent().repaint();
		}

		currentTime += ELParam.timestep;
	}

	private static void execute_output() {

		if(MultiThreadScheduler.printCurrentECMTime){
			OutD.println("stepEL = "+SimulationState.getLocal().cycle_counter);	
		}

	}

	public static void runAxons()
	{
		final ArrayHashTELP<ElectroPhysiolgyAxon> nodes = ElectroPhysiologyManager.I().axons;
		final int size =nodes.size()/MultiThreadScheduler.binsize()+1;
		for (int k = 0;k<MultiThreadScheduler.binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
					TimeToken so = Timer.start("prefetch1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;
						ElectroPhysiolgyAxon node = nodes.get(m);
						if(node==null) continue;
						node.process();
					}
					Timer.stop(so);
					return false;
				}

			});

		}

	}

	public static void applyAxons()
	{
		final ArrayHashTELP<ElectroPhysiolgyAxon> nodes = ElectroPhysiologyManager.I().axons;
		final int size =nodes.size()/MultiThreadScheduler.binsize()+1;
		for (int k = 0;k<MultiThreadScheduler.binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
					TimeToken so = Timer.start("prefetch1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;
						ElectroPhysiolgyAxon node = nodes.get(m);
						if(node==null) continue;
						node.applyCalculations();
					}
					Timer.stop(so);
					return false;
				}

			});

		}

	}

	public static void runDendrites()
	{
		final ArrayHashTELP<ElectroPhysiolgyDendrite> nodes = ElectroPhysiologyManager.I().dendrites;
		final int size =nodes.size()/MultiThreadScheduler.binsize()+1;
		for (int k = 0;k<MultiThreadScheduler.binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
					TimeToken so = Timer.start("prefetch1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;
						ElectroPhysiolgyDendrite node = nodes.get(m);
						if(node==null) continue;
						node.process();
					}
					Timer.stop(so);
					return false;
				}

			});

		}

	}

	public static void applySoma()
	{
		final ArrayHashTELP<ElectroPhysiolgySoma> nodes = ElectroPhysiologyManager.I().somas;
		final int size =nodes.size()/MultiThreadScheduler.binsize()+1;
		for (int k = 0;k<MultiThreadScheduler.binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
					TimeToken so = Timer.start("prefetch1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;
						ElectroPhysiolgySoma node = nodes.get(m);
						if(node==null) continue;
						node.applyCalculations();
					}
					Timer.stop(so);
					return false;
				}

			});

		}

	}



	public static void applyDendrites()
	{
		final ArrayHashTELP<ElectroPhysiolgyDendrite> nodes = ElectroPhysiologyManager.I().dendrites;
		final int size =nodes.size()/MultiThreadScheduler.binsize()+1;
		for (int k = 0;k<MultiThreadScheduler.binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
					TimeToken so = Timer.start("prefetch1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;
						ElectroPhysiolgyDendrite node = nodes.get(m);
						if(node==null) continue;
						node.applyCalculations();
					}
					Timer.stop(so);
					return false;
				}

			});

		}

	}

	public static void runSoma()
	{
		final ArrayHashTELP<ElectroPhysiolgySoma> nodes = ElectroPhysiologyManager.I().somas;
		final int size =nodes.size()/MultiThreadScheduler.binsize()+1;
		for (int k = 0;k<MultiThreadScheduler.binsize();k++) {
			final int f = k; 
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				@Override
				public boolean apply() {
					TimeToken so = Timer.start("prefetch1*");
					for(int m=f*size;m<(f+1)*size;m++)
					{
						if(m>=nodes.size()) return false;
						ElectroPhysiolgySoma node = nodes.get(m);
						if(node==null) continue;
						node.process();
					}
					Timer.stop(so);
					return false;
				}

			});

		}

	}


}
