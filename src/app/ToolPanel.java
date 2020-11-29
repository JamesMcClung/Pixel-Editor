package app;

import static app.Constants.hpad;
import static app.Constants.pad;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import canvas.Layer;
import myawt.GBC;
import myawt.LabeledSlider;
import myawt.MyButton;
import myawt.SimpleButton;
import tools.BoxSelector;
import tools.Bucket;
import tools.ColorSelector;
import tools.Dragger;
import tools.Eraser;
import tools.Eyedropper;
import tools.HueChanger;
import tools.Marker;
import tools.Pencil;
import tools.Smoother;
import tools.Tool;
import tools.Warper;
import util.Enabler;
import util.Util;

public class ToolPanel extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 4242175503749643716L;
	
	public static final Color selectedColor = new Color(150, 190, 255);
	
	public ToolPanel(App app) {
		this.app = app;
		setLayout(new GridBagLayout());

		// make panels
		JPanel sliderPanel = new JPanel(new GridBagLayout());
		JPanel toolButtonPanel = new JPanel(new GridBagLayout());
		
		// sliders
		strengthSlider = new LabeledSlider(0, 255, 255);
		sizeSlider = new LabeledSlider(1, 50, 1);
		// make  sure tool buttons remember their sizes and strengths
		strengthSlider.addChangeListener(e -> currentTB.tool.currentStrength= strengthSlider.getValue());
		sizeSlider.addChangeListener(e -> currentTB.tool.currentSize = sizeSlider.getValue());
		
		GBC.addComp(sliderPanel::add, 0, 0, strengthText, new GBC().anchor(GBC.EAST).weight(.5, 0));
		GBC.addComp(sliderPanel::add, 1, 0, strengthSlider.label, new GBC().insets(0, hpad, 0, hpad).weight(.25, 0));
		GBC.addComp(sliderPanel::add, 2, 0, strengthSlider, new GBC().weight(.25, 0));
		GBC.addComp(sliderPanel::add, 0, 1, sizeText, new GBC().anchor(GBC.EAST).weight(.5, 0));
		GBC.addComp(sliderPanel::add, 1, 1, sizeSlider.label, new GBC().insets(0, hpad, 0, hpad).weight(.25, 0));
		GBC.addComp(sliderPanel::add, 2, 1, sizeSlider, new GBC().weight(.25, 0));
		
		// tools
		var pencilButton = new ToolButton("Pencil", new Pencil(), "P");
		var eraserButton = new ToolButton("Eraser", new Eraser(), "E");
		var markerButton = new ToolButton("Marker", new Marker(), "M");
		var bucketButton = new ToolButton("Fill", new Bucket(), "F");
		var eyedropperButton = new ToolButton("Eye", new Eyedropper(), "I");
		var hueButton = new ToolButton("Hue", new HueChanger(), "H");
		var warpButton = new ToolButton("Warp", new Warper(), "W");
		var smoothButton = new ToolButton("Smooth", new Smoother(), "S");
		var colorSelectButton = new ToolButton("CSelect", new ColorSelector(), "C");
		var boxSelectButton = new ToolButton("BSelect", new BoxSelector(), "B");
		var dropButton = new SimpleButton("Drop", "ENTER", e -> {app.canvasPanel.dropSelection(); app.repaintCanvas(); app.saveState();});
		var dragButton = new ToolButton("Drag", new Dragger(), "D");
		var deleteButton = new SimpleButton("Delete", "BACK_SPACE", e -> {app.canvasPanel.deleteSelection(); app.repaintCanvas(); app.saveState();});
		var cutButton = new SimpleButton("Cut", null, e -> {app.canvasPanel.cut(); app.repaintCanvas(); app.saveState();});
		var copyButton = new SimpleButton("Copy", null, e -> app.canvasPanel.copy());
		var pasteButton = new SimpleButton("Paste", null, e -> {app.canvasPanel.paste(); app.repaintCanvas(); app.saveState();});
		
		var undoButton = new SimpleButton("Undo", "Z", e -> app.undo());
		var redoButton = new SimpleButton("Redo", "shift Z", e -> app.redo());
		
		GBC toolButtonGBC = new GBC().weight(1,0).fill(GBC.BOTH);
		GBC sepGBC = new GBC().dim(4, 1).fill(GBC.BOTH).weight(1, 0).insets(0, hpad, 0, hpad); // GBC for separators
		int i = 0, j = 0;
		GBC.addComp(toolButtonPanel::add, i=0, j=0, pencilButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, eraserButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, markerButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, bucketButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, i=0, ++j, eyedropperButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, hueButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, warpButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, smoothButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, i=0, ++j, new JSeparator(SwingConstants.HORIZONTAL), sepGBC);
		GBC.addComp(toolButtonPanel::add, i=0, ++j, colorSelectButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, boxSelectButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, dragButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, dropButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, i=0, ++j, deleteButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, cutButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, copyButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, pasteButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, i=0, ++j, new JSeparator(SwingConstants.HORIZONTAL), sepGBC);
		GBC.addComp(toolButtonPanel::add, i=0, ++j, undoButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, redoButton, toolButtonGBC);
		
		// display for which pixel is selected
		pixelCoordDisplay = new JLabel();
		updatePixelCoordDisplay(null);
		
		// add panels
		GBC.addComp(this::add, 0, 0, sliderPanel, new GBC().insets(pad, pad, hpad, pad).anchor(GBC.SOUTH).fill(GBC.HORIZONTAL).weight(0, 1));
		GBC.addComp(this::add, 0, 1, new JSeparator(SwingConstants.HORIZONTAL), new GBC().fill(GBC.HORIZONTAL).weight(1, 0).insets(0, hpad, 0, hpad));
		GBC.addComp(this::add, 0, 2, toolButtonPanel, new GBC().insets(hpad, pad, pad, pad).fill(GBC.HORIZONTAL).anchor(GBC.NORTH).weight(0, 1));
		GBC.addComp(this::add, 0, 3, pixelCoordDisplay, new GBC().insets(hpad, pad, pad, pad).fill(GBC.HORIZONTAL).anchor(GBC.SOUTH).weight(0, 1));
		app.lockSizeAfterPack(() -> sliderPanel.setPreferredSize(sliderPanel.getSize()));
		
		// initial tool
		currentTB = pencilButton;
		currentTB.setBackground(selectedColor);
		
		// enabling
		enabler.add(undoButton::setEnabled, app::canUndo);
		enabler.add(redoButton::setEnabled, app::canRedo);
		enabler.add(app.canvasPanel::hasSelection, dropButton::setEnabled, deleteButton::setEnabled);
		enabler.add(app.canvasPanel::hasLayer, cutButton::setEnabled, copyButton::setEnabled);
		enabler.add(app.canvasPanel::canPaste, pasteButton::setEnabled);
		
		// key bindings
		Util.addKeyBinding(this, "UP", e -> {
			int str = sizeSlider.getValue();
			sizeSlider.setValue(str + 1);
			sendMoveEvent(currentTB.tool);
		});
		Util.addKeyBinding(this, "DOWN", e -> {
			int str = sizeSlider.getValue();
			sizeSlider.setValue(str - 1);
			sendMoveEvent(currentTB.tool);
		});
	}
	
	
	// Fields
	
	private final App app;
	private ToolButton currentTB;
	
	private final LabeledSlider strengthSlider;
	private final LabeledSlider sizeSlider;
	private final JLabel strengthText = new JLabel("Alpha:");
	private final JLabel sizeText = new JLabel("Diameter:");
	private final JLabel pixelCoordDisplay;
	
	private final Enabler enabler = new Enabler();

	
	// Methods
	
	public int getToolStrength() {
		return strengthSlider.getValue();
	}
	
	public int getToolSize() {
		return sizeSlider.getValue();
	}
	
	private void updatePixelCoordDisplay(Point pixel) {
		if (pixel == null)
			pixelCoordDisplay.setText("Pixel coordinates: none");
		else
			pixelCoordDisplay.setText("Pixel coordinates: (%d, %d)".formatted(pixel.x+1, pixel.y+1));
	}
	
	
	// Tool stuff
	
	
	private Tool.ToolParams getToolParams(MouseEvent e) {
		return new Tool.ToolParams(app.getCurrentColor(), app, e);
	}
	
	private Tool.ToolParams getToolParams() {
		Point mousePos = app.canvasPanel.getMousePosition();
		if (mousePos == null)
			return null;
		MouseEvent e = new MouseEvent(app.canvasPanel, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, mousePos.x, mousePos.y, 0, false);
		return getToolParams(e);
	}
	
	private Point getPointOnCanvas(MouseEvent e) {
		return app.canvasPanel.getPointOnLayer(e.getPoint(), true, false); 
	}
	
	private Layer getActiveLayer() {
		return app.getTopLayer();
	}
	
	private boolean canUseTool() {
		return currentTB != null && getActiveLayer() != null;
	}
	
	private boolean handleToolUse(Tool.ToolResult result) {
		if (result == null)
			return false;
		switch (result.command()) {
		case Tool.REPAINT:
			app.repaintCanvas();
			break;
		case Tool.REPAINT_AND_SAVE:
			app.repaintCanvas();
		case Tool.SAVE_STATE:
			app.saveState();
		case Tool.DO_NOTHING:
		}
		app.updateEnableds();
		return true;
	}
	
	private void sendMoveEvent(Tool tool) {
		var moveParams = getToolParams();
		if (moveParams != null)
			handleToolUse(tool.move(getActiveLayer(), getPointOnCanvas(moveParams.e()), moveParams));
	}
	

	@Override
	public void mouseDragged(MouseEvent e) {
		Point pixel = getPointOnCanvas(e);
		updatePixelCoordDisplay(pixel);
		if (canUseTool() && handleToolUse(currentTB.tool.drag(getActiveLayer(), pixel, getToolParams(e))))
			e.consume();
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		Point pixel = getPointOnCanvas(e);
		updatePixelCoordDisplay(pixel);
		if (canUseTool() && handleToolUse(currentTB.tool.move(getActiveLayer(), pixel, getToolParams(e))))
			e.consume();
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.click(getActiveLayer(), getPointOnCanvas(e), getToolParams(e))))
			e.consume();
	}
	@Override
	public void mousePressed(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.press(getActiveLayer(), getPointOnCanvas(e), getToolParams(e))))
			e.consume();
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.release(getActiveLayer(), getPointOnCanvas(e), getToolParams(e))))
			e.consume();
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.enter(getActiveLayer(), getPointOnCanvas(e), getToolParams(e))))
			e.consume();
	}
	@Override
	public void mouseExited(MouseEvent e) { 
		updatePixelCoordDisplay(null);
		if (canUseTool() && handleToolUse(currentTB.tool.exit(getActiveLayer(), getPointOnCanvas(e), getToolParams(e))))
			e.consume();
	}
	
	public static final int NO_NEXT_ALPHA = -1;
	private int nextAlpha = NO_NEXT_ALPHA;
	public void setNextAlpha(int alpha) {
		nextAlpha = alpha;
	}
	private boolean hasNextAlpha() {
		return nextAlpha != NO_NEXT_ALPHA;
	}
	
	
	/**
	 * A button associated with a tool, e.g. the pencil button. 
	 */
	private class ToolButton extends MyButton {
		private static final long serialVersionUID = -1156021027317278350L;
		
		public ToolButton(String name, Tool tool, String hotkey) {
			super(name, hotkey);
			this.tool = tool;
			
			this.setOpaque(true); // so it can be highlighted by color
		}
		
		
		// Fields

		private final Tool tool;
		
		
		// Methods
		
		private void makeCurrentTB() {
			currentTB.setBackground(null);
			currentTB = this;
			setBackground(selectedColor);
		}
		
		@SuppressWarnings("preview")
		@Override
		public void actionPerformed(ActionEvent e) {
			ToolButton lastTB = currentTB;
			
			// deal with situation where tool switches while over canvas panel
			var moveParams = getToolParams();
			Point p;
			if (moveParams != null && (p = getPointOnCanvas(moveParams.e())) != null) {
				Layer active = getActiveLayer();
				handleToolUse(currentTB.tool.exit(active, p, moveParams));
				
				makeCurrentTB();
				
				handleToolUse(tool.enter(active, p, moveParams));
				handleToolUse(tool.move(active, p, moveParams));
			} else {
				makeCurrentTB();
			}
			
			if (tool instanceof Eyedropper eye) {
				int ai = lastTB.tool.hasAlpha() ? lastTB.tool.currentStrength : NO_NEXT_ALPHA;
				eye.initialize(ai, app.getCurrentColor());
			}
			if (hasNextAlpha() && tool.hasAlpha()) {
				tool.currentStrength = nextAlpha;
				nextAlpha = NO_NEXT_ALPHA;
			}
				
			
			// reconfigure sliders
			int size = tool.currentSize;
			sizeSlider.setMinimum(tool.minSize);
			sizeSlider.setMaximum(tool.maxSize);
			sizeSlider.setValue(size);
			sizeText.setText(tool.sizeName + ":");
			
			sizeSlider.setVisible(tool.hasSize);
			sizeText.setVisible(tool.hasSize);
			
			sizeSlider.setEnabled(tool.enableSize);
			sizeText.setEnabled(tool.enableSize);
			
			int strength = tool.currentStrength;
			strengthSlider.setMinimum(tool.minStrength);
			strengthSlider.setMaximum(tool.maxStrength);
			strengthSlider.setValue(strength);
			strengthText.setText(tool.strengthName + ":");
			
			strengthSlider.setVisible(tool.hasStrength);
			strengthText.setVisible(tool.hasStrength);
			
			strengthSlider.setEnabled(tool.enableStrength);
			strengthText.setEnabled(tool.enableStrength);
		}
	}



	public void updateEnableds() {
		enabler.updateEnableds();
	}

	public void setStrength(int str) {
		strengthSlider.setValue(str);
	}

}
