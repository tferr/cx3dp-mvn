package ini.cx3d.utilities.reportgeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

public class DocumentPart {

	private String templatefile;
	private HashMap<String, String> replacings = new HashMap<String, String>();
	
	public DocumentPart(String templatefile)
	{
		this.templatefile = templatefile;
	}
	
	public void putReplacings(String replaceable,String replacing)
	{
		replacings.put(replaceable, replacing);
	}
	
	public void report(PrintStream out) {

	    
	    File file = new File(templatefile);
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = null;
 
        try
        {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
 
            // repeat until all lines is read
            while ((text = reader.readLine()) != null)
            {
                contents.append(text).append(System.getProperty("line.separator"));
            }
            text =contents.toString(); 
            for (String rep : replacings.keySet()) {
            	text = text.replace(rep,replacings.get(rep));
	    	}
            out.println(text);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
		
	}

	public void setTemplatefile(String templatefile) {
		this.templatefile = templatefile;
	}

	public String getTemplatefile() {
		return templatefile;
	}

}
