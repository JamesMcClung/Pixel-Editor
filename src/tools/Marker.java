package tools;

import java.awt.Point;

import canvas.Layer;
import util.Util;

public class Marker extends OPPBrush {

	@Override
	protected void drawStroke(Layer l, Point pixel, ToolParams params) {
		int maxA = currentStrength;
		int rgbNoA = params.color().getRGB() & 0x00ffffff;
		double rSquared = Math.pow(currentSize / 2d, 2) + 2d / currentSize; // last term is so that 2-diameter circle
																			// works
		l.doThingInCircle(pixel, currentSize / 2d, (im, p) -> {
			int a = (int) Math.max(0, maxA * (rSquared - Util.distSq(pixel, p)) / rSquared);
			im.setRGB(p.x, p.y, Pencil.mixRGB(im.getRGB(p.x, p.y), rgbNoA | a << 24));
		});
	}

}
