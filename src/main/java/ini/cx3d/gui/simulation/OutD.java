package ini.cx3d.gui.simulation;

import static ini.cx3d.utilities.StringUtilities.doubleToString;
import ini.cx3d.gui.ExternalWindow;
import ini.cx3d.simulation.ECM;
import ini.cx3d.simulation.SimulationState;
import ini.cx3d.utilities.RingBuffer;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class OutD extends ExternalWindow {

	
		public static boolean print = true;//false
		public static OutD current;
		static
		{
			current = new OutD();
		}
		
		JEditorPane   shown = new JEditorPane(); 
		RingBuffer<String> b  = new RingBuffer<String>(20000);
		
		String text;
		JTextField field = new JTextField();
		
	    public OutD() {
	    	current = this;
	        initialize();
	        this.name = "Console";
	    }
	    
	    public static void print(Object o)
	    {
	    	if(!print) return;
	    	current.b.enqueue(o.toString());
	    	//current.b.append("<br>");
	    }
	    
		public static void println() {
			if(!print) return;
			current.b.enqueue("<br>");
			
		}
	    
	    public static void println(Object o)
	    {
	    	if(!print) return;
	    	current.b.enqueue("t "+
	    			doubleToString(ECM.getInstance().getECMtime(), 2)+":" +SimulationState.getLocal().stagecounter
	    			+":: "+escapeHTML(o.toString())+"<br>");
	    }
	    
	    public static void println(Object o,String color)
	    {
	    	if(!print) return;
	    	current.b.enqueue("t "+
	    			doubleToString(ECM.getInstance().getECMtime(), 2) +":" +SimulationState.getLocal().stagecounter
	    			+":: "+" <font color=\""+color+"\"> "+
			    	escapeHTML(o.toString())+
			    	"</font><br>");
	    }
	    
	    
	    private void initialize() {
	       
	        this.setTitle("show object count");
	       // this.setSize(400, 500);
	        this.setBounds(0, 0, 1000, 500);
	     
	        
	        shown.setContentType("text/html");
	        JScrollPane scrollPane = new JScrollPane(shown);
	        
	        JButton button = new JButton("Close");
	        button.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	               OutD.this.setVisible(true);//false
	               
	            }
	        });
	       
	        this.setLayout(new BorderLayout(5, 5));
	        getContentPane().add(scrollPane, BorderLayout.CENTER);
	        getContentPane().add(button, BorderLayout.NORTH);
	        getContentPane().add(field, BorderLayout.SOUTH);
	    }

		protected String find(String search) {

			search = OutD.escapeHTML(search);
			StringBuffer b2 = new StringBuffer();
			for(int i=0;i<b.size();i++)
			{
				String t = b.get(i);
				if(t==null) continue;
				if(t.contains(search))
				{
					b2.append(t);
				}
			}
			return b2.toString();
		}
		
		protected String unfiltered() {

			StringBuffer b2 = new StringBuffer();
			for(int i=0;i<b.size();i++)
			{
				String t = b.get(i);
				b2.append(t);
			}
			return b2.toString();
		}

		@Override
		public void updateWindow() {
			try{
			
			}
			catch (Exception e) {
				OutD.println("");
			}
		}
		private int max;
		private long lasttime=0;
		private long lasttime2=0;
		
		
		public  void paint(Graphics g)
		{
			try{
				super.paint(g);
				String text;
				if(field.getText().length()>3)
				{
					text = find(field.getText());
					
				}
				else
				{
					text = unfiltered();
				}
				
				this.shown.setText("<html>"+text+"<br><br>"+b.length()+"</html>");
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
	
		
		public void printout()
		{
			try{
				String text;
				if(field.getText().length()>3)
				{
					text = find(field.getText());
					
				}
				else
				{
					text = b.toString();
				}
				System.out.println(text);
			}
			catch (Exception e) {
				OutD.println("");
			}
			
		}
		
	    
		private static final String escapeHTML(String s){
			   StringBuffer sb = new StringBuffer();
			   int n = s.length();
			   for (int i = 0; i < n; i++) {
			      char c = s.charAt(i);
			      switch (c) {
			         case '<': sb.append("&lt;"); break;
			         case '>': sb.append("&gt;"); break;
			         case '&': sb.append("&amp;"); break;
			         case '"': sb.append("&quot;"); break;
//			         case '�': sb.append("&agrave;");break;
//			         case '�': sb.append("&Agrave;");break;
//			         case '�': sb.append("&acirc;");break;
//			         case '�': sb.append("&Acirc;");break;
//			         case '�': sb.append("&auml;");break;
//			         case '�': sb.append("&Auml;");break;
//			         case '�': sb.append("&aring;");break;
//			         case '�': sb.append("&Aring;");break;
//			         case '�': sb.append("&aelig;");break;
//			         case '�': sb.append("&AElig;");break;
//			         case '�': sb.append("&ccedil;");break;
//			         case '�': sb.append("&Ccedil;");break;
//			         case '�': sb.append("&eacute;");break;
//			         case '�': sb.append("&Eacute;");break;
//			         case '�': sb.append("&egrave;");break;
//			         case '�': sb.append("&Egrave;");break;
//			         case '�': sb.append("&ecirc;");break;
//			         case '�': sb.append("&Ecirc;");break;
//			         case '�': sb.append("&euml;");break;
//			         case '�': sb.append("&Euml;");break;
//			         case '�': sb.append("&iuml;");break;
//			         case '�': sb.append("&Iuml;");break;
//			         case '�': sb.append("&ocirc;");break;
//			         case '�': sb.append("&Ocirc;");break;
//			         case '�': sb.append("&ouml;");break;
//			         case '�': sb.append("&Ouml;");break;
//			         case '�': sb.append("&oslash;");break;
//			         case '�': sb.append("&Oslash;");break;
//			         case '�': sb.append("&szlig;");break;
//			         case '�': sb.append("&ugrave;");break;
//			         case '�': sb.append("&Ugrave;");break;         
//			         case '�': sb.append("&ucirc;");break;         
//			         case '�': sb.append("&Ucirc;");break;
//			         case '�': sb.append("&uuml;");break;
//			         case '�': sb.append("&Uuml;");break;
//			         case '�': sb.append("&reg;");break;         
//			         case '�': sb.append("&copy;");break;   
//			         case '�': sb.append("&euro;"); break;
			         // be carefull with this one (non-breaking whitee space)
			         case ' ': sb.append("&nbsp;");break;   
			         case '\n': sb.append("<br>");break;
			         
			         default:  sb.append(c); break;
			      }
			   }
			   return sb.toString();
			}

	


		
}


