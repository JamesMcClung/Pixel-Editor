package canvas;

import java.awt.Graphics2D;
import java.awt.Point;

import util.Util;

public class Marker implements Tool {
	
	public static void draw(Layer l, Point pixel, ToolParams params) {
		Graphics2D g = l.getGraphics();
		Util.enableAntiAliasing(g);
		g.setColor(Tool.getFadedColor(params.color, params.alpha));
		int x = pixel.x - params.size / 2;
		int y = pixel.y - params.size / 2;
		g.fillOval(x, y, params.size, params.size);
		g.dispose();
	}

	@Override
	public int click(Layer l, Point pixel, ToolParams params) {
		draw(l, pixel, params);
		return REPAINT;
	}

	@Override
	public int press(Layer l, Point pixel, ToolParams params) {
		draw(l, pixel, params);
		return REPAINT;
	}

	@Override
	public int release(Layer l, Point pixel, ToolParams params) {
		return SAVE_STATE;
	}

	@Override
	public int drag(Layer l, Point pixel, ToolParams params) {
		draw(l, pixel, params);
		return REPAINT;
	}

	@Override
	public int move(Layer l, Point pixel, ToolParams params) {
		return DO_NOTHING;
	}
	
	


}
