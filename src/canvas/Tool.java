package canvas;

import java.awt.Color;
import java.awt.Point;

public interface Tool {
	
	public static final int DO_NOTHING = 0, REPAINT = 1, SAVE_STATE = 2, REPAINT_AND_SAVE = 3;
	
	int click(Layer l, Point pixel, ToolParams params);
	int press(Layer l, Point pixel, ToolParams params);
	int release(Layer l, Point pixel, ToolParams params);
	int drag(Layer l, Point pixel, ToolParams params);
	int move(Layer l, Point pixel, ToolParams params);
	
	public static Color getFadedColor(Color c, int alpha) {
		// assumes given color starts with alpha=255
		return new Color(c.getRGB() & (alpha << 24 | 0xffffff), true);
	}
}
