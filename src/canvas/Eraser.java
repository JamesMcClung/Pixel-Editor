package canvas;

import java.awt.Color;
import java.awt.Point;

public class Eraser implements Tool {
	
	public static final Color eraseColor = new Color(255, 255, 255, 0); 
	
	public static void erasePixel(Layer l, Point pixel) {
		l.setPixel(pixel, eraseColor);
	}

	@Override
	public int click(Layer l, Point pixel, ToolParams params) {
		erasePixel(l, pixel);
		return REPAINT;
	}

	@Override
	public int press(Layer l, Point pixel, ToolParams params) {
		erasePixel(l, pixel);
		return REPAINT;
	}

	@Override
	public int release(Layer l, Point pixel, ToolParams params) {
		return SAVE_STATE;
	}

	@Override
	public int drag(Layer l, Point pixel, ToolParams params) {
		erasePixel(l, pixel);
		return REPAINT;
	}

	@Override
	public int move(Layer l, Point pixel, ToolParams params) {
		return DO_NOTHING;
	}

}
