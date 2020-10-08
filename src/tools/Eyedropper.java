package tools;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import canvas.Layer;

public class Eyedropper extends CircleTool {
	
	public Eyedropper() {
		super();
		enableStrength = false;
	}
	
	/**
	 * Calculates the average of the colors in a circle, weighted by alpha.
	 * @param l layer of colors
	 * @param pixel center of circle
	 * @param diameter diameter of circle
	 * @return average color
	 */
	public static Color getAverageColor(Layer l, Point pixel, int diameter) {
		ArrayList<Color> colors = new ArrayList<>(diameter*diameter);
		l.doThingInCircle(pixel, diameter/2d, (im, pix) -> colors.add(new Color(im.getRGB(pix.x, pix.y), true)));
		int rSum = 0, gSum = 0, bSum = 0, aSum = 0;
		for (Color c : colors) {
			rSum += c.getRed() * c.getAlpha();
			gSum += c.getGreen() * c.getAlpha();
			bSum += c.getBlue() * c.getAlpha();
			aSum += c.getAlpha();
		}
		
		int divisor = aSum;
		if (divisor == 0)
			return Layer.ERASE_COLOR;
		return new Color(rSum / divisor, gSum / divisor, bSum / divisor, aSum / colors.size());
	}
	
	private void pickColor(Layer l, Point pixel, ToolParams params) {
		Color c = getAverageColor(l, pixel, currentSize);
		params.app().colorPanel.setCurrentColor(c);
		params.app().toolPanel.setStrength(c.getAlpha());
		setRealAlpha(c.getAlpha());
		params.app().toolPanel.setNextAlpha(realAlpha);
		realPreviewColor = c;
	}
	
	/**
	 * Sets the preview color in the colormaker to the given color.
	 * @param l layer of color
	 * @param pixel pixel of color
	 * @param params params
	 */
	private void previewColor(Layer l, Point pixel, ToolParams params) {
		Color c = getAverageColor(l, pixel, currentSize);
		params.app().colorPanel.setPreviewColor(c);
		params.app().toolPanel.setStrength(c.getAlpha());
	}

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		pickColor(l, pixel, params);
		return super.release(l, pixel, params);
	}

	@Override
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		previewColor(l, pixel, params);
		return super.drag(l, pixel, params);
	}

	@Override
	public ToolResult move(Layer l, Point pixel, ToolParams params) {
		previewColor(l, pixel, params);
		return super.move(l, pixel, params);
	}
	
	private Color realPreviewColor = null;
	private int realAlpha;

	@Override
	public ToolResult exit(Layer l, Point pixel, ToolParams params) {
		params.app().colorPanel.setPreviewColor(realPreviewColor);
		params.app().toolPanel.setStrength(realAlpha);
		return super.exit(l, pixel, params);
	}
	
	@Override
	public ToolResult enter(Layer l, Point p, ToolParams params) {
		return super.enter(l, p, params);
	}
	
	/**
	 * Sets the "real" alpha and preview colors. These are the values that carry over to the next tool, if applicable.
	 * @param alpha real alpha
	 * @param c real preview color
	 */
	public void initialize(int alpha, Color c) {
		realPreviewColor = c;
		setRealAlpha(alpha);
	}
	
	private void setRealAlpha(int a) {
		realAlpha = a;
	}
}
