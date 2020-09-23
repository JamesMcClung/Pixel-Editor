package canvas;

import java.awt.Point;

public class Pencil implements Tool {
	
	public static void draw(Layer l, Point pixel, ToolParams params) {
		l.setPixels(pixel, params.size()/2.0, params.color());
	}

	@Override
	public ToolResult click(Layer l, Point pixel, ToolParams params) {
		return null;
	}

	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		draw(l, pixel, params);
		return new ToolResult(REPAINT);
	}

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		return new ToolResult(SAVE_STATE);
	}

	@Override
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		draw(l, pixel, params);
		return new ToolResult(REPAINT);
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
