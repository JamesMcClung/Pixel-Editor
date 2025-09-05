package tools;

import java.awt.Point;
import java.awt.image.BufferedImage;

import util.Util;

public class Pencil extends StrokeBrush {

	public static int mixRGB(int rgb1, int rgb2) {
		int a1 = Util.a(rgb1);
		int a2 = Util.a(rgb2);

		if (a2 == 0)
			return rgb1;

		int w1 = (255 - a2) * a1 / 255;

		int r = (Util.r(rgb1) * w1 + a2 * Util.r(rgb2)) / (w1 + a2);
		int g = (Util.g(rgb1) * w1 + a2 * Util.g(rgb2)) / (w1 + a2);
		int b = (Util.b(rgb1) * w1 + a2 * Util.b(rgb2)) / (w1 + a2);
		int a = a1 + a2 - (a1 * a2) / 255;

		return Util.argb(a, r, g, b);
	}

	@Override
	void applyBrushToPoint(BufferedImage im, Point p, ToolParams params) {
		int rgb = im.getRGB(p.x, p.y);
		im.setRGB(p.x, p.y, mixRGB(rgb, params.color().getRGB() & 0x00ffffff | currentStrength << 24));
	}

}
