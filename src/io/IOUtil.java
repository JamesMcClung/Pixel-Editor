package io;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import canvas.Layer;
import canvas.Spritesheet;

public class IOUtil {
	
	public static final String[] VALID_EXTENSIONS = {"png", "gif", "jpg", "jpeg"};
	public static boolean isValidExtension(String ext) {
		for (String s : VALID_EXTENSIONS)
			if (s.equals(ext))
				return true;
		return false;
	}
	
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
	
	public static boolean saveSpritesheet(Spritesheet s) {
		return saveLayerAs(s, s.getFile());
	}
	
	@SuppressWarnings("preview")
	public static boolean saveLayerAs(Layer s, File file) {
		try {
			String name = file.getName();
			String ext = name.substring(name.lastIndexOf('.') + 1);
			if (!isValidExtension(ext))
				throw new RuntimeException("Could not save file as ." + ext);
			
			BufferedImage pic = s.getImage();
			
			// remove alpha channel for jpgs, gifs
			if (!ext.equals("png")) {
				var temp = new BufferedImage(pic.getWidth(),  pic.getHeight(), BufferedImage.TYPE_INT_RGB);
				var g = temp.createGraphics();
				g.drawImage(pic, 0, 0, null);
				g.dispose();
				pic = temp;
			}
			
			if (ImageIO.write(pic, ext, file)) {
				if (s instanceof Spritesheet ss)
					ss.setFile(file);
				return true;
			}
			throw new RuntimeException("Could not save to file: " + file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Saves the given spritesheet to the given file as an animated gif.
	 * Each nonempty sprite is added the gif in order.
	 * @param ss the spritesheet
	 * @param file the file
	 * @param delay time in milliseconds between frames
	 */
	public static void saveImagesAsGIF(List<BufferedImage> images, File file, int delay) {
		var encoder = new AnimatedGifEncoder();
		encoder.start(file.getAbsolutePath());
		encoder.setRepeat(0); // play indefinitely
		encoder.setDelay(delay);
		for (var im : images)
			encoder.addFrame(im);
		encoder.finish();
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
