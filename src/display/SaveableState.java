package display;

import java.awt.image.BufferedImage;

import canvas.Layer;

@SuppressWarnings("preview")
public record SaveableState(Layer[] layers, BufferedImage[] images) { }