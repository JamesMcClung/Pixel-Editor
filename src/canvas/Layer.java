package canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import util.Cache;
import util.Util;

public class Layer implements BitMask {
	
	public static final int RENDER_TRANSPARENT = 0;
	public static final int RENDER_TILES = 1;
	public static final int RENDER_WHITE = 2;
//	public static final int RENDER_GRID = 3; TODO
	

	public static final Color ERASE_COLOR = new Color(255, 255, 255, 0); 
	
	/**
	 * A Layer is a grid of pixels that can be drawn on, rendered to the display, etc.
	 */
	
	public Layer(int width, int height) {
		this(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
	}
	
	public Layer(Dimension size) {
		this(size.width, size.height);
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
	
	public int getRGB(Point pixel) {
		return image.getRGB(pixel.x, pixel.y);
	}
	
	/**
	 * Returns the width of the underlying image, in pixels.
	 * @return the width
	 */
	@Override
	public int getWidth() {
		return image.getTileWidth();
	}
	
	/**
	 * Returns the height of the underlying image, in pixels.
	 * @return the height
	 */
	@Override
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
	 * Sets the color value of the given pixel. Ignores out-of-bounds pixels.
	 * @param pixel pixel to set color of
	 * @param color the color
	 */
	public void setPixel(Point pixel, Color color) {
		if (isInBounds(pixel))
			image.setRGB(pixel.x, pixel.y, color.getRGB());
	}
	
	/**
	 * Sets the color values of each pixel within the given radius of the given point.
	 * @param center center of circle to fill
	 * @param radius2 square of radius of circle
	 * @param color the color
	 */
	public void setPixels(Point center, double radius, Color color) {
		doThingInCircle(center, radius, (image, pixel) -> setPixel(pixel, color));
	}
	
	public void setPixels(Point x0, PixelMask mask, Color color) {
		int rgb = color.getRGB();
		mask.doAction(x0, (p) -> image.setRGB(p.x, p.y, rgb));
	}
	
	/**
	 * Make sure to call {@link Graphics#dispose} when done!
	 * @return the graphics that can be used to draw on this layer's underlying image 
	 */
	public Graphics2D getGraphics() {
		return image.createGraphics();
	}
	
	/**
	 * Draws the given image at the given point on this layer's underlying image.
	 * @param p the point in the image to draw given image
	 */
	public void drawImage(BufferedImage image, Point p) {
		var g = getGraphics();
		g.drawImage(image, p.x, p.y, null);
		g.dispose();
	}
	/**
	 * Sets the layer to the given image. 
	 */
	public void setImage(BufferedImage image) {
		this.image.setData(image.getData());
	}
	
	
	// Rendering methods 
	
	// slight optimization to prevent generation of new Paint objects every time
	private final Dimension backgroundTileDim = new Dimension(1, 1); // why does app break if this is static?
	private Cache<Dimension, Paint> backgroundPaint = new Cache<>(Util::getPaint);
	
	/**
	 * Draws the image scaled and moved to fit within the rectangle specified by loc and size, including a background depending on specified style.
	 * @param g graphics to draw on
	 * @param loc top-left pixel of image when drawn
	 * @param size size of image when drawn, in pixels
	 * @param style either {@link #RENDER_TILES}, {@link #RENDER_WHITE}, or {@link #RENDER_TRANSPARENT}.
	 */
	public void renderAt(Graphics2D g, Point loc, Dimension size, int style) {
		renderAt(g, getTransform(loc, size), style);
	}
	
	/**
	 * Draws the image scaled and moved according to given transform, including a background depending on specified style.
	 * @param g graphics to draw on
	 * @param tf AffineTransform to use
	 * @param style either {@link #RENDER_TILES}, {@link #RENDER_WHITE}, or {@link #RENDER_TRANSPARENT}.
	 */
	public void renderAt(Graphics2D g, AffineTransform tf, int style) {
		g = (Graphics2D) g.create();
		g.transform(tf);
		switch(style) {
		case RENDER_TILES:
			g.setPaint(backgroundPaint.get(backgroundTileDim));
			g.fillRect(0, 0, getWidth(), getHeight());
			break;
		case RENDER_WHITE:
			g.setColor(Color.WHITE);
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
	
	public PixelMask getMonochromeRegion(Point pixel, double searchRadius, List<Integer> otherRGBs) {
		final List<Integer> rgbs = otherRGBs == null ? new ArrayList<>() : otherRGBs;
		
		rgbs.add(image.getRGB(pixel.x, pixel.y)); // make sure we search for color at given pixel
		
		PixelMask mask = new PixelMask(getSize());
		PixelMask explored = new PixelMask(getSize());
		mask.set(pixel, true);
		PixelMask.Condition condition = (p, rgb2) -> {
			for (Integer rgb : rgbs)
				if (Util.rgbEqual(rgb, rgb2))
					return true;
			return false;
		};
		
		explore(pixel, searchRadius, mask, explored, condition);
		
		return mask;
	}
	private void explore(Point p, double searchRadius, PixelMask mask, PixelMask explored, PixelMask.Condition condition) {
		explored.set(p.x, p.y, true);
		doThingInCircle(p, searchRadius, (image, pixel) -> {
			if (isInBounds(pixel) && !explored.get(pixel.x, pixel.y) && condition.accept(null, image.getRGB(pixel.x, pixel.y))) {
				mask.set(pixel.x, pixel.y, true);
				explore(pixel, searchRadius, mask, explored, condition);
			}
		});
	}
	
	/**
	 * Returns whether the given pixel is within the bounds of the underlying image.
	 * @param pixel the pixel
	 * @return see above
	 */
	public boolean isInBounds(Point pixel) {
		return pixel.x > -1 && pixel.y > -1 && pixel.x < image.getWidth() && pixel.y < image.getHeight();
	}

	public void clearImage() {
		Point pixel = new Point();
		for (pixel.x = 0; pixel.x < getWidth(); pixel.x++)
			for (pixel.y = 0; pixel.y < getHeight(); pixel.y++)
				setPixel(pixel, ERASE_COLOR);
	}
	 
	
	// Util Methods
	
	public static interface ThingDoer {
		void doThing(BufferedImage image, Point pixel);
	}
	
	public void doThingInCircle(Point center, double radius, ThingDoer doer) {
		int x0 = Util.floor(center.x - radius), y0 = Util.floor(center.y - radius);
		Point p = new Point(x0, y0);
		double radius2 = radius * radius;
		for (p.x = x0; p.x <= center.x + radius; p.x++) {
			for (p.y = y0; p.y <= center.y + radius; p.y++) {
				if (Math.pow(p.x - center.x, 2) + Math.pow(p.y - center.y, 2) <= radius2)
					doer.doThing(image, p);
			}
		}
	}
	
	
	// Bitmask stuff

	@Override
	public boolean get(int x, int y) {
		return get(new Point(x, y));
	}
	@Override
	public boolean get(Point pixel) {
		return isInBounds(pixel) && Util.getAlpha(getRGB(pixel)) != 0;
	}
	
}