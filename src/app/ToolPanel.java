package app;

import static app.Constants.hpad;
import static app.Constants.pad;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import canvas.Layer;
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
import util.GBC;
import util.LabeledSlider;

public class ToolPanel extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 4242175503749643716L;
	
	public static final Color selectedColor = new Color(150, 190, 255);
	
	public ToolPanel(App app) {
		// direct interactions with app
		this.app = app;
		app.addKeyBinding("UP", new AbstractAction() {
			private static final long serialVersionUID = -4225429967635528669L;
			@Override
			public void actionPerformed(ActionEvent e) {
				int str = sizeSlider.getValue();
				sizeSlider.setValue(str + 1);
				if (currentTB.tool instanceof Renderable)
					app.canvasPanel.repaint();
			}
		});
		app.addKeyBinding("DOWN", new AbstractAction() {
			private static final long serialVersionUID = -4225429967635528669L;
			@Override
			public void actionPerformed(ActionEvent e) {
				int str = sizeSlider.getValue();
				sizeSlider.setValue(str - 1);
				if (currentTB.tool instanceof Renderable)
					app.canvasPanel.repaint();
			}
		});
		
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
		var deselectButton = new JButton("Drop");
		deselectButton.addActionListener(e -> {app.canvasPanel.dropSelection(); app.repaintCanvas(); app.saveState();});
		var dragButton = new ToolButton("Drag", new Dragger(), "D");
		
		var undoButton = new JButton("Undo");
		undoButton.addActionListener((e) -> app.undo());
		var redoButton = new JButton("Redo");
		redoButton.addActionListener((e) -> app.redo());
		
		GBC toolButtonGBC = new GBC().weight(1,0).fill(GBC.BOTH);
		int i = 0, j = 0;
		GBC.addComp(toolButtonPanel::add, i=0, j=0, pencilButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, eraserButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, markerButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, bucketButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, i=0, j=1, eyedropperButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, hueButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, warpButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, smoothButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, i=0, j=2, colorSelectButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, boxSelectButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, dragButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, deselectButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, i=0, ++j, new JSeparator(SwingConstants.HORIZONTAL), new GBC().dim(3, 1).fill(GBC.HORIZONTAL).weight(1, 0).insets(0, hpad, 0, hpad));
		GBC.addComp(toolButtonPanel::add, i=0, ++j, undoButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, redoButton, toolButtonGBC);
		
		// panels
		GBC.addComp(this::add, 0, 0, sliderPanel, new GBC().dim(2, 1).insets(pad, pad, hpad, pad).anchor(GBC.SOUTH).fill(GBC.HORIZONTAL).weight(0, 1));
		GBC.addComp(this::add, 0, 1, new JSeparator(SwingConstants.HORIZONTAL), new GBC().fill(GBC.HORIZONTAL).weight(1, 0).insets(0, hpad, 0, hpad));
		GBC.addComp(this::add, 0, 2, toolButtonPanel, new GBC().dim(2, 1).insets(hpad, pad, pad, hpad).fill(GBC.HORIZONTAL).anchor(GBC.NORTH).weight(0, 1));
		app.lockSizeAfterPack(() -> sliderPanel.setPreferredSize(sliderPanel.getSize()));
		
		// initial tool
		currentTB = pencilButton;
		currentTB.setBackground(selectedColor);
		
		// enabling
		enabler.add(undoButton::setEnabled, app::canUndo);
		enabler.add(redoButton::setEnabled, app::canRedo);
		enabler.add(deselectButton::setEnabled, app.canvasPanel::hasSelection);
	}
	
	
	// Fields
	
	private final App app;
	private ToolButton currentTB;
	
	private final LabeledSlider strengthSlider;
	private final LabeledSlider sizeSlider;
	private final JLabel strengthText = new JLabel("Alpha:");
	private final JLabel sizeText = new JLabel("Diameter:");
	
	private final Enabler enabler = new Enabler();

	
	// Methods
	
	public int getToolStrength() {
		return strengthSlider.getValue();
	}
	
	public int getToolSize() {
		return sizeSlider.getValue();
	}
	
	
	// Tool stuff
	
	
	private Tool.ToolParams getToolParams(MouseEvent e) {
		return new Tool.ToolParams(app.getCurrentColor(), app, e);
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
	

	@Override
	public void mouseDragged(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.drag(getActiveLayer(), getPointOnCanvas(e), getToolParams(e))))
				e.consume();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.move(getActiveLayer(), getPointOnCanvas(e), getToolParams(e))))
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
		if (canUseTool() && handleToolUse(currentTB.tool.exit(getActiveLayer(), getPointOnCanvas(e), getToolParams(e))))
			e.consume();
	}
	
	
	/**
	 * A button associated with a tool, e.g. the pencil button. 
	 */
	private class ToolButton extends JButton implements ActionListener {
		private static final long serialVersionUID = -1156021027317278350L;
		
		public ToolButton(String name, Tool tool, String hotkey) {
			super(name);
			this.tool = tool;
			
			this.setOpaque(true); // so it can be highlighted by color
			addActionListener(this);
			
			if (hotkey != null)
				app.addKeyBinding(hotkey, new AbstractAction() {
					private static final long serialVersionUID = -3450898524607129561L;

					@Override
					public void actionPerformed(ActionEvent e) {
						ToolButton.this.doClick();
						app.canvasPanel.repaint();
					}
				});
		}
		
		
		// Fields

		private final Tool tool;
		
		
		// Methods
		
		@SuppressWarnings("preview")
		@Override
		public void actionPerformed(ActionEvent e) {
			// jank to make pencil or marker use eyedropper's alpha when switching
			if (currentTB.tool instanceof Eyedropper && 
					(tool instanceof Pencil || tool instanceof Marker)) {
				tool.currentStrength = currentTB.tool.currentStrength;
			}
			
			if (currentTB.tool instanceof Renderable r)
				app.canvasPanel.removeRenderable(r);
			if (tool instanceof Renderable r)
				app.canvasPanel.addRenderable(r);
			
			// switch active tool button
			currentTB.setBackground(null);
			currentTB = ToolButton.this;
			setBackground(selectedColor);
			
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

	public void setAlpha(int alpha) {
		strengthSlider.setValue(alpha);
	}

}
