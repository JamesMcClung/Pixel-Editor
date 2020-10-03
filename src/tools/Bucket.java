package tools;

import java.awt.Point;

import canvas.Layer;

public class Bucket implements Tool {
	
	public static void fill(Layer l, Point pixel, ToolParams params) {
		l.setPixels(new Point(), l.getMonochromeRegion(pixel, Math.max(1, params.size()/2d), null), params.fadedColor());
	}

	@Override
	public ToolResult click(Layer l, Point pixel, ToolParams params) {
		return null;
	}

	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		fill(l, pixel, params);
		return new ToolResult(Tool.REPAINT);
	}

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		return new ToolResult(SAVE_STATE);
	}

	@Override
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		fill(l, pixel, params);
		return new ToolResult(Tool.REPAINT);
	}

	@Override
	public ToolResult move(Layer l, Point pixel, ToolParams params) {
		return null;
	}

	@Override
	public ToolResult enter(Layer l, Point pixel, ToolParams params) {
		return null;
	}

	@Override
	public ToolResult exit(Layer l, Point pixel, ToolParams params) {
		return null;
	}

}
