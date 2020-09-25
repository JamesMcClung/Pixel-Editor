package app;

import java.awt.Point;
import java.awt.image.BufferedImage;

import canvas.Layer;
import canvas.Spritesheet;

@SuppressWarnings("preview")
public record SaveableState(Layer[] layers, BufferedImage[] images, Spritesheet ss, Point spriteIndex, boolean isTransient) { 
	
	public SaveableState(Layer[] layers, BufferedImage[] images, Spritesheet ss, Point spriteIndex) {
		this(layers, images, ss, spriteIndex, false);
	}
	
}