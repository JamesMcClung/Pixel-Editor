package app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JPanel;

import canvas.BitMask;
import canvas.Layer;
import canvas.PixelMask;
import util.Util;

public class CanvasPanel extends JPanel {

	// constants

	private static final long serialVersionUID = 7046692110388368464L;

	public static final int width = 500, height = 500;
	public static final int initialRenderStyle = Layer.RENDER_TILES;

	// constructors

	public CanvasPanel() {
		this.setPreferredSize(new Dimension(width, height));
	}

	// fields

	private int renderStyle = initialRenderStyle;

	final ArrayList<Layer> layers = new ArrayList<>();
	private Layer selection = null;
	private final Point selectionLoc = new Point();

	private final HashSet<Renderable> renderables = new HashSet<>(); // things drawn on top, like selection outlines

	// methods

	/**
	 * Cuts out pixels within the given shape from the top layer, putting them in their own special layer. This layer
	 * can be rendered at arbitrary locations; see {@link CanvasPanel#setSelectionLoc(Point)}. If the given mask is
	 * null, the entire top layer is made a selection. If a selection already exists, the specified region is added to
	 * the existing selection.
	 * 
	 * @param mask mask specifying desired selection
	 */
	public void select(PixelMask mask) {
		Layer top = getTopLayer(true);
		if (mask == null)
			mask = new PixelMask(top.getSize()).invert();

		boolean hadSelection = hasSelection();

		Point currentSelOffset = getPointOnLayer(selectionLoc, false, true);
		Point newSelOffset = Util.min(new Point(), currentSelOffset);

		// set up selection layer (must be big enough to contain previous selection entirely)
		Layer newSelection;
		if (hadSelection)
			newSelection = new Layer(
					Math.max(top.getWidth(), currentSelOffset.x + selection.getWidth()) - newSelOffset.x,
					Math.max(top.getHeight(), currentSelOffset.y + selection.getHeight()) - newSelOffset.y);
		else
			newSelection = new Layer(top.getWidth(), top.getHeight());

		// transfer selected region rgb values to selection layer
		mask.doAction(new Point(), p -> {
			newSelection.getImage().setRGB(p.x - newSelOffset.x, p.y - newSelOffset.y, top.getRGB(p));
			top.setPixel(p, Layer.ERASE_COLOR);
		});
		if (hadSelection) // draw previous selection on top
			newSelection.drawImage(selection.getImage(),
					Util.difference(getPointOnLayer(selectionLoc, false, true), newSelOffset));
		selection = newSelection;

		setSelectionLoc(Util.min(new Point(), selectionLoc));
	}

	/**
	 * Sets where the selection is to be drawn.
	 * 
	 * @param p top-left coordinate of selection in this component's frame
	 */
	public void setSelectionLoc(Point p) {
		selectionLoc.setLocation(p);
	}

	public Point getSelectionLoc() {
		return new Point(selectionLoc);
	}

	public Layer getSelection() {
		return selection;
	}

	public void snapSelectionToGrid() {
		var tf = getTopLayer(true).getTransform(new Point(), getSize());
		Point dest = getPointOnLayer(selectionLoc, false, true);
		tf.transform(dest, dest);
		setSelectionLoc(dest);
	}

	/**
	 * Unselects the selection, overriding what was beneath it.
	 */
	public void dropSelection() {
		Layer top = getTopLayer(true);
		Point dest = getPointOnLayer(selectionLoc, false, true);
		top.drawImage(selection.getImage(), dest);
		selectionLoc.setLocation(0, 0);
		selection = null;
	}

	public void deleteSelection() {
		selectionLoc.setLocation(0, 0);
		selection = null;
	}

	public boolean hasSelection() {
		return selection != null;
	}

	public void ensureHasSelection() {
		if (hasSelection())
			return;
		select(null);
	}

	// copy and pasting

	private BufferedImage clipboard;
	private Point clipboardPos = new Point();

	public void copy() {
		clipboard = Util.deepCopy(getTopLayer().getImage());
		clipboardPos.setLocation(selectionLoc);
	}

	public void cut() {
		copy();
		if (hasSelection())
			deleteSelection();
		else
			getTopLayer().clearImage();
	}

	public void paste() {
		if (hasSelection())
			selection.drawImage(clipboard, getPointOnLayer(Util.difference(clipboardPos, selectionLoc), true, true));
		else {
			selection = new Layer(clipboard);
			selectionLoc.setLocation(clipboardPos);
		}
	}

	public boolean canPaste() {
		return clipboard != null;
	}

	// other operations
	public void rotate(int quarterTurns) {
		ensureHasSelection();
		quarterTurns %= 4;
		Point center = selection.getMeanPixel();

		for (int i = 0; i < quarterTurns; i++)
			selection = selection.rotatedCW();
		for (int i = 0; i > quarterTurns; i--)
			selection = selection.rotatedCCW();

		Point newCenter = selection.getMeanPixel();

		// transform before taking difference because transform might include a translation
		var tf = getTransform();
		tf.transform(center, center);
		tf.transform(newCenter, newCenter);
		Point diff = Util.difference(center, newCenter);

		// move selection location to maintain same center
		selectionLoc.translate(diff.x, diff.y);
	}

	/**
	 * Reflects the selection or entire sprite across the specified axis.
	 * 
	 * @param updown true for up/down, false for left/right
	 */
	public void reflect(boolean updown) {
		ensureHasSelection();

		if (updown)
			selection.reflectUpDown();
		else
			selection.reflectLeftRight();
	}

	/**
	 * Adds the given layer to the top of the list of layers.
	 * 
	 * @param l the layer
	 */
	public void addLayer(Layer l) {
		layers.add(l);
	}

	/**
	 * Clears previous layers and replaces them with given layer. Ignores null layers.
	 * 
	 * @param l given layer
	 */
	public void setLayer(Layer l) {
		if (l == null)
			return;
		layers.clear();
		addLayer(l);
	}

	/**
	 * Returns the topmost (last to be rendered) layer, or null if there are no layers
	 * 
	 * @return the layer
	 */
	public Layer getTopLayer() {
		return getTopLayer(false);
	}

	/**
	 * Returns the topmost (last to be rendered) layer, or null if there are no layers.
	 * 
	 * @param bypassSelection whether or not to ignore the selection layer
	 * @return the layer
	 */
	public Layer getTopLayer(boolean bypassSelection) {
		if (!bypassSelection && hasSelection())
			return selection;
		if (layers.size() == 0)
			return null;
		return layers.get(layers.size() - 1);
	}

	public boolean hasLayer() {
		return !layers.isEmpty();
	}

	/**
	 * Creates and returns an array of all the layers, in order, with the selection at the end.
	 * 
	 * @param includeSelection whether or not to include the selection
	 * @return the array
	 */
	public Layer[] getLayers(boolean includeSelection) {
		includeSelection = includeSelection && hasSelection();
		Layer[] ls = new Layer[layers.size() + (includeSelection ? 1 : 0)];

		layers.toArray(ls);

		if (includeSelection)
			ls[ls.length - 1] = selection;

		return ls;
	}

	/**
	 * Calculates and returns the pixel coordinates of the given point
	 * 
	 * @param pointOnScreen a point in the reference frame of this panel
	 * @return a point in the reference frame of the underlying top Layer, not including the selection which might be
	 *         floating
	 */
	public Point getPointOnLayer(Point pointOnScreen, boolean roundDown, boolean bypassSelection) {
		if (!hasLayer())
			return null;

		// determine which transform to use
		AffineTransform tf;
		if (hasSelection() && !bypassSelection)
			tf = getSelectionTransform();
		else
			tf = getTransform();

		// find pixel according to inverse transform
		try {
			tf.invert();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		var pt = tf.transform(pointOnScreen, null);

		// round as needed
		if (roundDown)
			return new Point(Util.floor(pt.getX()), Util.floor(pt.getY()));
		else
			return new Point((int) Math.round(pt.getX()), (int) Math.round(pt.getY()));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (layers.isEmpty())
			return;

		var g2 = (Graphics2D) g;
		var loc = new Point(); // draw at 0,0 in panel
		var size = getSize();

		// render all layers, bottom to top
		var tf = getTransform();
		layers.get(0).renderAt(g2, tf, renderStyle);
		for (int i = 1; i < layers.size(); i++)
			layers.get(i).renderAt(g2, tf, Layer.RENDER_TRANSPARENT);
		if (hasSelection()) {
			var stf = getSelectionTransform();
			selection.renderAt(g2, stf, Layer.RENDER_TRANSPARENT);
			BitMask.renderOutline(g2, selection, stf, Constants.selectionOutline);
		}

		// draw box around bottom layer, which determines transformation of other layers
		layers.get(0).drawBoundingBox(g2, loc, size);

		for (var r : renderables)
			r.renderAt(g2, tf);
	}

	/**
	 * Sets render style to one of those defined in {@link canvas.Layer}.
	 * 
	 * @param s render style
	 */
	public void setRenderStyle(int s) {
		renderStyle = s;
		repaint();
	}

	public void addRenderable(Renderable r) {
		renderables.add(r);
	}

	public boolean removeRenderable(Renderable r) {
		return renderables.remove(r);
	}

	public AffineTransform getTransform() {
		if (layers.isEmpty())
			return null;
		return layers.get(0).getTransform(new Point(), getSize());
	}

	public AffineTransform getSelectionTransform() {
		if (hasSelection()) {
			var tf = AffineTransform.getTranslateInstance(selectionLoc.x, selectionLoc.y);
			tf.concatenate(getTransform());
			return tf;
		}
		return null;
	}

	// State Saving

	/**
	 * Sets the layers and their images to a previous state. Ignores null states.
	 * 
	 * @param state the state
	 */
	public void restoreState(State state) {
		if (state == null)
			return;
		layers.clear();
		for (int i = 0; i < state.layers().length; i++) {
			layers.add(state.layers()[i]);
			state.layers()[i].setImage(state.images()[i]);
		}
		selection = state.hadSelection() ? new Layer(state.selection()) : null;
		selectionLoc.setLocation(state.selectionLoc());
		repaint();
	}

	public State getState() {
		boolean hadSelection = hasSelection();
		return new State(getLayers(), getImageCopies(), hadSelection,
				hadSelection ? Util.deepCopy(selection.getImage()) : null,
				hadSelection ? new Point(selectionLoc) : new Point());
	}

	/**
	 * Creates and returns an array containing the layers.
	 * 
	 * @return the array
	 */
	private Layer[] getLayers() {
		return layers.toArray(new Layer[layers.size()]);
	}

	/**
	 * Creates and returns an array containing deep copies of the layer images.
	 * 
	 * @return the array
	 */
	private BufferedImage[] getImageCopies() {
		BufferedImage[] stateImages = new BufferedImage[layers.size()];
		for (int i = 0; i < layers.size(); i++)
			stateImages[i] = Util.deepCopy(layers.get(i).getImage());
		return stateImages;
	}

	public record State(Layer[] layers, BufferedImage[] images, boolean hadSelection, BufferedImage selection,
			Point selectionLoc) {
	}
}
