package ini.cx3d.utilities;

import ini.cx3d.simulation.ECM;

import java.awt.Color;

public class SomeColors {
	
	
	public static Color LILAC = new Color(191,179,254);
	public static Color MAUVE = new Color(220,176,254);
	public static Color LIGHT_PINK = new Color(254,176,254);
	public static Color LIGHT_YELLOW = new Color(254,254,134);
	public static Color LIGHT_GREEN = new Color(142,252,134);
	
	public static Color GREY_1 = new Color(221,225,217);
	public static Color GREY_2 = new Color(176,179,172);
	public static Color GREY_3 = new Color(136,138,133);
	
	public static Color RED_1 = new Color(255,180,203);
	public static Color RED_2 = new Color(255,128,167);
	
	public static Color YELLOW_1 = new Color(255,255,107);
	
	public static Color GREEN_1 = new Color(177,255,177);
	
	
	public static Color getRandomColor(){
		return new Color((float) ECM.getRandomDouble(),(float)ECM.getRandomDouble(),(float) ECM.getRandomDouble(),0.1f);
	}
	
	private static Color[] colorField;
	public static void initColorField()
	{
		colorField = new Color[18];
		int i =0;
		colorField[i++] = new Color(191,179,254);
		colorField[i++] = new Color(220,176,254);
		colorField[i++] = new Color(254,176,254);
		colorField[i++] = new Color(254,254,134);
		colorField[i++] = new Color(142,252,134);
		colorField[i++] = new Color(221,225,217);
		colorField[i++] = new Color(176,179,172);
		colorField[i++] =new Color(136,138,133);
		colorField[i++] = new Color(255,180,203);
		colorField[i++] = new Color(255,128,167);
		colorField[i++] = new Color(255,255,107);
		colorField[i++] = new Color(177,255,177);
		colorField[i++] = Color.ORANGE;
		colorField[i++] = Color.black;
		colorField[i++] = Color.CYAN;
		colorField[i++] = Color.pink;
		colorField[i++] = Color.green;
		colorField[i++] = Color.MAGENTA;
							
	}
	
	public static Color getColorAssociated(Object o)
	{
		int i = Math.abs( o.hashCode());
		return colorField[i%colorField.length];
	}
	
}
