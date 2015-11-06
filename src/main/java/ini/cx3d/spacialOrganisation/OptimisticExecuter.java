package ini.cx3d.spacialOrganisation;



public class OptimisticExecuter {

	public static void exectue(IToExectue o)
	{
		boolean s = false;
		while(!s)
		{
			try{
				o.execute();
				if(o.check())
				{
					s=true;
				}
			}
			catch (Exception e) {
				
			}
		}
	}
}
