package tools;

import java.awt.Color;
import java.awt.Point;

import canvas.Layer;

public class Eyedropper implements Tool {
	
	private void pickColor(Layer l, Point pixel, ToolParams params) {
		Color c = new Color(l.getImage().getRGB(pixel.x, pixel.y), true);
		params.app().colorPanel.setCurrentColor(c);
		params.app().toolPanel.setAlpha(c.getAlpha());
		initialAlpha = c.getAlpha();
	}
	
	private void previewColor(Layer l, Point pixel, ToolParams params) {
		if (initialPreviewColor == null)
			initialPreviewColor = params.app().colorPanel.getPreviewColor();
		if (initialAlpha == -1)
			initialAlpha = params.alpha();
		Color c = new Color(l.getImage().getRGB(pixel.x, pixel.y), true);
		params.app().colorPanel.setPreviewColor(c);
		params.app().toolPanel.setAlpha(c.getAlpha());
	}

	@Override
	public ToolResult click(Layer l, Point pixel, ToolParams params) {
		return null;
	}

	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		return null;
	}

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		pickColor(l, pixel, params);
		return null;
	}

	@Override
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		previewColor(l, pixel, params);
		return null;
	}

	@Override
	public ToolResult move(Layer l, Point pixel, ToolParams params) {
		previewColor(l, pixel, params);
		return null;
	}
	
	private Color initialPreviewColor = null;
	private int initialAlpha = -1;

	@Override
	public ToolResult enter(Layer l, Point pixel, ToolParams params) {
		initialPreviewColor = params.app().colorPanel.getPreviewColor();
		initialAlpha = params.alpha();
		return null;
	}

	@Override
	public ToolResult exit(Layer l, Point pixel, ToolParams params) {
		params.app().colorPanel.setPreviewColor(initialPreviewColor);
		params.app().toolPanel.setAlpha(initialAlpha);
		initialPreviewColor = null;
		initialAlpha = -1;
		return null;
	}

}
