package ini.cx3d.utilities.export.rendering;


import ini.cx3d.parallelization.ObjectHandler.commands.AbstractComplexCommand;
import ini.cx3d.parallelization.communication.Hosts;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.utilities.export.IExporter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;

public class ImageExportLocal implements IExporter{

	transient MonitoringImage m;
	transient BufferedImage o;
	transient int counter;
	public int processinground =1;
	private String folder;
	private String fileN;
	public ImageExportLocal(String basefilenameAndFolder,MonitoringImage m)
	{
		this.folder = basefilenameAndFolder.substring(0, basefilenameAndFolder.lastIndexOf("/")+1)+getDateTimeFolderName()+"/";
		this.fileN = basefilenameAndFolder.substring(basefilenameAndFolder.lastIndexOf("/")+1,basefilenameAndFolder.length());
		this.m = m;
	}
	
	public ImageExportLocal(String folder,String basefilename,MonitoringImage m)
	{
		this.folder = folder+getDateTimeFolderName()+"/";
		this.fileN = basefilename;
		this.m = m;
	}
	
	private BufferedImage generateImage()
	{
		
			BufferedImage bf = new BufferedImage(m.component.width,m.component.height,BufferedImage.TYPE_INT_RGB);
			m.renderBackground(bf);
			return bf;
		
	}
	
	public void renderMyImagePart()
	{
	//	m.component.optimizeViewToRoi();
		m.render(o);
	}
	
	private void save()
	{
		
		(new File(folder)).mkdirs();
		
		DecimalFormat myFormatter = new DecimalFormat("000000");
		String temp = folder+fileN+ myFormatter.format(counter)+".png";
		File file = new File(temp);
		try {
			ImageIO.write(o, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}


	

	@Override
	public void process() {
		if(m ==null) return;
		if(SimulationState.getLocal().stagecounter%processinground==0)
		{
			
			this.o = generateImage();
			
			waitForImageToArrive();
			renderMyImagePart();
			
			if(Hosts.getPrevHost()!=null && Hosts.getPrevActive())
			{
				(new RemoteRendering2(this.m,counter)).remoteExecute(Hosts.getPrevHost());
			}
			
			save();
			counter++;
		}
	}
	
	private void waitForImageToArrive()
	{
		while(o==null)
		{
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public String getDateTimeFolderName()
	{
	    Date today = Calendar.getInstance().getTime();
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss");
	    return formatter.format(today);
	}
	

}

class RemoteRendering2 extends AbstractComplexCommand<Boolean>
{
	
	private MonitoringImage m;
	private int counter;
	
	
	public RemoteRendering2(MonitoringImage m,int counter )
	{
		this.m = m;
		this.counter = counter;
	}
	
	@Override
	public boolean apply() {
		
		ImageExportLocal e = findExporter();
		e.counter = counter;
		e.m = this.m;
		return false;
	}
	
	
	private ImageExportLocal findExporter()
	{
		for(IExporter e: SimulationState.getLocal().exporters)
		{
			if(e instanceof ImageExportLocal)
			{
				return(ImageExportLocal) e;
			}
		}
		return null;
	}
	
}
