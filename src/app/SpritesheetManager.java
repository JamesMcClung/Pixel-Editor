package app;

import static app.Constants.hpad;
import static app.Constants.pad;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import canvas.Eraser;
import canvas.Layer;
import canvas.Spritesheet;
import util.GBC;

public class SpritesheetManager extends JPanel {
	private static final long serialVersionUID = 6720331984243667952L;
	
	public SpritesheetManager(App app) {
		this.app = app;
		
		setLayout(new GridBagLayout());
		
		// preview panel
		previewPanel = new PreviewPanel();
		
		// Buttons
		var buttonPanel = new JPanel(new GridBagLayout());
		var lbutton = new JButton("<");
		lbutton.addActionListener((e) -> app.viewSprite(currentSheet.getSpriteRelative(-1)));
		
		var rbutton = new JButton(">");
		rbutton.addActionListener((e) -> app.viewSprite(currentSheet.getSpriteRelative(1)));
		
		var clearButton = new JButton("Clear");
		clearButton.addActionListener((e) -> clearSprite(currentSheet.getActiveSpriteIndex()));
		
		GBC.addComp(buttonPanel::add, 0, 0, lbutton, new GBC().insets(pad, hpad, hpad, hpad));
		GBC.addComp(buttonPanel::add, 1, 0, rbutton, new GBC().insets(pad, hpad, hpad, hpad));
		GBC.addComp(buttonPanel::add, 2, 0, clearButton, new GBC().insets(pad, hpad, hpad, hpad));
		
		// Text fields
		var textPanel = new JPanel(new GridBagLayout());
		int ncols = 2;
		ActionListener al = (e) -> {
			Dimension dim = getTextSpriteDim();
			if (adjustSpriteDim(dim))
				setTextSpriteDim(dim);
			setSpriteDim(currentSheet, dim);
		};
		
		spriteWidthField = new JTextField(ncols);
		spriteWidthField.addActionListener(al);
		
		spriteHeightField = new JTextField(ncols);
		spriteHeightField.addActionListener(al);
		
		GBC.addComp(textPanel::add, 0, 0, new JLabel("Sprite Dimensions:"), new GBC().insets(hpad, hpad, pad, 0));
		GBC.addComp(textPanel::add, 1, 0, spriteWidthField, new GBC().anchor(GBC.EAST).insets(hpad, 0, pad, 0));
		GBC.addComp(textPanel::add, 2, 0, new JLabel("x"), new GBC().insets(hpad, 0, hpad, 0));
		GBC.addComp(textPanel::add, 3, 0, spriteHeightField, new GBC().anchor(GBC.WEST).insets(hpad, 0, pad, hpad));
		
		// Panels
		GBC.addComp(this::add, 0, 0, previewPanel, new GBC().dim(1, 2).fill(GBC.BOTH).weight(1, 1), BorderFactory.createLoweredSoftBevelBorder());
		GBC.addComp(this::add, 1, 0, buttonPanel, new GBC().fill(GBC.BOTH));
		GBC.addComp(this::add, 1, 1, textPanel, new GBC().fill(GBC.BOTH));
		
		dependentComps.add(spriteWidthField);
		dependentComps.add(spriteHeightField);
		dependentComps.add(lbutton);
		dependentComps.add(rbutton);
		dependentComps.add(clearButton);
		
		updateComponentStates();
	}
	
	private final App app;
	
	private final List<Spritesheet> openSheets = new ArrayList<>();
	private Spritesheet currentSheet = null;
	
	private final JPanel previewPanel;
	private final JTextField spriteWidthField, spriteHeightField;
	private final List<JComponent> dependentComps = new ArrayList<>(); // components that are enabled/disabled whether or not a spritesheet is present 
	
	
	public void clearSprite(Point spriteIndex) { // TODO this should be in Layer
		Layer sprite = currentSheet.getSprite(spriteIndex);
		Dimension spriteDim = currentSheet.getSpriteDim();
		Point pixel = new Point();
		for (pixel.x = 0; pixel.x < spriteDim.width; pixel.x++)
			for (pixel.y = 0; pixel.y < spriteDim.height; pixel.y++)
				sprite.setPixel(pixel, Eraser.eraseColor);
		app.repaintCanvas();
	}
	
	/**
	 * Parses and returns the user's entries in the text fields specifiying sprite dimensions.
	 * Returns (-1, -1) if either entry is not an integer.
	 * @return the dimensions
	 */
	private Dimension getTextSpriteDim() {
		Dimension dim = new Dimension();
		try {
			dim.width = Integer.parseInt(spriteWidthField.getText());
			dim.height = Integer.parseInt(spriteHeightField.getText());
			return dim;
		} catch (NumberFormatException e) {
			return new Dimension(-1, -1);
		}
	}
	
	/**
	 * Modiifes the given dimension to be a valid sprite dimension for the current spritesheet.
	 * @param dim a dimension
	 * @return whether the dimension was adjusted
	 */
	private boolean adjustSpriteDim(Dimension dim) {
		Dimension adjDim = new Dimension(dim);
		adjDim.width = Math.min(Math.max(dim.width, 1), currentSheet.getImage().getWidth());
		adjDim.height = Math.min(Math.max(dim.height, 1), currentSheet.getImage().getHeight());
		boolean adjusted = !adjDim.equals(dim);
		dim.setSize(adjDim);
		return adjusted;
	}
	
	/**
	 * Sets the text fields specifying sprite dimensions to the given dimension
	 * @param d the dimension
	 */
	private void setTextSpriteDim(Dimension d) {
		spriteWidthField.setText("" + d.width);
		spriteHeightField.setText("" + d.height);
	}
	
	/**
	 * Sets the sprite dimensions of the given spreadsheet. Refreshes screen if necessary.
	 * @param s spreadsheet
	 * @param dim dimension, in pixels
	 */
	private void setSpriteDim(Spritesheet s, Dimension dim) {
		s.setSpriteDim(dim);
		if (s.equals(currentSheet))
			app.viewSprite(currentSheet.getSprite());
	}
	
	/**
	 * Updates the manager's fields etc. to align with the given spritesheet being active.
	 * DOES NOT update any other app components.
	 * @param s given spritesheet.
	 */
	public void setCurrentSheet(Spritesheet s) {
		currentSheet = s;
		if (!openSheets.contains(s))
			openSheets.add(s);
		
		updateComponentStates();
		
		// update text entries
		setTextSpriteDim(s.getSpriteDim());
	}
	
	public Spritesheet getCurrentSheet() {
		return currentSheet;
	}
	
	/**
	 * Enables or disables scroll buttons, text fields, etc. depending on presence of currentSheet
	 */
	private void updateComponentStates() {
		boolean enabled = currentSheet != null;
		for (JComponent comp : dependentComps)
			comp.setEnabled(enabled);
	}
	
	/**
	 * Repaints the preview. Call this e.g. after editing the sprite.
	 */
	public void repaintPreview() {
		previewPanel.repaint();
	}
	
	
	
	private class PreviewPanel extends JPanel implements MouseListener {
		private static final long serialVersionUID = 8611113602166572032L;
		
		public PreviewPanel() {
			addMouseListener(this);
		}
		
		private Color highlightColor = new Color(200, 170, 0, 64);
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			if (currentSheet == null)
				return;
			
			var g2 = (Graphics2D) g;
			Dimension size = getSize();
			Point loc = new Point();
			
			// render sheet
			currentSheet.renderAt(g2, loc, size, Layer.RENDER_WHITE);
			currentSheet.renderSpriteHighlight(g2, loc, size, highlightColor);
			currentSheet.drawBoundingBox(g2, loc, size);
		}


		@Override
		public void mouseReleased(MouseEvent e) {
			// Sets the current sprite to the clicked sprite
			if (currentSheet == null)
				return;
			
			// find pixel on spritesheet
			var tf = currentSheet.getTransform(new Point(), getSize());
			Point pixel = e.getPoint();
			try {
				tf.inverseTransform(pixel, pixel);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			Dimension spriteDim = currentSheet.getSpriteDim();
			Point spriteIndex = new Point(pixel.x / spriteDim.width, pixel.y / spriteDim.height);
			app.viewSprite(currentSheet.getSprite(spriteIndex));
		}

		@Override
		public void mouseClicked(MouseEvent e) { }
		@Override
		public void mousePressed(MouseEvent e) { }
		@Override
		public void mouseEntered(MouseEvent e) { }
		@Override
		public void mouseExited(MouseEvent e) { }
	}



	public void restoreState(SaveableState state) {
		if (state == null)
			return;
		setCurrentSheet(state.ss());
		currentSheet.setSpriteIndex(state.spriteIndex());
		Dimension spriteSize = state.layers()[0].getSize();
		currentSheet.setSpriteDim(spriteSize);
		setTextSpriteDim(spriteSize);
		repaintPreview();
	}

}
