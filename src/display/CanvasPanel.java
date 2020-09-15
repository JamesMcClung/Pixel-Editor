package display;

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
	
	
	// constructors

	public CanvasPanel() {
		this.setPreferredSize(new Dimension(width, height));
	}
	
	
	// fields
	
	private List<Layer> layers = new ArrayList<>();
	private int renderStyle;
	
	
	// methods
	
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
	
	public Layer getTopLayer() {
		return layers.get(layers.size() - 1);
	}
	
	public Point getPointOnLayer(Point pointOnScreen) {
		var tf = getTopLayer().getTransform(new Point(), getSize());
		try {
			tf.invert();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		var pt = tf.transform(pointOnScreen, null);
		return new Point((int) pt.getX(), (int) pt.getY());
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (layers.isEmpty())
			return;
		
		var g2 = (Graphics2D) g;
		var loc = new Point(); // draw at 0,0 in panel
		var size = getSize();
		for (Layer l : layers) { // render all layers, bottom to top
			l.renderAt(g2, loc, size, renderStyle);
		}
		
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
	 * Creates a copy of the current state.
	 * @return the state
	 */
	public SaveableState getState() {
		Layer[] stateLayers = layers.toArray(new Layer[layers.size()]);
		BufferedImage[] stateImages = new BufferedImage[layers.size()];
		for (int i = 0; i < stateLayers.length; i++)
			stateImages[i] = Util.deepCopy(stateLayers[i].getImage());
		return new SaveableState(stateLayers, stateImages);
	}
}
