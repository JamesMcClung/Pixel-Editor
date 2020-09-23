package app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import canvas.Layer;
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
	
	private List<Layer> layers = new ArrayList<>();
	private int renderStyle = initialRenderStyle;
	
	
	// methods
	
	/**
	 * Adds the given layer to the top of the list of layers.
	 * @param l the layer
	 */
	public void addLayer(Layer l) {
		layers.add(l);
	}
	
	/**
	 * Clears previous layers and replaces them with given layer. Ignores null layers.
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
	 * @return the layer
	 */
	public Layer getTopLayer() {
		if (layers.size() == 0)
			return null;
		return layers.get(layers.size() - 1);
	}
	
	/**
	 * Calculates and returns the pixel coordinates of the given point 
	 * @param pointOnScreen a point in the reference frame of this panel
	 * @return a point in the reference frame of the underlying top Layer 
	 */
	public Point getPointOnLayer(Point pointOnScreen) {
		var tf = getTopLayer().getTransform(new Point(), getSize());
		try {
			tf.invert();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		var pt = tf.transform(pointOnScreen, null);
		return new Point(Util.floor(pt.getX()), Util.floor(pt.getY()));
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
		if (layers.size() > 0)
			layers.get(0).renderAt(g2, loc, size, renderStyle);
		for (int i = 1; i < layers.size(); i++)
			layers.get(i).renderAt(g2, loc, size, Layer.RENDER_TRANSPARENT);
		
		// draw box around top layer
		getTopLayer().drawBoundingBox(g2, loc, size);
	}
	
	/**
	 * Sets render style to one of those defined in {@link canvas.Layer}. 
	 * @param s render style
	 */
	public void setRenderStyle(int s) {
		renderStyle = s;
		repaint();
	}
	
	/**
	 * Sets the layers and their images to a previous state. Ignores null states.
	 * @param state the state
	 */
	public void restoreState(SaveableState state) {
		if (state == null)
			return;
		layers.clear();
		for (int i = 0; i < state.layers().length; i++) {
			layers.add(state.layers()[i]);
			state.layers()[i].setImage(state.images()[i]);
		}
		repaint();
	}

	/**
	 * Creates and returns an array containing the layers.
	 * @return the array
	 */
	public Layer[] getLayers() {
		return layers.toArray(new Layer[layers.size()]);
	}
	
	/**
	 * Creates and returns an array containing deep copies of the layer images.
	 * @return the array
	 */
	public  BufferedImage[] getImageCopies() {
		BufferedImage[] stateImages = new BufferedImage[layers.size()];
		for (int i = 0; i < layers.size(); i++)
			stateImages[i] = Util.deepCopy(layers.get(i).getImage());
		return stateImages;
	}
}
