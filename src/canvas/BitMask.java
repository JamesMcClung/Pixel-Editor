package canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import myawt.Line;
import util.XorSet;

public interface BitMask {

	boolean get(int x, int y);

	default boolean get(Point p) {
		return get(p.x, p.y);
	}

	int getWidth();

	int getHeight();

	default Dimension getSize() {
		return new Dimension(getWidth(), getHeight());
	}

	/**
	 * Renders an outline of the given bitmask in the given color.
	 * 
	 * @param g  graphics to draw on
	 * @param b  a bitmask
	 * @param tf transform from bitmask space to graphics space
	 * @param c  color of outline
	 */
	public static void renderOutline(Graphics2D g, BitMask b, AffineTransform tf, Color c) {
		XorSet<Line> lines = new XorSet<>();
		for (int i = 0; i < b.getWidth(); i++) {
			for (int j = 0; j < b.getHeight(); j++) {
				if (b.get(i, j)) {
					lines.add(new Line(i, j, i + 1, j));
					lines.add(new Line(i + 1, j, i + 1, j + 1));
					lines.add(new Line(i, j, i, j + 1));
					lines.add(new Line(i, j + 1, i + 1, j + 1));
				}
			}
		}
		g.setColor(c);
		for (var line : lines)
			g.draw(tf.createTransformedShape(line));
	}

}
