package tools;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import app.App;
import canvas.Layer;

public abstract class Tool {
	
	public static final int DO_NOTHING = 0, REPAINT = 1, SAVE_STATE = 2, REPAINT_AND_SAVE = 3;
	
	protected Tool() { 
		strengthName = "Alpha";
		minStrength = 0;
		maxStrength = 255;
		currentStrength = 255;
		hasStrength = true;
		enableStrength = true;
		
		sizeName = "Diameter";
		minSize = 1;
		maxSize = 64;
		currentSize = 1;
		hasSize = true;
		enableSize = true;
	}
	
	public String strengthName;
	public int minStrength, maxStrength, currentStrength;
	public boolean hasStrength, enableStrength;
	
	public String sizeName;
	public int minSize, maxSize, currentSize;
	public boolean hasSize, enableSize;
	
	
	public ToolResult click(Layer l, Point pixel, ToolParams params) {
		return null;
	}
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		return null;
	}
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		return null;
	}
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		return null;
	}
	public ToolResult move(Layer l, Point pixel, ToolParams params) {
		return null;
	}
	public ToolResult enter(Layer l, Point pixel, ToolParams params) {
		return null;
	}
	public ToolResult exit(Layer l, Point pixel, ToolParams params) {
		return null;
	}
	
	public String getStrengthName() {
		if (!hasStrength)
			return null;
		return strengthName;
	}
	public boolean hasAlpha() {
		return getStrengthName() == "Alpha";
	}
	
	
	
	public static Color getFadedColor(Color c, int alpha) {
		// assumes given color starts with alpha=255
		return new Color(c.getRGB() & (alpha << 24 | 0xffffff), true);
	}
	
	@SuppressWarnings("preview")
	public static record ToolResult(int command) { }
	
	@SuppressWarnings("preview")
	public static record ToolParams(Color color, App app, MouseEvent e) {
		public Color fadedColor(int strength) {
			return getFadedColor(color, strength);
		}
	}
}
