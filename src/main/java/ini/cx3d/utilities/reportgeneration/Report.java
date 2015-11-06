package ini.cx3d.utilities.reportgeneration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

public class Report {

	private String reportfile;
	private DocumentPart header;
	private DocumentPart footer;
	private ArrayList<DocumentPart> mainparts = new ArrayList<DocumentPart>();
	
	public Report(String reportfile)
	{
		this.reportfile = reportfile;
	}
	
	
	public void addMainPart(DocumentPart mainpart)
	{
		mainparts.add(mainpart);
	}

	public void setHeader(DocumentPart header) {
		this.header = header;
	}

	public DocumentPart getHeader() {
		return header;
	}

	public void setFooter(DocumentPart footer) {
		this.footer = footer;
	}

	public DocumentPart getFooter() {
		return footer;
	}
	
	public void GenerateCurrentReprt()
	{
		File mfile = new File(reportfile);
		try{
			mfile.delete();
		}
		catch (Exception e) {
			
		}
		PrintStream out=System.out;
		try {
		 	out = new PrintStream(mfile);
		 	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		header.report(out);
		for (DocumentPart p : mainparts) {
			p.report(out);
		}
		footer.report(out);
		out.close();
	}
	
}
