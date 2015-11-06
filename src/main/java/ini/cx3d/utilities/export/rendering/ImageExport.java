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

public class ImageExport implements IExporter{

	public transient MonitoringImage m;
	transient BufferedImage o;
	transient int counter;
	public int processinground =1;
	private String folder;
	private String fileN;
	public ImageExport(String basefilenameAndFolder,MonitoringImage m)
	{
		this.folder = basefilenameAndFolder.substring(0, basefilenameAndFolder.lastIndexOf("/")+1)+getDateTimeFolderName()+"/";
		this.fileN = basefilenameAndFolder.substring(basefilenameAndFolder.lastIndexOf("/")+1,basefilenameAndFolder.length());
		this.m = m;
	}
	
	public ImageExport(String folder,String basefilename,MonitoringImage m)
	{
		this.folder = folder+getDateTimeFolderName()+"/";
		this.fileN = basefilename;
		this.m = m;
	}
	
	private BufferedImage generateImage()
	{
		 //  	m.component.optimizeViewToRoi();
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
			if(Hosts.getNextHost()==null) //first comp;
			{
				this.o = generateImage();
				
			}
			
			waitForImageToArrive();
			renderMyImagePart();
			
			if(Hosts.getPrevHost()!=null && Hosts.getPrevActive())
			{
				(new RemoteRendering(this.o,this.m,counter)).remoteExecute(Hosts.getPrevHost());
			}
			else
			{
				save();
			}
			this.o = null;
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

class RemoteRendering extends AbstractComplexCommand<Boolean>
{
	private byte[] o;
	private MonitoringImage m;
	private int counter;
	
	
	public RemoteRendering(BufferedImage o,MonitoringImage m,int counter )
	{
		this.o = toByteArray(o);
		this.m = m;
		this.counter = counter;
	}
	
	@Override
	public boolean apply() {
		
		ImageExport e = findExporter();
		e.counter = counter;
		e.m = this.m;
		e.o =fromByteArray(o);
		return false;
	}
	
	
	private ImageExport findExporter()
	{
		for(IExporter e: SimulationState.getLocal().exporters)
		{
			if(e instanceof ImageExport)
			{
				return(ImageExport) e;
			}
		}
		return null;
	}
	
	private BufferedImage fromByteArray(byte[] imagebytes) {
		try {
			if (imagebytes != null && (imagebytes.length > 0)) {
				BufferedImage im = ImageIO.read(new ByteArrayInputStream(imagebytes));
				return im;
			}
			return null;
		} catch (IOException e) {
			throw new IllegalArgumentException(e.toString());
		}
	}
 
	private byte[] toByteArray(BufferedImage bufferedImage) {
		if (bufferedImage != null) {
			BufferedImage image = bufferedImage;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ImageIO.write(image, "png", baos);
			} catch (IOException e) {
				throw new IllegalStateException(e.toString());
			}
			byte[] b = baos.toByteArray();
			return b;
		}
		return new byte[0];
	}
	
	
	
}
