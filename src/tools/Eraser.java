package tools;

import java.awt.Point;
import java.awt.image.BufferedImage;

import util.Util;

public class Eraser extends StrokeBrush {
	
	public Eraser() {
		super();
		strengthName = "Percent";
		maxStrength = currentStrength = 100;
	}
	
	@Override
	void applyBrushToPoint(BufferedImage im, Point p, ToolParams params) {
		int rgb = im.getRGB(p.x, p.y);
		int a = Util.a(rgb) * (maxStrength - currentStrength )/ maxStrength;
		im.setRGB(p.x, p.y, rgb & 0x00ffffff | a << 24);
	}

}
