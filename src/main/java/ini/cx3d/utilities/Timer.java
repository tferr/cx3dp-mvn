package ini.cx3d.utilities;

import ini.cx3d.gui.simulation.OutD;

import java.util.ArrayList;
import java.util.HashMap;

public class Timer {

	private static int timestage;
	private static long total;
	private static long startingTime;
	
	private static HashMap<String, Long> addedup = new HashMap<String, Long>();
	private static ArrayList<String> temp = new ArrayList<String>();
	
	
	public synchronized static void startRound()
	{
		clearAll();
		startingTime = System.currentTimeMillis();
	}
	
	public synchronized static void stopRound()
	{
		timestage ++;
		total = System.currentTimeMillis()-startingTime;
		preparePrint();
	}
	
	public static TimeToken start(String name)
	{
		TimeToken s = new TimeToken(timestage,name);
		s.init();
		return s;
	}

	public static void stop(TimeToken  t)
	{
		if(t.isValid(timestage))
		{
			
			long last =0;
			if(addedup.containsKey(t.getTimerName()))
			{
				if(addedup==null)
				{
					System.out.println("asdfasfa");
				}
				if(t==null)
				{
					System.out.println("awwerre");
				}
				if(t.getTimerName()==null)
				{
					System.out.println("awwwww");
				}
				try
				{
					last = addedup.get(t.getTimerName());
				}
				catch (Exception e) {
	
				}
			}
			addedup.put(t.getTimerName(),last+t.elapsedtime());
			t.setInvalid();
		}
		else
		{
			OutD.println("timetoken invalid!!");
		}
	}

	public  synchronized static long getLastTotal()
	{
		return total;
	}

	public synchronized static void printPercentages()
	{
		OutD.println("Recorded times:");
		for (String s : temp) {
			OutD.print(s);
		}

	}

	public static void clearAll() {

		try{
			for (String k: addedup.keySet()) {
				addedup.put(k, 0L);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}

	}

	public static ArrayList<String> getTimerStringInfo() {

		return temp;

	}

	public static void preparePrint() {
		temp.clear();
		try{
		for (String key : addedup.keySet()) {
			if(!key.contains("*"))
			{
				double precent = 100.0/total*addedup.get(key);
				temp.add(String.format(key+" : %2.2f%n", precent)+"%    t:"+addedup.get(key));
			}
		}
		minius("search","searchrange");
		minius("searchrange","visit");
		plus(new String[]{"prefetch","physics","bio","apply"});
		plus(new String[]{"allinone","apply changes"});
//		realation("prefetch1*","prefetch");
//		realation("phys1*","physics");
//		realation("bio1*","bio");
//		realation("apply1*","apply");
//		realation("genmarg1*","genmarg");
		}catch(Exception e){}
	}

	private static void minius(String key1, String key2) {
		if(!addedup.containsKey(key1) ) return;
		if(!addedup.containsKey(key2) ) return;
		double abs= (addedup.get(key1)-addedup.get(key2));
		double precent = 100.0/total*abs;
		temp.add(String.format(key1+"-"+key2+" : %2.2f%n",precent)+"%    t:"+abs);
	}
	
	private static void realation(String key1, String key2) {
		if(!addedup.containsKey(key1) ) return;
		if(!addedup.containsKey(key2) ) return;
		double a = addedup.get(key1);
		double b = addedup.get(key2);
		double abs= (a/b);
		temp.add(String.format(key1+"/"+key2+" : %2.2f%n",abs));
	}
	
	private static void plus(String[] keys) {
		int abs= 0;
		String s =""; 
		for(int i=0;i<keys.length;i++)
		{
			if(!addedup.containsKey(keys[i]) ) return;
			s+=keys[i]+",";
			abs+=addedup.get(keys[i]);
		}
		double precent = 100.0/total*abs;
		temp.add(String.format(s+ " : %2.2f%n",precent)+"%    t:"+abs);
	}

	
	public static double getTimer(String key) {
		double getT =0;
		if(addedup.containsKey(key))
		{
			getT= addedup.get(key);
		}
		return 100.0/total*getT;
	}

	public static double getTimerTime(String string) {
		// TODO Auto-generated method stub
		try{
			return addedup.get(string);
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		return 0;
	}

}

