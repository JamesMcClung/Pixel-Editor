package tools;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Random;

import canvas.Layer;

public class Warper extends Brush {
	
	public Warper() {
		super();
		strengthName = "Percent";
		maxStrength = 100;
		currentStrength = 50;
	}
	
	private final Random random = new Random();
	private final Point lastPixel = new Point(-1, -1);

	@Override
	public void applyBrush(Layer l, Point pixel, ToolParams params) {
		if (!lastPixel.equals(pixel)) {
			l.doThingInCircle(pixel, currentSize/2d, (im, p) -> warp(im, p, (double) currentStrength / maxStrength));
			lastPixel.setLocation(pixel);
		}
	}
	
	private void warp(BufferedImage im, Point p, double percent) {
		int rgb = im.getRGB(p.x, p.y);
		
		boolean decrease = random.nextBoolean(); // whether to decrease or increase
		double factor = 1 + Math.pow(percent * random.nextDouble(), 1);
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
	
	

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		lastPixel.setLocation(-1, -1);
		return super.release(l, pixel, params);
	}

}
