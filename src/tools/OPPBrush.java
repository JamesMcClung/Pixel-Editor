package tools;

import java.awt.Point;

import canvas.Layer;

/**
 * "Once Per Pixel Brush", for brushes that are applied only once each time the cursor enters a pixel
 */
public abstract class OPPBrush extends Brush {

	private final Point lastPoint = new Point(-1, -1);

	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		lastPoint.setLocation(-1, -1); // to ensure that it is drawn on
		return super.press(l, pixel, params);
	}

	@Override
	protected void applyBrush(Layer l, Point pixel, ToolParams params) {
		if (!lastPoint.equals(pixel)) {
			lastPoint.setLocation(pixel);
			drawStroke(l, pixel, params);
		}
	}

	/**
	 * The method called on a pixel that is ok to draw on.
	 * 
	 * @param l      layer to draw on
	 * @param pixel  pixel to draw at
	 * @param params params
	 */
	abstract protected void drawStroke(Layer l, Point pixel, ToolParams params);

}
