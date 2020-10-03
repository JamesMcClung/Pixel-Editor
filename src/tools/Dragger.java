package tools;

import java.awt.Point;

import canvas.Layer;
import util.Util;

public class Dragger implements Tool {

	@Override
	public ToolResult click(Layer l, Point pixel, ToolParams params) {
		if (params.app().canvasPanel.hasSelection()) {
			params.app().canvasPanel.dropSelection();
			return new ToolResult(Tool.REPAINT_AND_SAVE);
		} else
			return null;
	}
	
	private final Point initialMouseLoc = new Point();
	private final Point initialSelectionLoc = new Point();
	private boolean madeSelection = false;  

	@Override
	public ToolResult press(Layer l, Point pixel, ToolParams params) {
		initialMouseLoc.setLocation(params.e().getPoint());
		madeSelection = !params.app().canvasPanel.hasSelection();
		if (madeSelection) // select entire sprite by default
			params.app().canvasPanel.select(null);
		initialSelectionLoc.setLocation(params.app().canvasPanel.getSelectionLoc());
		return new ToolResult(Tool.REPAINT);
	}

	@Override
	public ToolResult release(Layer l, Point pixel, ToolParams params) {
		if (madeSelection)
			params.app().canvasPanel.dropSelection();
		else
			params.app().canvasPanel.snapSelectionToGrid();
		return new ToolResult(Tool.REPAINT_AND_SAVE);
	}

	@Override
	public ToolResult drag(Layer l, Point pixel, ToolParams params) {
		params.app().canvasPanel.setSelectionLoc(Util.sum(Util.difference(params.e().getPoint(), initialMouseLoc), initialSelectionLoc));
		return new ToolResult(Tool.REPAINT);
	}

	@Override
	public ToolResult move(Layer l, Point pixel, ToolParams params) {
		return null;
	}

	@Override
	public ToolResult enter(Layer l, Point pixel, ToolParams params) {
		return null;
	}

	@Override
	public ToolResult exit(Layer l, Point pixel, ToolParams params) {
		return null;
	}

}
