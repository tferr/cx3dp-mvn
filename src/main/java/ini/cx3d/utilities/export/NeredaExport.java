package ini.cx3d.utilities.export;

import ini.cx3d.biology.NeuriteElement;
import ini.cx3d.biology.SomaElement;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.simulation.ECM;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class NeredaExport implements IExporter {

	StringBuffer b;
	@Override
	public void process() {
		b = new StringBuffer();
		b.append("<cells>\n");
		for(PhysicalSphere sp: ECM.getInstance().getPhysicalSphereList())
		{
			
			SomaElement s = sp.getSomaElement();
			if(s.getNeuriteList().size()!=0)
			{
				processSoma(s,b,"  ");
			}
		}
		b.append("</cells>\n");
		
	}

	public void processSoma(SomaElement s,StringBuffer b,String ident)
	{
		long id = s.getPhysical().getSoNode().getID();
		String type = s.getPropertiy("cellType");
		b.append(ident+"<cell id=\""+id+"\" type=\""+type+"\">\n");
		int dendriteC =0;
		for(NeuriteElement n :s.getNeuriteList())
		{
			
			if(n.isAnAxon())
			{
				b.append(ident+"   "+"<axon>\n");
				processNeurite(n,b,ident+"      ");
				b.append(ident+"   "+"</axon>\n");
			}
//			else
//			{
//				b.append(ident+"   "+"<dendriete id=\""+dendriteC+"\">\n");
//				processNeurite(n,b,ident+"      ");
//				b.append(ident+"   "+"</dendriete>\n");
//				dendriteC++;
//			}
			
		}
		b.append(ident+"</cell>\n");
	}
	
	
	private void processNeurite(NeuriteElement n, StringBuffer b, String ident) {
		b.append(ident+"<branch>\n");
		double [] d = n.getPhysicalCylinder().getMyOrigin();
		b.append(ident+"   "+"<pos  x=\""+d[0]+"\"  y=\""+d[1]+"\"  z=\""+d[2]+"\" ></pos>\n");
		while(n!=null )
		{
			d = n.getPhysicalCylinder().getSoNode().getPosition();
			b.append(ident+"   "+"<pos  x=\""+d[0]+"\"  y=\""+d[1]+"\"  z=\""+d[2]+"\" ></pos>\n");
			if(n.getDaughterRight()!=null)
			{
				processNeurite(n.getDaughterLeft(),b,ident+"   ");
				processNeurite(n.getDaughterRight(),b,ident +"   ");
				break;
			}
			n = n.getDaughterLeft();
			
		}
		
		
		
		b.append(ident+"</branch>\n");
	}

	public void writeToFile(String filename)
	{
		FileWriter outFile;
		try {
			outFile = new FileWriter(filename,false);
			PrintWriter p = new PrintWriter(outFile);
			p.println(b);
			p.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
