package canvas;

import static util.Util.modPositive;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

import io.IOUtil;
import util.Util;

public class Spritesheet extends Layer implements Iterable<Layer> {
	
	public Spritesheet(Dimension spriteDim, Dimension nSprites) {
		this(null, new BufferedImage(spriteDim.width * nSprites.width, spriteDim.height * nSprites.height, BufferedImage.TYPE_INT_ARGB), spriteDim);
	}
	
	public Spritesheet(File file, BufferedImage image) {
		this(file, image, IOUtil.readSpriteDimSignature(image));
	}
	
	/**
	 * Constructs a Spritesheet with the given parameters.
	 * @param file location where spritesheet is stored
	 * @param image the content of the spritesheet 
	 * @param spriteDim the number of pixels in each sprite
	 */
	public Spritesheet(File file, BufferedImage image, Dimension spriteDim) {
		super(image);
		this.file = file;
		this.spriteDim = spriteDim == null ? new Dimension(image.getWidth(), image.getHeight()) : new Dimension(spriteDim);
	}
	
	private File file;
	private final Dimension spriteDim; // the dimensions in pixels of each sprite
	private final Point currentSpriteIndex = new Point(0, 0);
	
	
	// methods
	
	public Layer getSprite(Point index) {
		return cropped(new Point(index.x * spriteDim.width, index.y * spriteDim.height), spriteDim);
	}
	
	public void setCurrentSprite(Point index) {
		currentSpriteIndex.setLocation(index);
	}
	
	/**
	 * Constructs and returns a view of the current sprite.
	 * @return a Layer with the view of the sprite
	 */
	public Layer getCurrentSprite() {
		return getSprite(currentSpriteIndex);
	}

	/**
	 * Constructs and returns a view of the specified sprite on the spritesheet.
	 * @param spritex the coordinate of the sprite on the sheet (e.g. (0,1))
	 * @return a Layer with the view of the sprite, or <code>null</code> if out of bounds.
	 */
	public Layer moveSprite(Point spriteIndex) {
		if (!isSpriteIndexValid(spriteIndex))
			return null;
		setCurrentSprite(spriteIndex);
		return getCurrentSprite();
	}
	
	/**
	 * Constructs and returns a view of the sprite to the right of the current sprite by specified amount
	 * @param relativePos amount to the right
	 * @return a Layer with the view of the sprite
	 */
	public Layer moveSpriteRelative(int relativePos) {
		return moveSprite(getIndexAfter(currentSpriteIndex, relativePos));
	}
	
	private Point getIndexAfter(Point index, int spritesAfter) {
		Dimension ssDim = getSSDim();
		int spritex = modPositive(index.x + spritesAfter, ssDim.width);
		int spritey = modPositive(index.y + Math.floorDiv(index.x + spritesAfter, ssDim.width), ssDim.height);
		return new Point(spritex, spritey);
	}
	
	public boolean isSpriteIndexValid(Point p) {
		return new Rectangle(getSSDim()).contains(p);
	}
	
	/**
	 * Draws a solid square around the current active sprite.
	 * @param g graphics to draw on
	 * @param loc top left corner of valid drawing area
	 * @param size dimension of valid drawing area
	 * @param highlightColor color to use
	 */
	public void renderSpriteHighlight(Graphics2D g, Point loc, Dimension size, Color highlightColor) {
		g = (Graphics2D) g.create();
		g.transform(getTransform(loc, size));
		g.setColor(highlightColor);
		g.fill(getSpriteBounds(getActiveSpriteIndex()));
		g.dispose();
	}
	
	public Rectangle getSpriteBounds(Point spriteIndex) {
		var spritedim = getSpriteDim();
		Rectangle rect = new Rectangle(spritedim);
		rect.setLocation(Util.elementwiseProd(spriteIndex, spritedim));
		return rect;
	}
	
	
	
	public File getFile() {
		return file;
	}
	public void setFile(File f) {
		this.file = f;
	}
	
	/**
	 * @return the name of this spritesheet (determined by file path), or "unnamed" if file is null
	 */
	public String getName() {
		if (file == null)
			return "unnamed";
		return file.getName();
	}
	
	public Dimension getSpriteDim() {
		return new Dimension(spriteDim);
	}
	
	/**
	 * Sets the dimension of each sprite to the given dimension. Shunts active sprite position if it would be invalidated.
	 * @param d new dimension, in pixels
	 */
	public void setSpriteDim(Dimension d) {
		if (d.width > getWidth() || d.height > getHeight())
			throw new RuntimeException("Invalid dimension: %s (spritesheet is dimension %s".formatted(d.toString(), getImageDim().toString()));
		spriteDim.setSize(d);
		
		// move active sprite if necessary
		Dimension newSSDim = getSSDim();
		currentSpriteIndex.move(Math.min(currentSpriteIndex.x, newSSDim.width-1), Math.min(currentSpriteIndex.y, newSSDim.height-1));
		
		// secretly sign the image with the new dimension
		IOUtil.addSpriteDimSignature(getImage(), d);
	}
	

	/**
	 * @return a Dimension that gives how many (full) sprites fit in the spreadsheet along each axis
	 */
	public Dimension getSSDim() {
		int width = getWidth() / spriteDim.width;
		int height = getHeight() / spriteDim.height;
		return new Dimension(width, height);
	}
	
	public Dimension getImageDim() {
		return new Dimension(getWidth(), getHeight());
	}
	
	public Point getActiveSpriteIndex() {
		return new Point(currentSpriteIndex);
	}

	public void setSpriteIndex(Point spriteIndex) {
		this.currentSpriteIndex.setLocation(spriteIndex);
	}

	@Override
	public Iterator<Layer> iterator() {
		return new Iterator<Layer>() {
			private boolean first = true; 
			private Point index = new Point();

			@Override
			public boolean hasNext() {
				return first || !index.equals(new Point());
			}

			@Override
			public Layer next() {
				var l = getSprite(index);
				first = false;
				index.setLocation(getIndexAfter(index, 1));
				return l;
			}
		};
	}
	
	
}
