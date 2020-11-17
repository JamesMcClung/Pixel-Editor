package canvas;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;

import util.Util;

public class PixelMask implements BitMask {
	
	@FunctionalInterface
	public static interface Condition {
		boolean accept(Point pixel, int rgb);
	}
	@FunctionalInterface
	public static interface Action {
		/**
		 * Does something at a point.
		 * @param pixel location in image
		 */
		void doAction(Point pixel);
	}
	
	public PixelMask(BufferedImage im, Condition c) {
		this(im.getWidth(), im.getHeight());
		match(im, new Point(), c);
	}

	public PixelMask(int width, int height) {
		mask = new boolean[height][width];
		this.width = width;
		this.height = height;
	}
	
	public PixelMask(Dimension d) {
		this(d.width, d.height);
	}
	
	private final boolean[][] mask;
	public final int width, height;
	
	
	public void match(BufferedImage image, Point x0, Condition c) {
		Point p = new Point();
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				p.setLocation(x0.x + i, x0.y + j);
				mask[j][i] = c.accept(p, image.getRGB(p.x, p.y));
			}
		}
	}
	
	/**
	 * Performs the given action everywhere that this mask is true.
	 * (The action is passed Points where the mask is true, offset by x0.)
	 * @param offset added to each index before performing action there
	 * @param action an action
	 */
	public void doAction(Point offset, Action action) {
		Point pixel = new Point();
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				if (mask[j][i]) {
					pixel.setLocation(offset.x + i, offset.y + j);
					action.doAction(pixel);
				}
			}
		}
	}
	
	
	// not sure if this works properly
	@FunctionalInterface
	public static interface Merger {
		boolean merge(boolean b1, boolean b2);
	}
	public static PixelMask or(PixelMask mask1, PixelMask mask2, Point offset2) {
		return merge(mask1, mask2, offset2, (b1, b2) -> b1 || b2);
	}
	public static PixelMask and(PixelMask mask1, PixelMask mask2, Point offset2) {
		return merge(mask1, mask2, offset2, (b1, b2) -> b1 && b2);
	}
	public static PixelMask merge(PixelMask mask1, PixelMask mask2, Point offset2, Merger merger) {
		Point offset1 = Util.times(Util.min(new Point(), offset2), -1);
		offset2 = Util.max(new Point(), offset2);
		
		int width = Math.max(mask1.width + offset1.x, mask2.width + offset2.x);
		int height = Math.max(mask1.height + offset1.y, mask2.height + offset2.y);
		PixelMask merged = new PixelMask(width, height);
		
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				merged.set(i, j, merger.merge(mask1.get(i - offset1.x, j - offset1.y), mask2.get(i - offset2.x, j - offset2.y)));
		
		return merged;
	}
	
	
	// getters and setters
	
	@Override
	public boolean get(int x, int y) {
		return !isOutOfBounds(x, y) && mask[y][x];
	}
	public void set(Point p, boolean b) {
		set(p.x, p.y, b);
	}
	public void set(int x, int y, boolean b) {
		if (!isOutOfBounds(x, y))
			mask[y][x] = b;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	@Override
	public int getHeight() {
		return height;
	}
	
	public boolean isOutOfBounds(int x, int y) {
		return x < 0 || y  < 0 || x >= width || y >= height;
	}

	
	// other operations
	/**
	 * Flips every bit.
	 * @return this mask
	 */
	public PixelMask invert() {
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				mask[j][i] = !mask[j][i];
		return this;
	}
}
