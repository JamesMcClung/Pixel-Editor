package tools;

import java.awt.Graphics2D;
import java.awt.Point;

import canvas.Layer;
import util.Util;

public class Marker implements Tool {
	
	public static void draw(Layer l, Point pixel, ToolParams params) {
		Graphics2D g = l.getGraphics();
		Util.enableAntiAliasing(g);
		g.setColor(Tool.getFadedColor(params.color(), params.alpha()));
		int x = pixel.x - params.size() / 2;
		int y = pixel.y - params.size() / 2;
		g.fillOval(x, y, params.size(), params.size());
		g.dispose();
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
