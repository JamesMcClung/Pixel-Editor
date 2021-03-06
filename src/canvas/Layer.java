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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	/**
	 * Calculates the mean pixel location, weighted by alpha.
	 * @return mean pixel
	 */
	public Point getMeanPixel() {
		Point mp = new Point();
		int alpha; 
		int totalAlpha = 0;
		for (int i = 0; i < getWidth(); i++)
			for (int j = 0; j < getHeight(); j++) {
				alpha = Util.getAlpha(image.getRGB(i, j));
				mp.translate(i * alpha, j * alpha);
				totalAlpha += alpha;
			}
		mp.x /= totalAlpha;
		mp.y /= totalAlpha;
		return mp;
	}
	
	public PixelMask getMonochromeRegion(Point pixel, double searchRadius, List<Integer> otherRGBs) {
		final List<Integer> rgbs = otherRGBs == null ? new ArrayList<>() : otherRGBs;
		
		rgbs.add(image.getRGB(pixel.x, pixel.y)); // make sure we search for color at given pixel
		
		PixelMask mask = new PixelMask(getSize());
		PixelMask explored = new PixelMask(getSize());
		Set<Point> frontier1 = new HashSet<>(), frontier2 = new HashSet<>();
		mask.set(pixel, true);
		PixelMask.Condition condition = (p, rgb2) -> {
			for (Integer rgb : rgbs)
				if (Util.rgbEqual(rgb, rgb2))
					return true;
			return false;
		};
		
		// find the points
		frontier1.add(new Point(pixel));
		while (!frontier1.isEmpty()) {
			final Set<Point> frontier = frontier2;
			for (Point p : frontier1) {
				explored.set(p, true);
				doThingInCircle(p, searchRadius, (image, p2) -> {
					if (isInBounds(p2) && !explored.get(p2.x, p2.y) && condition.accept(null, image.getRGB(p2.x, p2.y))) {
						mask.set(p2.x, p2.y, true);
						frontier.add(new Point(p2));
					}
				});
			}
			frontier1.clear();
			frontier2 = frontier1;
			frontier1 = frontier;
		}
		
		return mask;
	}
	
	/**
	 * Returns whether the given pixel is within the bounds of the underlying image.
	 * @param pixel the pixel
	 * @return see above
	 */
	public boolean isInBounds(Point pixel) {
		return pixel.x > -1 && pixel.y > -1 && pixel.x < image.getWidth() && pixel.y < image.getHeight();
	}
	
	public boolean hasVisibleContent() {
		for (int i  = 0; i < getWidth(); i++)
			for (int j = 0; j < getHeight(); j++)
				if (Util.getAlpha(image.getRGB(i, j)) > 0)
					return true;
		return false;
	}

	public void clearImage() {
		Point pixel = new Point();
		for (pixel.x = 0; pixel.x < getWidth(); pixel.x++)
			for (pixel.y = 0; pixel.y < getHeight(); pixel.y++)
				setPixel(pixel, ERASE_COLOR);
	}
	
	/**
	 * Returns a new Layer that is a rotated version of this layer, rotated 90 degrees clockwise.
	 * @return the layer
	 */
	public Layer rotatedCW() {
		Layer rotated = new Layer(getHeight(), getWidth());
		for (int i = 0; i < getWidth(); i++)
			for (int j = 0; j < getHeight(); j++)
				rotated.image.setRGB(j, i, image.getRGB(i, getHeight() - 1 - j));
		return rotated;
	}
	/**
	 * Returns a new Layer that is a rotated version of this layer, rotated 90 degrees counterclockwise.
	 * @return the layer
	 */
	public Layer rotatedCCW() {
		Layer rotated = new Layer(getHeight(), getWidth());
		for (int i = 0; i < getWidth(); i++)
			for (int j = 0; j < getHeight(); j++)
				rotated.image.setRGB(j, i, image.getRGB(getWidth() - 1 - i, j));
		return rotated;
	}
	
	public void reflectLeftRight() {
		int rgb; 
		for (int i = 0; i < getWidth()/2; i++) {
			for (int j = 0; j < getHeight(); j++) {
				rgb = image.getRGB(i, j);
				image.setRGB(i, j, image.getRGB(getWidth() - 1 - i, j));
				image.setRGB(getWidth() - 1 - i, j, rgb);
			}
		}
	}
	public void reflectUpDown() {
		int rgb; 
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight()/2; j++) {
				rgb = image.getRGB(i, j);
				image.setRGB(i, j, image.getRGB(i, getHeight() - 1 - j));
				image.setRGB(i, getHeight() - 1 - j, rgb);
			}
		}
	}
	
	/**
	 * Creates a new layer from this one, scaled up or down as specified.
	 * The new layer is not a view of this one.
	 * @param sx scale x
	 * @param sy scale y
	 * @return the layer
	 */
	public Layer scaled(float sx, float sy) {
		var scaledSize = getScaledSize(sx, sy);
		BufferedImage scaledIm = new BufferedImage(scaledSize.width, scaledSize.height, BufferedImage.TYPE_INT_ARGB);
		var g = scaledIm.createGraphics();
		var tf = new AffineTransform(1d*scaledSize.width / image.getWidth(), 0, 0, 1d*scaledSize.height / image.getHeight(), 0, 0);
		g.drawImage(image, tf, null);
		g.dispose();
		return new Layer(scaledIm);
	}
	/**
	 * Returns what the scaled size would be (as in {@link #scaled(float, float)}).
	 * @param sx scale x
	 * @param sy scale y
	 * @return the dimension
	 */
	public Dimension getScaledSize(float sx, float sy) {
		var scaledSize = getSize();
		scaledSize.width = Math.round(scaledSize.width * sx);
		scaledSize.height = Math.round(scaledSize.height * sy);
		return scaledSize;
	}
	
	/**
	 * Finds and returns the minimal region containing all visible pixels
	 * @return a view of the region as a layer backed by this image
	 */
	public Layer shrinkwrapped() {
		Point min = new Point(image.getWidth(), image.getHeight());
		Point max = new Point(-1, -1);
		Point p = new Point();
		for (p.x = 0; p.x < image.getWidth(); p.x++) {
			for (p.y = 0; p.y < image.getHeight(); p.y++) {
				int alpha = Util.getAlpha(getRGB(p));
				if (alpha > 0) {
					min.setLocation(Util.min(min, p));
					max.setLocation(Util.max(max, p));
				}
			}
		}
		return cropped(min, max);
	}
	/**
	 * Returns a cropped version of this layer, backed by the same image.
	 * @param p1 top-left, inclusive
	 * @param p2 bottom-right, inclusive
	 * @return the layer
	 */
	public Layer cropped(Point p1, Point p2) {
		return new Layer(image.getSubimage(p1.x, p1.y, p2.x-p1.x+1, p2.y-p1.y+1));
	}
	public Layer cropped(Point p, Dimension d) {
		return new Layer(image.getSubimage(p.x, p.y, d.width, d.height));
	}

	
	// Util Methods
	public static interface ThingDoer {
		void doThing(BufferedImage image, Point pixel);
	}
	
	/**
	 * Performs a given action at each point in a specified circle.
	 * @param center center of circle
	 * @param radius radius of circle (inclusive)
	 * @param doer map from (BufferedImage, Point) -> void
	 */
	public void doThingInCircle(Point center, double radius, ThingDoer doer) {
		int x0 = Util.floor(center.x - radius), y0 = Util.floor(center.y - radius);
		Point p = new Point(x0, y0);
		double radius2 = radius * radius;
		for (p.x = x0; p.x <= center.x + radius; p.x++) {
			for (p.y = y0; p.y <= center.y + radius; p.y++) {
				if (Math.pow(p.x - center.x, 2) + Math.pow(p.y - center.y, 2) <= radius2 && isInBounds(p))
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