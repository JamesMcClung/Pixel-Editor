package canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import util.Cache;
import util.Util;

public class Layer {
	
	public static final int RENDER_TRANSPARENT = 0;
	public static final int RENDER_OPAQUE = 1;
//	public static final int RENDER_GRID = 2; TODO
	
	public static final int maxNumSaveStates = 100;
	
	/**
	 * A Layer is a grid of pixels that can be drawn on, rendered to the display, etc.
	 */
	
	public Layer(int width, int height) {
		this(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
	}
	
	public Layer(BufferedImage image) {
		this.image = image;
	}

	
	// fields

	private final BufferedImage image;
	
	
	// Getters & Setters
	
	/**
	 * @return the underlying image
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	 * Returns the width of the underlying image, in pixels.
	 * @return the width
	 */
	public int getWidth() {
		return image.getTileWidth();
	}
	
	/**
	 * Returns the height of the underlying image, in pixels.
	 * @return the height
	 */
	public int getHeight() {
		return image.getHeight();
	}

	
	// Editing methods
	
	/**
	 * Draws on top of the preexisting pixel value, which will bleed through if alpha is not 255.
	 * @param pixel
	 * @param color
	 */
	public void drawPixel(Point pixel, Color color) {
		var g = image.createGraphics();
		g.setColor(color);
		g.drawLine(pixel.x, pixel.y, pixel.x, pixel.y);
		g.dispose();
	}
	
	/**
	 * Sets the color value of the given pixel.
	 * @param pixel pixel to set color of
	 * @param color the color
	 */
	public void setPixel(Point pixel, Color color) {
		image.setRGB(pixel.x, pixel.y, color.getRGB());
	}
	
	/**
	 * Make sure to call {@link Graphics#dispose} when done!
	 * @return the graphics that can be used to draw on this layer's underlying image 
	 */
	public Graphics2D getGraphics() {
		return image.createGraphics();
	}
	
	/**
	 * Draws the given image. 
	 */
	public void drawImage(BufferedImage image) {
		var g = getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
	}
	/**
	 * Sets the layer to the given image. 
	 */
	public void setImage(BufferedImage image) {
		image.setData(image.getData());
	}
	
	
	// Rendering methods 
	
	/**
	 * Draws the image scaled and moved to fit within the rectangle specified by loc and size
	 * @param g graphics to draw on
	 * @param loc top-left pixel of image when drawn
	 * @param size size of image when drawn, in pixels
	 */
	public void renderAt(Graphics2D g, Point loc, Dimension size) {
		renderAt(g, loc, size, RENDER_TRANSPARENT);
	}
	
	// slight optimization to prevent generation of new Paint objects every time
	private static final Dimension backgroundTileDim = new Dimension(1, 1);
	private Cache<Dimension, Paint> backgroundPaint = new Cache<>(Util::getPaint);
	
	/**
	 * Does same thing as {@link Layer#renderAt}, but draws background depending on specified style.
	 * @param g graphics to draw on
	 * @param loc top-left pixel of image when drawn
	 * @param size size of image when drawn, in pixels
	 * @param style either {@link #RENDER_OPAQUE} or {@link #RENDER_TRANSPARENT}.
	 */
	public void renderAt(Graphics2D g, Point loc, Dimension size, int style) {
		g = (Graphics2D) g.create();
		g.transform(getTransform(loc, size));
		switch(style) {
		case RENDER_OPAQUE:
			g.setPaint(backgroundPaint.get(backgroundTileDim));
			g.fillRect(0, 0, getWidth(), getHeight());
			break;
		case RENDER_TRANSPARENT:
			break;
		default:	
			throw new IllegalArgumentException("Invalid style: " + style);
		}
		g.drawImage(getImage(), 0, 0, null);
		g.dispose();
	}
	
	/**
	 * Draws a thin rectangle around where this layer's image would be drawn.
	 * @param g graphics to draw  on
	 * @param loc top-left pixel of region to draw in
	 * @param size dimensions in pixels of region to draw in
	 */
	public void drawBoundingBox(Graphics2D g, Point loc, Dimension size) {
		// these points will be transformed to where they need to be in the graphics
		Point topLeftPt = new Point();
		Point bottomRightPt = new Point(getWidth(),  getHeight());
		var tf = getTransform(loc, size);
		tf.transform(bottomRightPt, bottomRightPt);
		tf.transform(topLeftPt, topLeftPt);
		g.setColor(Color.BLACK);
		g.drawRect(topLeftPt.x, topLeftPt.y, bottomRightPt.x, bottomRightPt.y);
	}
	
	/**
	 * Returns the transformation associated with the rendering of the underlying image onto graphics 
	 * @param loc top-left corner of image in graphics coords
	 * @param size dimensions of image in graphics coords
	 * @return the transform
	 */
	public AffineTransform getTransform(Point loc, Dimension size) {
		float scalex = size.width / (float) getWidth();
		float scaley = size.height / (float) getHeight();
		float scale = Math.min(scalex, scaley);
		return new AffineTransform(scale, 0, 0, scale, loc.x, loc.y);
	}
	 
	
}