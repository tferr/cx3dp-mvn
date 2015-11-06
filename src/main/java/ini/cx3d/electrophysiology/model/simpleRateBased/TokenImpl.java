package ini.cx3d.electrophysiology.model.simpleRateBased;

import ini.cx3d.electrophysiology.model.Token;

import java.awt.Color;

public class TokenImpl implements Token{
	public double rateValue;

	@Override
	public Color getColor() {
		int i = (int)(rateValue/250*255);
		return new Color(0,0,255,i);
	}
}
