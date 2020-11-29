package tools;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;

import canvas.Layer;

public abstract class StrokeBrush extends Brush {
	
	private final HashSet<Point> pixelsInStroke = new HashSet<>(); // so same pixels are not doubly affected
	
	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		pixelsInStroke.clear();
		return super.press(l, pixel, params);
	}

	@Override
	protected void applyBrush(Layer l, Point pixel, ToolParams params) {
		l.doThingInCircle(pixel, currentSize / 2d, (im, p) -> {
			if (pixelsInStroke.add(new Point(p)))
				applyBrushToPoint(im, p, params);
		});
	}
	
	abstract void applyBrushToPoint(BufferedImage im, Point p, ToolParams params);
}
