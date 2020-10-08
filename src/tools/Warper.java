package tools;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Warper extends StrokeBrush {
	
	public Warper() {
		super();
		strengthName = "Percent";
		maxStrength = 100;
		currentStrength = 25;
	}
	
	private final Random random = new Random();

	@Override
	void applyBrushToPoint(BufferedImage im, Point p, ToolParams params) {
		int rgb = im.getRGB(p.x, p.y);
		
		boolean decrease = random.nextBoolean(); // whether to decrease or increase
		double factor = 1 + Math.pow(random.nextDouble() * currentStrength / maxStrength, 1);
		if (decrease)
			factor = 1 / factor;
		
		int r = Math.min(255, (int) ((rgb >> 16 & 0xff) * factor));
		int g = Math.min(255, (int) ((rgb >> 8 & 0xff) * factor));
		int b = Math.min(255, (int) ((rgb >> 0 & 0xff) * factor));
		if (!decrease) {
			r = r == 0 ? 1 : r;
			g = g == 0 ? 1 : g;
			b = b == 0 ? 1 : b;
		}
		
		rgb = (rgb & 0xff000000) | r << 16 |  g << 8 |  b;
		im.setRGB(p.x, p.y, rgb);
	}

}
