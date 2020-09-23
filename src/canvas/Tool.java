package canvas;

import java.awt.Color;
import java.awt.Point;

import app.App;

public interface Tool {
	
	public static final int DO_NOTHING = 0, REPAINT = 1, SAVE_STATE = 2, REPAINT_AND_SAVE = 3;
	
	ToolResult click(Layer l, Point pixel, ToolParams params);
	ToolResult press(Layer l, Point pixel, ToolParams params);
	ToolResult release(Layer l, Point pixel, ToolParams params);
	ToolResult drag(Layer l, Point pixel, ToolParams params);
	ToolResult move(Layer l, Point pixel, ToolParams params);
	ToolResult enter(Layer l, Point pixel, ToolParams params);
	ToolResult exit(Layer l, Point pixel, ToolParams params);
	
	public static Color getFadedColor(Color c, int alpha) {
		// assumes given color starts with alpha=255
		return new Color(c.getRGB() & (alpha << 24 | 0xffffff), true);
	}
	
	@SuppressWarnings("preview")
	public static record ToolResult(int command) { }
	
	@SuppressWarnings("preview")
	public static record ToolParams(Color color, int alpha, int size, App app) { }
}
