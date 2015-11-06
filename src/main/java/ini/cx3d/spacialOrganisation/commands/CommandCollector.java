package ini.cx3d.spacialOrganisation.commands;

import ini.cx3d.parallelization.ObjectHandler.commands.AbstractSimpleCommand;
import ini.cx3d.physics.diffusion.DiffusionMargin;
import ini.cx3d.simulation.commands.SpreadStage;
import ini.cx3d.spacialOrganisation.ManagerResolver;
import ini.cx3d.spacialOrganisation.ObjectReference;
import ini.cx3d.utilities.HashT;
import ini.cx3d.utilities.VecT;

public class CommandCollector extends AbstractSimpleCommand<ObjectReference>{

	public static HashT<String, CommandCollector> commands = new HashT<String, CommandCollector>();

	public  static CommandCollector get(String host)
	{
		if(!commands.containsKey(host))
		{
			commands.put(host, new CommandCollector());
		}
		return commands.get(host);
	}
	
	public static void execute()
	{
		if(ManagerResolver.I().getRemotePartitions().size()!=commands.keySet().size())
		{
			System.out.println("nooooo not working!!!");
		}
		for (String host : commands.keySet()) {
			commands.get(host).remoteExecute(host);
		}
		commands.clear();
	}
	
	private VecT<Remote_InsertBulk> inserts = new VecT<Remote_InsertBulk>();
	public DiffusionMargin diffMargin; 
	public RemoteMarginBox margin;
	private SpreadStage s;
	
	public  void add(Remote_InsertBulk a)
	{
		inserts.add(a);
	}

	public  void add(SpreadStage a)
	{
		s=a;
	}

	@Override
	public boolean apply() {
		// TODO Auto-generated method stub
		
		
		if(diffMargin!=null)
		{
			diffMargin.setClient(this.client);
			diffMargin.apply();
		}
	
		if(margin!=null)
		{
			margin.setClient(this.client);
			margin.apply();

		}
		
		for (Remote_InsertBulk o: inserts) {
			o.setClient(this.client);
			o.apply();
		}
	
		//	ShowConsoleOutput.println("__apply margin"+o.p.address);

		
		if(s!=null)
		{
//			ShowConsoleOutput.println("Command Collector stage ::"+s.d.stagecounter);
			s.setClient(this.client);
			s.apply();
			
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		if(inserts.size()>0) return " inserts ";
		//if(margins.size()>0) return " margins ";
		else return " others ";
		
	}

	
}