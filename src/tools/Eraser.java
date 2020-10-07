package tools;

import java.awt.Point;

import canvas.Layer;

public class Eraser extends Brush {
	
	@Override
	public void applyBrush(Layer l, Point pixel, ToolParams params) {
		l.setPixels(pixel, currentSize/2.0, Layer.ERASE_COLOR);
	}

}
