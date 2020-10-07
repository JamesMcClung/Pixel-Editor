package tools;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

import canvas.Layer;

public class HueChanger extends Brush {
	
	public HueChanger() {
		super();
		hasStrength = false;
	}
	
	public void applyBrush(Layer l, Point pixel, ToolParams params) {
		l.doThingInCircle(pixel, currentSize / 2d, (im, p) -> changeHueHelper(im, p, params.color()));
	}
	
	public void changeHueHelper(BufferedImage im, Point p, Color c) {
		int imrgb = im.getRGB(p.x, p.y);
		int imr = imrgb >> 16 & 0xff;
		int img = imrgb >> 8 & 0xff;
		int imb = imrgb & 0xff;
		
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
