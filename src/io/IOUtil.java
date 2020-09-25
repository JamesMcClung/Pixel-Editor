package io;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import canvas.Layer;
import canvas.Spritesheet;

public class IOUtil {
	
	public static Layer loadLayer(String path) {
		File file = new File(path);
		try {
			return new Layer(ImageIO.read(file));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Spritesheet loadSpritesheat(File file) {
		try {
			return new Spritesheet(file, ImageIO.read(file));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void saveSpritesheet(Spritesheet s) {
		saveSpritesheetAs(s, s.getFile());
	}
	
	public static void saveSpritesheetAs(Spritesheet s, File file) {
		try {
			ImageIO.write(s.getImage(), "png", file);
			s.setFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Adds a transparent pixel in the bottom-right corner encoding the given dimensions.
	 * Specifically, alpha is 0, the first 12 bits store height, and the next 12 bits store width.
	 * @param image a spritesheet image
	 * @param spriteDim dimension of sprites, in pixels
	 */
	public static void addSpriteDimSignature(BufferedImage image, Dimension spriteDim) {
		int rgb = (spriteDim.width << 12) + spriteDim.height; 
		image.setRGB(image.getWidth() - 1, image.getHeight() - 1, rgb);
	}
	
	/**
	 * Reads the hidden information encoded in the bottom-right corner of the spritesheet image. Returns null if no such information exists. 
	 * @param image a spritesheet image
	 * @return the Dimension of the spritesheet's sprites, in pixels
	 */
	public static Dimension readSpriteDimSignature(BufferedImage image) {
		int rgb = image.getRGB(image.getWidth() - 1, image.getHeight() - 1);
		int alpha  = rgb & 0xff000000;
		int width = (rgb & 0x00fff000) >> 12;
		int height = rgb & 0x00000fff;
		if (alpha != 0 || width == 0 || height == 0)
			return null;
		return new Dimension(width, height);
	}
}
