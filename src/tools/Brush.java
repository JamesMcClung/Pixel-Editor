package tools;

import java.awt.Point;

import canvas.Layer;

public abstract class Brush extends CircleTool {
	
	/**
	 * Called when a pixel is pressed or dragged over.
	 * @param l layer of pixel
	 * @param pixel pixel
	 * @param params params
	 */
	protected abstract void applyBrush(Layer l, Point pixel, ToolParams params);

	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		applyBrush(l, pixel, params);
		return new ToolResult(REPAINT);
	}

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		return new ToolResult(SAVE_STATE);
	}

	@Override
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		super.drag(l, pixel, params);
		applyBrush(l, pixel, params);
		return new ToolResult(REPAINT);
	}
	

}
