package ini.cx3d.utilities;

public class TimeToken
{
	private int stage;
	private long milies;
	private String timer; 
	
	public TimeToken(int stage,String timer)
	{
		this.timer = timer;
		this.stage=stage;
	}
	
	public void init()
	{
		milies = System.currentTimeMillis();
	}
	
	public void setInvalid()
	{
		stage = -1;
	}

	public long elapsedtime()
	{
		return System.currentTimeMillis()-milies;
	}
	
	boolean isValid(int i)
	{
		return i == stage;
	}

	public String getTimerName() {
		return timer;
	}
}
