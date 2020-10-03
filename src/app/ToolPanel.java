package app;

import static app.Constants.hpad;
import static app.Constants.pad;

import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

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
import tools.Marker;
import tools.Pencil;
import tools.Tool;
import util.Enabler;
import util.GBC;
import util.LabeledSlider;

public class ToolPanel extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 4242175503749643716L;
	
	public ToolPanel(App app) {
		this.app = app;
		setLayout(new GridBagLayout());

		// make panels
		JPanel sliderPanel = new JPanel(new GridBagLayout());
		JPanel toolButtonPanel = new JPanel(new GridBagLayout());
		
		// sliders
		alphaSlider = new LabeledSlider(0, 255, 255);
		diamSlider = new LabeledSlider(1, 50, 1);
		diamSlider.addChangeListener(e -> currentTB.size = diamSlider.getValue());
		
		GBC.addComp(sliderPanel::add, 0, 0, new JLabel("Alpha:"), new GBC().anchor(GBC.EAST).insets(0, pad, 0, hpad));
		GBC.addComp(sliderPanel::add, 1, 0, alphaSlider.label, new GBC().insets(0, hpad, 0, hpad));
		GBC.addComp(sliderPanel::add, 2, 0, alphaSlider, new GBC());
		GBC.addComp(sliderPanel::add, 0, 1, new JLabel("Size:"), new GBC().anchor(GBC.EAST).insets(0, hpad, 0, hpad));
		GBC.addComp(sliderPanel::add, 1, 1, diamSlider.label, new GBC().insets(0, hpad, 0, hpad));
		GBC.addComp(sliderPanel::add, 2, 1, diamSlider, new GBC());
		
		// tools
		var pencilButton = new ToolButton("Pencil", new Pencil());
		var eraserButton = new ToolButton("Eraser", new Eraser());
		var markerButton = new ToolButton("Marker", new Marker());
		var bucketButton = new ToolButton("Fill", new Bucket(), 2, 50, 2);
		var eyedropperButton = new ToolButton("Eye", new Eyedropper());
		var colorSelectButton = new ToolButton("CSelect", new ColorSelector(), 2, 50, 2);
		var boxSelectButton = new ToolButton("BSelect", new BoxSelector(), 2, 50, 2);
		var deselectButton = new JButton("Drop");
		deselectButton.addActionListener(e -> {app.canvasPanel.dropSelection(); app.repaintCanvas(); app.saveState();});
		var dragButton = new ToolButton("Drag", new Dragger());
		
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
		GBC.addComp(toolButtonPanel::add, i=0, j=2, colorSelectButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, boxSelectButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, dragButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, deselectButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, i=0, ++j, new JSeparator(SwingConstants.HORIZONTAL), new GBC().dim(3, 1).fill(GBC.HORIZONTAL).weight(1, 0).insets(0, hpad, 0, hpad));
		GBC.addComp(toolButtonPanel::add, i=0, ++j, undoButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, ++i, j, redoButton, toolButtonGBC);
		
		// panels
		GBC.addComp(this::add, 0, 0, sliderPanel, new GBC().dim(2, 1).insets(pad, pad, hpad, pad).fill(GBC.BOTH));
		GBC.addComp(this::add, 0, 1, new JSeparator(SwingConstants.HORIZONTAL), new GBC().fill(GBC.HORIZONTAL).weight(1, 0).insets(0, hpad, 0, hpad));
		GBC.addComp(this::add, 0, 2, toolButtonPanel, new GBC().dim(2, 1).insets(hpad, pad, pad, hpad).fill(GBC.BOTH));
		
		// initial tool
		currentTB = pencilButton;
		
		// enabling
		enabler.add(undoButton::setEnabled, app::canUndo);
		enabler.add(redoButton::setEnabled, app::canRedo);
		enabler.add(deselectButton::setEnabled, app.canvasPanel::hasSelection);
	}
	
	
	// Fields
	
	private final App app;
	private ToolButton currentTB;
	
	private final LabeledSlider alphaSlider;
	private final LabeledSlider diamSlider;
	
	private final Enabler enabler = new Enabler();

	
	// Methods
	
	public int getAlpha() {
		return alphaSlider.getValue();
	}
	
	public int getDiam() {
		return diamSlider.getValue();
	}
	
	
	private Tool.ToolParams getToolParams(MouseEvent e) {
		return new Tool.ToolParams(app.getCurrentColor(), getAlpha(), getDiam(), app, e);
	}
	
	private Point getPointOnCanvas(MouseEvent e) {
		return app.canvasPanel.getPointOnLayer(e.getPoint(), true, false); 
	}
	
	private Layer getCurrentLayer() {
		return app.getTopLayer();
	}
	
	private boolean canUseTool() {
		return currentTB != null && getCurrentLayer() != null;
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
		if (canUseTool() && handleToolUse(currentTB.tool.drag(getCurrentLayer(), getPointOnCanvas(e), getToolParams(e))))
				e.consume();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.move(getCurrentLayer(), getPointOnCanvas(e), getToolParams(e))))
				e.consume();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.click(getCurrentLayer(), getPointOnCanvas(e), getToolParams(e))))
				e.consume();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.press(getCurrentLayer(), getPointOnCanvas(e), getToolParams(e))))
			e.consume();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.release(getCurrentLayer(), getPointOnCanvas(e), getToolParams(e))))
			e.consume();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		if (canUseTool() && handleToolUse(currentTB.tool.enter(getCurrentLayer(), getPointOnCanvas(e), getToolParams(e))))
			e.consume();
	}

	@Override
	public void mouseExited(MouseEvent e) { 
		if (canUseTool() && handleToolUse(currentTB.tool.exit(getCurrentLayer(), getPointOnCanvas(e), getToolParams(e))))
			e.consume();
	}
	
	
	
	private class ToolButton extends JButton {
		private static final long serialVersionUID = -1156021027317278350L;

		public ToolButton(String name, Tool tool) {
			this(name, tool, 1, 50, 1);
		}
		public ToolButton(String name, Tool tool, int minSize, int maxSize, int initialSize) {
			super(name);
			this.tool = tool;
			size = initialSize;
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					currentTB = ToolButton.this;
					diamSlider.setMinimum(minSize);
					diamSlider.setMaximum(maxSize);
					diamSlider.setValue(size);
				}
			});
		}
		
		private final Tool tool;
		private int size;
	}



	public void updateEnableds() {
		enabler.updateEnableds();
	}

	public void setAlpha(int alpha) {
		alphaSlider.setValue(alpha);
	}

}
