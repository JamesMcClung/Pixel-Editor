package tools;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import app.Constants;
import app.Renderable;
import canvas.Layer;
import canvas.PixelMask;
import util.Util;

public class BoxSelector extends Tool implements Renderable {

	public BoxSelector() {
		super();
		hasStrength = false;
		hasSize = false;
	}

	@Override
	public void renderAt(Graphics2D g, AffineTransform tf) {
		AffineTransform tfInverse = null;
		try {
			tfInverse = tf.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}

		Point2D p1 = tfInverse.transform(start, null), p2 = tfInverse.transform(end, null);

		Point topLeft = new Point(Util.floor(Math.min(p1.getX(), p2.getX())),
				Util.floor(Math.min(p1.getY(), p2.getY())));
		Point bottomRight = new Point(Util.ceil(Math.max(p1.getX(), p2.getX())),
				Util.ceil(Math.max(p1.getY(), p2.getY())));

		tf.transform(topLeft, topLeft);
		tf.transform(bottomRight, bottomRight);

		g.setColor(Constants.preselectionOutline);
		g.drawRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);

	}

	private final Point start = new Point();
	private final Point end = new Point();

	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		params.app().canvasPanel.addRenderable(this);
		start.setLocation(params.e().getPoint());
		end.setLocation(start);
		return new ToolResult(REPAINT);
	}

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		var cp = params.app().canvasPanel;
		var top = cp.getTopLayer(true);

		var startPix = cp.getPointOnLayer(start, true, true);
		var endPix = cp.getPointOnLayer(end, true, true);

		Point topLeft = Util.min(startPix, endPix);
		Point botRight = Util.max(startPix, endPix);

		PixelMask mask = new PixelMask(top.getSize());
		for (int i = 0; i < mask.getWidth(); i++)
			for (int j = 0; j < mask.getHeight(); j++)
				mask.set(i, j, topLeft.x <= i && i <= botRight.x && topLeft.y <= j && j <= botRight.y);
		cp.select(mask);

		cp.removeRenderable(this);

		return new ToolResult(REPAINT_AND_SAVE);
	}

	@Override
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		end.setLocation(params.e().getPoint());
		return new ToolResult(REPAINT);
	}

}
