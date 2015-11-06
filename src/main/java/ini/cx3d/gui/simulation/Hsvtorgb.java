package ini.cx3d.gui.simulation;

public class Hsvtorgb {

	/**
	 * Coloring RGB to HSV
	 */
	public static float[] HSV2RGB(float hue, float saturation, float value) {
        int h = (int) Math.floor(hue / 60.0f);
        float f = (hue/60.0f - h);
        float p = value*(1-saturation);
        float q = value*(1-saturation*f);
        float t = value*(1-saturation*(1-f));
        
        float[] tmp = new float[3];
        switch (h) {
            case 0: tmp[0]=value; tmp[1]=t; tmp[2]=p; break;
            case 1: tmp[0]=q; tmp[1]=value; tmp[2]=p; break;
            case 2: tmp[0]=p; tmp[1]=value; tmp[2]=t; break;
            case 3: tmp[0]=p; tmp[1]=q; tmp[2]=value; break;
            case 4: tmp[0]=t; tmp[1]=p; tmp[2]=value; break;
            case 5: tmp[0]=value; tmp[1]=p; tmp[2]=q; break;
            case 6: tmp[0]=value; tmp[1]=t; tmp[2]=p; break;
        }

    return tmp;
    }

	}


