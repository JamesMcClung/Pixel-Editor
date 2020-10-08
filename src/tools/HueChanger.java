package tools;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class HueChanger extends StrokeBrush {
	
	public HueChanger() {
		super();
		hasStrength = false;
	}

	@Override
	void applyBrushToPoint(BufferedImage im, Point p, ToolParams params) {
		int imrgb = im.getRGB(p.x, p.y);
		int imr = imrgb >> 16 & 0xff;
		int img = imrgb >> 8 & 0xff;
		int imb = imrgb & 0xff;
		
		var c = params.color();
		int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
		
		double immax = Math.max(imr, Math.max(img, imb));
		double max = Math.max(r, Math.max(g, b));
		
		double scale = max == 0 ? 0 : immax / max;
		
		imr = (int) (r * scale);
		img = (int) (g * scale);
		imb = (int) (b * scale);
		
		imrgb = imrgb & 0xff000000 | imr << 16 | img <<  8 | imb;
		
		im.setRGB(p.x, p.y, imrgb);
	}
	
	
}
