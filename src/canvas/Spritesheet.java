package canvas;

import static util.Util.divideFloor;
import static util.Util.modPositive;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;

import io.IOUtil;
import util.Util;

public class Spritesheet extends Layer {
	
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
	
	private final File file;
	private final Dimension spriteDim; // the dimensions in pixels of each sprite
	private final Point activeSpriteIndex = new Point(0, 0);
	
	
	// methods
	
	/**
	 * Constructs and returns a view of the most recently selected sprite on the spritesheet.
	 * @return a Layer with the view of the sprite
	 */
	public Layer getSprite() {
		try {
			return new Layer(getImage().getSubimage(activeSpriteIndex.x * spriteDim.width, activeSpriteIndex.y * spriteDim.height, spriteDim.width, spriteDim.height));
		} catch (RasterFormatException e) {
			e.printStackTrace();
			System.out.println("sprite index: " + activeSpriteIndex.toString());
			System.out.println("sprite dims: " + spriteDim.toString());
			System.out.println("image dims: " + getImageDim().toString());
			return null;
		}
	}

	/**
	 * Constructs and returns a view of the specified sprite on the spritesheet.
	 * @param spritex the coordinate of the sprite on the sheet (e.g. (0,1))
	 * @return a Layer with the view of the sprite, or <code>null</code> if out of bounds.
	 */
	public Layer getSprite(Point spriteIndex) {
		if (!isSpriteIndexValid(spriteIndex))
			return null;
		activeSpriteIndex.setLocation(spriteIndex);
		return getSprite();
	}
	
	/**
	 * Constructs and returns a view of the sprite to the right of the current sprite by specified amount
	 * @param relativePos amount to the right
	 * @return a Layer with the view of the sprite
	 */
	public Layer getSpriteRelative(int relativePos) {
		Dimension ssDim = getSSDim();
		int spritex = modPositive(activeSpriteIndex.x + relativePos, ssDim.width);
		int spritey = modPositive(activeSpriteIndex.y + divideFloor(activeSpriteIndex.x + relativePos, ssDim.width), ssDim.height);
		return getSprite(new Point(spritex, spritey));
	}
	
	public boolean isSpriteIndexValid(Point p) {
		return new Rectangle(getSSDim()).contains(p);
	}
	
	public void renderSpriteHighlight(Graphics2D g, Point loc, Dimension size, Color highlightColor) {
		g.transform(getTransform(loc, size));
		g.setColor(highlightColor);
		g.fill(getSpriteBounds(getActiveSpriteIndex()));
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
		activeSpriteIndex.move(Math.min(activeSpriteIndex.x, newSSDim.width-1), Math.min(activeSpriteIndex.y, newSSDim.height-1));
		
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
		return new Point(activeSpriteIndex);
	}
	
	
}
