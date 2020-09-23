package display;

import java.awt.Point;
import java.awt.image.BufferedImage;

import canvas.Layer;
import canvas.Spritesheet;

@SuppressWarnings("preview")
public record SaveableState(Layer[] layers, BufferedImage[] images, Spritesheet ss, Point spriteIndex) { }