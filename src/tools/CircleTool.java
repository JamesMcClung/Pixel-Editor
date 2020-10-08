package tools;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import app.Constants;
import app.Renderable;
import canvas.BitMask;
import canvas.CircleMask;
import canvas.Layer;
import util.Cache;

public abstract class CircleTool extends Tool implements Renderable {

	public static final Cache<Integer, BitMask> circleMaskCache = new Cache<>(diam -> new CircleMask(diam));
	

	@Override
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		currentPixel.setLocation(pixel);
		return new ToolResult(REPAINT);
	}
	
	@Override
	public ToolResult move(Layer l, Point pixel, ToolParams params) {
		currentPixel.setLocation(pixel);
		return new ToolResult(REPAINT);
	}
	
	@Override
	public ToolResult enter(Layer l, Point pixel, ToolParams params) {
		params.app().canvasPanel.addRenderable(this);
		return new ToolResult(REPAINT);
	}
	
	@Override
	public ToolResult exit(Layer l, Point pixel, ToolParams params) {
		params.app().canvasPanel.removeRenderable(this);
		return new ToolResult(REPAINT);
	}
	
	private final Point currentPixel = new Point();
	
	@Override
	public void renderAt(Graphics2D g, AffineTransform tf) {
		var ttf = AffineTransform.getTranslateInstance(currentPixel.x - currentSize/2, currentPixel.y - currentSize/2);
		ttf.preConcatenate(tf);
		BitMask.renderOutline(g, circleMaskCache.get(currentSize), ttf, Constants.toolOutline);
	}
}
