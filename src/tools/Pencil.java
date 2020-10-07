package tools;

import java.awt.Point;

import canvas.Layer;

public class Pencil extends Brush {
	
	public void applyBrush(Layer l, Point pixel, ToolParams params) {
		l.setPixels(pixel, currentSize/2.0, params.fadedColor(currentStrength));
	}

}
