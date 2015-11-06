package ini.cx3d.electrophysiology.model.simpleSpikeBased;

import ini.cx3d.electrophysiology.model.Token;

import java.awt.Color;

public class TokenImpl implements Token{
	public double voltage;
	@Override
	public Color getColor() {
		return new Color(0,0,255,255);
	}
}
