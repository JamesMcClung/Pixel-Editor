package tools;

import java.awt.Graphics2D;
import java.awt.Point;

import canvas.Layer;
import util.Util;

public class Marker extends Brush {
	
	public void applyBrush(Layer l, Point pixel, ToolParams params) {
		Graphics2D g = l.getGraphics();
		Util.enableAntiAliasing(g);
		g.setColor(Tool.getFadedColor(params.color(), currentStrength));
		int x = pixel.x - currentSize / 2;
		int y = pixel.y - currentSize / 2;
		g.fillOval(x, y, currentSize, currentSize);
		g.dispose();
	}
}
