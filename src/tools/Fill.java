package tools;

import java.awt.Color;
import java.awt.Point;

import canvas.Layer;

public class Fill extends Tool {

	public Fill() {
		super();
		// hasStrength = false;
		sizeName = "Search Diameter";
		minSize = currentSize = 2;
	}

	public void fill(Layer l, Point pixel, Color color) {
		l.setPixels(new Point(), l.getMonochromeRegion(pixel, Math.max(1, currentSize / 2d), null), color);
	}

	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		fill(l, pixel, params.fadedColor(currentStrength));
		return new ToolResult(Tool.REPAINT);
	}

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		return new ToolResult(SAVE_STATE);
	}

	@Override
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		fill(l, pixel, params.fadedColor(currentStrength));
		return new ToolResult(Tool.REPAINT);
	}

}
