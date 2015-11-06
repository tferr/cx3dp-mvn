package ini.cx3d.utilities.dynamicLoading;
import ini.cx3d.gui.Drawer;
import ini.cx3d.utilities.HashT;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;


public class LoadDrawers {
	
	private static HashT<String,Class> classes = new HashT<String,Class>();

	public static ArrayList<Drawer> findNewDrawers() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException 
	{
		
		File directory = new File("drawer");
		if (!directory.exists()) {
			return new ArrayList<Drawer>(); 
		}
		ArrayList<Drawer> newones = new ArrayList<Drawer>();
		File[] files = directory.listFiles();
		for (File file : files) {
			
			if (file.getName().endsWith(".class")) {
				
				String name = file.getName().substring(0, file.getName().length() - 6);
				if(!classes.containsKey(name))
				{
					Class c= loadIT(name);
					Object o = c.newInstance();
					if(o instanceof Drawer)
					{
						newones.add((Drawer)o);
						classes.put(name,c);
					}
				}
			}
		}
		return newones;
	}
	
	private static Class loadIT(String name) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		URLClassLoader clazzLoader ;
		Class clazz;
		String filePath = "drawer/"+name+".class";
		ClassLoaderUtil.addFile(filePath);
		String urlz = "file:drawer/"+name+".class";
        URL url = new URL(urlz);
		clazzLoader = new URLClassLoader(new URL[]{url});
		clazz = clazzLoader.loadClass(name);
		return clazz;
	}
}
