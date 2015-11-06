package ini.cx3d.spacialOrganisation;

import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.gui.spacialOrganisation.WhatTheHellAreYouWaitingFor;
import ini.cx3d.parallelization.ObjectHandler.commands.AbstractComplexCommand;
import ini.cx3d.parallelization.ObjectHandler.commands.CommandManager;
import ini.cx3d.spacialOrganisation.commands.Remote_InsertBulk;
import ini.cx3d.utilities.HashT;

public class InsertionHandler {

	private static HashT<String, Remote_InsertBulk<?>> tobeInserted = new HashT<String, Remote_InsertBulk<?>>();
	
	
	public static boolean addTobeInserted(Remote_InsertBulk<?> a)
	{
		if(tobeInserted.containsKey(a.getClient())) return false;
		OutD.println("recived bulck from  "+a.getClient()+a.getStage());
		tobeInserted.put(a.getClient(), a);
		return true;
	}
	
	public static void StartArivalCheckers()
	{
		for (final SingleRemotePartitionManager i : ManagerResolver.I().getRemotePartitions()) {		
			CommandManager.addCommandToQueue(new AbstractComplexCommand<Integer>(){
				
				@Override
				public boolean apply() {
					
					
					String host = i.getAddress().getHost();
					String s = host+" arivalchecker";
					WhatTheHellAreYouWaitingFor.waitfor(s, "waiting for"+host+" arival checker");
					if(!tobeInserted.containsKey(host)) return true;
					tobeInserted.get(host).execute();
					tobeInserted.remove(host);
					WhatTheHellAreYouWaitingFor.notAnymore(s);
					return false;
				}
			});

		}
		
	}
}
