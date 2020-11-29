package tools;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

import canvas.Layer;

public class Smoother extends Brush {
	
	private final Point lastPoint = new Point(-1, -1);
	
	public Smoother() {
		super();
		strengthName = "Percent";
		minSize = currentSize = 2;
		maxStrength = 100;
		minStrength = 1;
		currentStrength = 50;
	}
	

	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		lastPoint.setLocation(-1, -1); // to ensure that it is drawn on
		return super.press(l, pixel, params);
	}

	@Override
	protected void applyBrush(Layer l, Point pixel, ToolParams params) {
		if (lastPoint.equals(pixel)) {
			return;
		}
		lastPoint.setLocation(pixel);
		Color c = Eyedropper.getAverageColor(l, pixel, currentSize);
		l.doThingInCircle(pixel, currentSize / 2d, (im, p) -> bringToColor(im, p, c));
	}
	
	private void bringToColor(BufferedImage im, Point p, Color c) {
		float w = (float) currentStrength / maxStrength;
		int rgb = im.getRGB(p.x, p.y);
		int a = (rgb >>> 24 & 0xff); // it is annoying when alpha is mixed
		int r = Math.round((rgb >>> 16 & 0xff) * (1-w) + c.getRed() * w);
		int g = Math.round((rgb >>> 8 & 0xff) * (1-w) + c.getGreen() * w);
		int b = Math.round((rgb >>> 0 & 0xff) * (1-w) + c.getBlue() * w);
		im.setRGB(p.x, p.y, a << 24 | r << 16 | g << 8 | b);
	}

}
