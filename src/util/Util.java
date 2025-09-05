package util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

public class Util {

	public static int distSq(Point p1, Point p2) {
		int dx = p1.x - p2.x;
		int dy = p1.y - p2.y;
		return dx * dx + dy * dy;
	}

	public static int r(int rgb) {
		return rgb >>> 16 & 0xff;
	}

	public static int g(int rgb) {
		return rgb >>> 8 & 0xff;
	}

	public static int b(int rgb) {
		return rgb & 0xff;
	}

	public static int a(int rgb) {
		return rgb >>> 24 & 0xff;
	}

	public static int argb(int a, int r, int g, int b) {
		return a << 24 | r << 16 | g << 8 | b;
	}

	public static Point max(Point a, Point b) {
		return new Point(Math.max(a.x, b.x), Math.max(a.y, b.y));
	}

	public static Point min(Point a, Point b) {
		return new Point(Math.min(a.x, b.x), Math.min(a.y, b.y));
	}

	public static boolean rgbEqual(int rgb1, int rgb2) {
		return rgb1 == rgb2 || ((rgb1 | rgb2) >>> 24 == 0); // alpha = 0 for both
	}

	public static int getAlpha(int rgb) {
		return rgb >>> 24;
	}

	public static int floor(double a) {
		int af = (int) a;
		if (a < 0)
			af--;
		return af;
	}

	public static int ceil(double a) {
		int ac = (int) a;
		if (a > 0)
			ac++;
		return ac;
	}

	/**
	 * Returns a % b such that the result is positive.
	 * 
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

	public static Point sum(Point a, Point b) {
		return new Point(a.x + b.x, a.y + b.y);
	}

	public static Point difference(Point a, Point b) {
		return new Point(a.x - b.x, a.y - b.y);
	}

	public static Point times(Point a, double d) {
		return new Point(floor(a.x * d), floor(a.y * d));
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
	public static Color tileColor1 = new Color(245, 245, 245), tileColor2 = new Color(220, 220, 220);

	/**
	 * Creates a texture for the background. The texture is like a chess board, with tiles of the given dimension.
	 * 
	 * @param tileSize dimension of tile, in pixels
	 * @return a texure that can be applied using {@link Graphics2D#setPaint(java.awt.Paint)} and then
	 *         {@link Graphics2D#fill(java.awt.Shape)}.
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
	 * Sets the label's preferred width to the maximum possible width and aligns it to the left or right. Given string
	 * can be null, in which case the label is only aligned.
	 * 
	 * @param label      the JLabel
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
	 * Binds the given key to the given action.
	 * 
	 * @param component to bind key to
	 * @param key       a character, e.g. "A" (case sensitive!)
	 * @param action    what happens when key is pressed
	 */
	public static void addKeyBinding(JComponent comp, String key, ActionListener action) {
		var inputMap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		var actionMap = comp.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(key), key);
		actionMap.put(key, new AbstractAction() {
			private static final long serialVersionUID = -4711311088424057197L;

			@Override
			public void actionPerformed(ActionEvent e) {
				action.actionPerformed(e);
			}
		});
	}

	/**
	 * Returns a copy of the given image.
	 * 
	 * @param bi an image to copy
	 * @return a copy
	 */
	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public static void changeExtension(JFileChooser fc, String newExt) {
		String path = fc.getSelectedFile().getAbsolutePath();
		int lastdot = path.lastIndexOf('.');
		path = path.substring(0, lastdot + 1) + newExt;
		fc.setSelectedFile(new File(path));
	}

}
