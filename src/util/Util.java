package util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Util {
	
	/**
	 * Returns a/b, rounded down to the nearest integer.
	 * @param a numerator
	 * @param b denominator
	 * @return result of division
	 */
	public static int divideFloor(int a, int b) {
		int d = a/b;
		if (a != 0 && a < 0 ^ b < 0)
			return d - 1;
		return d;
	}
	
	/**
	 * Returns a % b such that the result is positive.
	 * @param a 
	 * @param b 
	 * @return result of modulus
	 */
	public static int modPositive(int a, int b) {
		int m = a % b;
		if (m < 0)
			return b + m;
		return m;
	}
	
	public static Point elementwiseProd(Point a, Point b) {
		return new Point(a.x * b.x, a.y * b.y);
	}
	public static Point elementwiseProd(Point a, Dimension b) {
		return new Point(a.x * b.width, a.y * b.height);
	}
	public static Dimension elementwiseProd(Dimension a, Dimension b) {
		return new Dimension(a.width * b.width, a.height * b.height);
	}
	public static Dimension scale(Dimension d, double scale) {
		d.width = (int) (d.width * scale);
		d.height = (int) (d.height * scale);
		return d;
	}

	// color used for background tiling
	public static Color tileColor1 = Color.WHITE, tileColor2 = new Color(220, 220, 220);
	/**
	 * Creates a texture for the background. The texture is like a chess board, with tiles of the given dimension.
	 * @param tileSize dimension of tile, in pixels
	 * @return a texure that can be applied using {@link Graphics2D#setPaint(java.awt.Paint)} and then {@link Graphics2D#fill(java.awt.Shape)}.
	 */
	public static TexturePaint getPaint(Dimension tileSize) {
		BufferedImage sample = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
		sample.setRGB(0, 0, tileColor1.getRGB());
		sample.setRGB(1, 1, tileColor1.getRGB());
		sample.setRGB(0, 1, tileColor2.getRGB());
		sample.setRGB(1, 0, tileColor2.getRGB());
		return new TexturePaint(sample, new Rectangle(scale(tileSize, 2)));
	}
	

	/**
	 * Sets the label's preferred width to the maximum possible width and aligns it to the left or right.
	 * Given string can be null, in which case the label is only aligned.
	 * @param label the JLabel 
	 * @param longestStr the longest possible String that the JLabel text might be
	 * @param rightAlign whether or not to right-align the label
	 * @return the label
	 */
	public static JLabel configureLabel(JLabel label, String longestStr, boolean rightAlign) {
		if (longestStr != null) {
			String text = label.getText();
			label.setText(longestStr);
			label.setPreferredSize(label.getPreferredSize());
			label.setText(text);
		}
		label.setHorizontalAlignment(rightAlign ? SwingConstants.RIGHT : SwingConstants.LEFT);
		return label;
	}
	
	public static void enableAntiAliasing(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	/**
	 * Returns a copy of the given image.
	 * @param bi an image to copy
	 * @return a copy
	 */
	public static BufferedImage deepCopy(BufferedImage bi) {
	    ColorModel cm = bi.getColorModel();
	    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
	    WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
	    return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
}
