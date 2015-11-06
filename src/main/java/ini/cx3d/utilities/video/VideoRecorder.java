package ini.cx3d.utilities.video;


import ini.cx3d.gui.MonitoringGui;
import ini.cx3d.gui.simulation.OutD;
import ini.cx3d.utilities.video.AVIOutputStream.VideoFormat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VideoRecorder {
	
	
	public VideoRecorder(String file) {
		this.file = file;
	}
	
	
	BufferedImage img;
	private AVIOutputStream out = null;
    private Graphics2D g = null;
    
  //options
    private AVIOutputStream.VideoFormat format = VideoFormat.RAW;
    private float quality = 1f;
    private Rectangle bounds = MonitoringGui.getCurrent().getDrawingDimension();
    private String file;
    private int frameRate=1;
    private int timescale=30;
    
    
	public void start()
	{
		File file = new File(this.file);
		try {
			out = new AVIOutputStream(file, format);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.setVideoCompressionQuality(getQuality());
		        
		out.setTimeScale(frameRate);
		out.setFrameRate(timescale);
		img = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
		g = img.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, img.getWidth(), img.getHeight());
		
	}
	
	public void  paint(MonitoringGui p)
	{	
		try{
			g.setColor(Color.white);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			g.setColor(Color.black);
			boolean worked = false;
			while(!worked)
			{
				worked = false;
				try{
					p.component.paint(g);
					worked = true;
				}
				catch (Exception e) {
					OutD.println("asdfadsf");
				}
			}
			
			img.flush();
			try {
				out.writeFrame(img);
				//out.finish();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void stop()
	{
		  if (out != null) {
              try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          }
	}

	public float getQuality() {
		return quality;
	}

	public void setQuality(float quality) {
		this.quality = quality;
	}

	
	public void setBounds(int x, int y, int width, int height) {
		this.bounds = new Rectangle(x, y, width, height);
	}

	public Rectangle getBounds() {
		return bounds;
	}
	
	public void setFile(String file) {
		this.file = file;
	}

	public String getFile() {
		return file;
	}

	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public void setTimescale(int timescale) {
		this.timescale = timescale;
	}

	public int getTimescale() {
		return timescale;
	}

}
