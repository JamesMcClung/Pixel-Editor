package canvas;

import java.awt.Point;

public class Pencil implements Tool {
	
	public static void draw(Layer l, Point pixel, ToolParams params) {
		l.setPixel(pixel, Tool.getFadedColor(params.color, params.alpha));
		// TODO adjustable size
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
