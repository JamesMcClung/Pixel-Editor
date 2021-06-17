package tools;

import java.awt.Point;
import java.awt.image.BufferedImage;

import util.Util;

public class Shader extends StrokeBrush {
	
	public Shader(int targetShade) {
		super();
		this.targetShade = targetShade;
		
		strengthName = "Percent";
		maxStrength = 100;
		minStrength = 1;
		currentStrength = 25;
	}
	
	private final int targetShade;

	@Override
	void applyBrushToPoint(BufferedImage im, Point p, ToolParams params) {
		int rgb = im.getRGB(p.x, p.y);
		int oldr = Util.r(rgb), oldg = Util.g(rgb), oldb = Util.b(rgb);
		float oldShade = Math.max((oldr + oldg + oldb) / 3f, .125f);
		
		float w = (float) currentStrength / maxStrength; 
		int newShade = Math.round(targetShade * w + oldShade * (1-w));
		
		int r = roundNBound(oldr * newShade / oldShade);
		int g = roundNBound(oldg * newShade / oldShade);
		int b = roundNBound(oldb * newShade / oldShade);
		
		im.setRGB(p.x, p.y, Util.argb(Util.a(rgb), r, g, b));
	}
	
	private static int roundNBound(float c) {
		return Math.max(0, Math.min(255, Math.round(c)));
	}

}
