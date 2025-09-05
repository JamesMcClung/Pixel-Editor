package tools;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import app.Constants;
import app.Renderable;
import canvas.BitMask;
import canvas.Layer;
import canvas.PixelMask;
import util.Util;

public class ColorSelector extends Tool implements Renderable {

	// static methods

	private static boolean isSelectionOpaque(Layer l, Point pixel, ToolParams params) {
		return params.app().canvasPanel.hasSelection() && l.get(pixel);
	}

	public ColorSelector() {
		super();
		hasStrength = false;
		sizeName = "Search Diameter";
		minSize = currentSize = 2;
	}

	// Fields

	private final ArrayList<Integer> rgbsInDrag = new ArrayList<>();
	private PixelMask selectionMask;

	// Internal Methods

	private boolean madeSelection() {
		return selectionMask != null;
	}

	private ToolResult selectColor(Layer l, Point pixel, ToolParams params) {
		// don't select anything if selection is opaque at mouse click
		if (isSelectionOpaque(l, pixel, params))
			return null;

		var cp = params.app().canvasPanel;
		var top = cp.getTopLayer(true);

		// now we want the pixel underneath the selection layer
		pixel = cp.getPointOnLayer(params.e().getPoint(), true, true);
		int rgb = top.getRGB(pixel);

		// don't select transparent pixels
		if (Util.getAlpha(rgb) == 0)
			return null;

		// add the region to the mask of pixels to select
		PixelMask newRegion = top.getMonochromeRegion(pixel, currentSize / 2d, rgbsInDrag);
		selectionMask = selectionMask == null ? newRegion : PixelMask.or(selectionMask, newRegion, new Point());
		params.app().canvasPanel.addRenderable(this);
		return new ToolResult(REPAINT);
	}

	// paint stuff

	@Override
	public void renderAt(Graphics2D g, AffineTransform tf) {
		if (!madeSelection())
			return;

		BitMask.renderOutline(g, selectionMask, tf, Constants.preselectionOutline);
	}

	// mouse stuff

	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		return selectColor(l, pixel, params);
	}

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		if (!madeSelection())
			return null;

		params.app().canvasPanel.removeRenderable(this);
		params.app().canvasPanel.select(selectionMask);
		rgbsInDrag.clear();
		selectionMask = null;
		return new ToolResult(REPAINT_AND_SAVE);
	}

	@Override
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		return selectColor(l, pixel, params);
	}

}
