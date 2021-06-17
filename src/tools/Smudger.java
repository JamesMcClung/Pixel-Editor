package tools;

import java.awt.Point;
import java.util.HashMap;

import canvas.Layer;
import util.Util;

public class Smudger extends OPPBrush {

	public Smudger() {
		super();
		strengthName = "Percent";
		minSize = currentSize = 2;
		maxStrength = 100;
		minStrength = 1;
		currentStrength = 50;
	}

	private final HashMap<Point, Integer> rgbs = new HashMap<>();

	@Override
	protected void drawStroke(Layer l, Point pixel, ToolParams params) {
		if (!rgbs.isEmpty()) {
			// merge colors, and save the new colors
			l.doThingInCircle(pixel, currentSize / 2d, (im, p) -> {
				Point prel = Util.difference(p, pixel);
				int rgb1 = im.getRGB(p.x, p.y);
				if (rgbs.containsKey(prel)) {
					int rgb2 = rgbs.get(prel);
					// reduce alpha of rgb2 according to current strength
					rgb2 = (rgb2 & 0x00ffffff) | (Util.a(rgb2) * currentStrength / maxStrength << 24);

					int rgb = Pencil.mixRGB(rgb1, rgb2);
					im.setRGB(p.x, p.y, rgb);
					rgbs.put(prel, rgb);
				}
			});
		} else {
			// just save the colors
			l.doThingInCircle(pixel, currentSize / 2d,
					(im, p) -> rgbs.put(Util.difference(p, pixel), im.getRGB(p.x, p.y)));
		}
	}

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		rgbs.clear();
		return super.release(l, pixel, params);
	}

}
