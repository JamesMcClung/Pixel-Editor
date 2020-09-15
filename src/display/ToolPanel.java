package display;

import static display.Constants.hpad;
import static display.Constants.pad;

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

import canvas.Eraser;
import canvas.Layer;
import canvas.Marker;
import canvas.Pencil;
import canvas.Tool;
import canvas.ToolParams;
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
		
		GBC.addComp(sliderPanel::add, 0, 0, new JLabel("Alpha:"), new GBC().anchor(GBC.EAST).insets(0, pad, 0, hpad));
		GBC.addComp(sliderPanel::add, 1, 0, alphaSlider.label, new GBC().insets(0, hpad, 0, hpad));
		GBC.addComp(sliderPanel::add, 2, 0, alphaSlider, new GBC());
		GBC.addComp(sliderPanel::add, 0, 1, new JLabel("Size:"), new GBC().anchor(GBC.EAST).insets(0, hpad, 0, hpad));
		GBC.addComp(sliderPanel::add, 1, 1, diamSlider.label, new GBC().insets(0, hpad, 0, hpad));
		GBC.addComp(sliderPanel::add, 2, 1, diamSlider, new GBC());
		
		// tools
		JButton pencilButton = new ToolButton("Pencil", new Pencil());
		JButton eraserButton = new ToolButton("Eraser", new Eraser());
		JButton markerButton = new ToolButton("Marker", new Marker());
		
		undoButton = new JButton("Undo");
		undoButton.addActionListener((e) -> app.undo());
		redoButton = new JButton("Redo");
		redoButton.addActionListener((e) -> app.redo());
		
		GBC toolButtonGBC = new GBC().weight(1,0).fill(GBC.BOTH).insets(hpad, hpad, hpad, hpad);
		GBC.addComp(toolButtonPanel::add, 0, 0, pencilButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, 1, 0, eraserButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, 2, 0, markerButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, 0, 1, new JSeparator(SwingConstants.HORIZONTAL), new GBC().dim(3, 1).fill(GBC.HORIZONTAL).weight(1, 0).insets(0, hpad, 0, hpad));
		GBC.addComp(toolButtonPanel::add, 0, 2, undoButton, toolButtonGBC);
		GBC.addComp(toolButtonPanel::add, 1, 2, redoButton, toolButtonGBC);
		
		// panels
		GBC.addComp(this::add, 0, 0, sliderPanel, new GBC().dim(2, 1).insets(pad, pad, hpad, pad).fill(GBC.BOTH));
		GBC.addComp(this::add, 0, 1, new JSeparator(SwingConstants.HORIZONTAL), new GBC().fill(GBC.HORIZONTAL).weight(1, 0).insets(0, hpad, 0, hpad));
		GBC.addComp(this::add, 0, 2, toolButtonPanel, new GBC().dim(2, 1).insets(hpad, pad, pad, hpad).fill(GBC.BOTH));
	}
	
	private final App app;
	private Tool currentTool;
	
	private final LabeledSlider alphaSlider;
	private final LabeledSlider diamSlider;
	
	private final JButton undoButton, redoButton; 
	
	public int getAlpha() {
		return alphaSlider.getValue();
	}
	
	public int getDiam() {
		return diamSlider.getValue();
	}
	
	
	private ToolParams getToolParams() {
		return new ToolParams(app.getCurrentColor(), getAlpha(), getDiam());
	}
	
	private Point getPointOnCanvas(MouseEvent e) {
		return app.getPointOnCanvas(e.getPoint());
	}
	
	private Layer getCurrentLayer() {
		return app.getCurrentLayer();
	}
	
	private boolean canUseTool() {
		return currentTool != null && getCurrentLayer() != null;
	}
	
	private void handleToolUse(int command) {
		switch (command) {
		case Tool.REPAINT:
			app.repaintCanvas();
			break;
		case Tool.REPAINT_AND_SAVE:
			app.repaintCanvas();
		case Tool.SAVE_STATE:
			app.saveState();
			break;
		case Tool.DO_NOTHING:
			break;
		}
	}
	

	@Override
	public void mouseDragged(MouseEvent e) {
		if (canUseTool())
			handleToolUse(currentTool.drag(getCurrentLayer(), getPointOnCanvas(e), getToolParams()));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (canUseTool())
			handleToolUse(currentTool.move(getCurrentLayer(), getPointOnCanvas(e), getToolParams()));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (canUseTool())
			handleToolUse(currentTool.click(getCurrentLayer(), getPointOnCanvas(e), getToolParams()));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (canUseTool())
			handleToolUse(currentTool.press(getCurrentLayer(), getPointOnCanvas(e), getToolParams()));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (canUseTool())
			handleToolUse(currentTool.release(getCurrentLayer(), getPointOnCanvas(e), getToolParams()));
	}
	
	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }
	
	
	
	private class ToolButton extends JButton {
		private static final long serialVersionUID = -1156021027317278350L;

		public ToolButton(String name, Tool tool) {
			super(name);
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					currentTool = tool;
				}
			});
		}
		
	}



	public void updateEnableds() {
		undoButton.setEnabled(app.canUndo());
		redoButton.setEnabled(app.canRedo());
	}

}
