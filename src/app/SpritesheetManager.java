package app;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;

import canvas.Layer;
import canvas.Spritesheet;
import myawt.GBC;
import util.Enabler;

public class SpritesheetManager extends JPanel {
	private static final long serialVersionUID = 6720331984243667952L;
	
	/**
	 * Sprites per second when playing animation
	 */
	private static final int initialFPS = 4;
	
	public SpritesheetManager(App app) {
		this.app = app;
		
		setLayout(new GridBagLayout());
		
		playTimer = new Timer(1000/initialFPS, e -> nextSprite());
		
		// preview panel
		previewPanel = new PreviewPanel();
		
		// Buttons
		var buttonPanel = new JPanel(new GridBagLayout());
		
		playButton = new JButton("Play");
		playButton.addActionListener(e -> togglePlay());
		
		var fpsSlider = new JSlider(1, 20, initialFPS);
		fpsSlider.setMinorTickSpacing(1);
		fpsSlider.setPaintTicks(true);
		fpsSlider.setSnapToTicks(true);
		fpsSlider.setFocusable(false);
		fpsSlider.addChangeListener(e -> playTimer.setDelay(1000/fpsSlider.getValue()));
		fpsSlider.setPreferredSize(playButton.getPreferredSize());
		
		var lbutton = new JButton("<");
		lbutton.addActionListener(e -> prevSprite());
		var rbutton = new JButton(">");
		rbutton.addActionListener(e -> nextSprite());
		
		var clearButton = new JButton("Clear");
		clearButton.addActionListener((e) -> clearSprite());
		
		
		int pad = -Constants.pad/2 *0; // tinker with this
		int hpad = pad/2;
		GBC.addComp(buttonPanel::add, 0, 0, lbutton, new GBC().insets(pad, hpad, hpad, hpad));
		GBC.addComp(buttonPanel::add, 1, 0, rbutton, new GBC().insets(hpad, hpad, hpad, hpad));
		GBC.addComp(buttonPanel::add, 2, 0, playButton, new GBC().insets(hpad, hpad, hpad, hpad));
		GBC.addComp(buttonPanel::add, 3, 0, fpsSlider, new GBC().insets(hpad, hpad, hpad, pad));
		GBC.addComp(buttonPanel::add, 1, 1, clearButton, new GBC().insets(pad, hpad, hpad, pad));
		
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
		
		// enabling management 
		Enabler.Condition hasSheet = () -> currentSheet != null;
		Enabler.Condition isNotPlaying = () -> !playTimer.isRunning();
		enabler.add(hasSheet,
						spriteWidthField::setEnabled,
						spriteHeightField::setEnabled,
						lbutton::setEnabled,
						rbutton::setEnabled,
						playButton::setEnabled,
						clearButton::setEnabled
				);
		enabler.add(new Enabler.Enableable[] {
						lbutton::setEnabled,
						rbutton::setEnabled
				}, new Enabler.Condition[] {hasSheet, isNotPlaying});
		
		updateEnableds();
	}
	
	
	// Fields
	
	private final App app;
	private final Enabler enabler = new Enabler();
	
	private final List<Spritesheet> openSheets = new ArrayList<>();
	private Spritesheet currentSheet = null;
	
	private final JButton playButton;
	private final Timer playTimer;
	
	private final JPanel previewPanel;
	private final JTextField spriteWidthField, spriteHeightField;
	
	
	// Methods
	
	public void clearSprite() {
		currentSheet.getCurrentSprite().clearImage();
		app.saveState();
		app.repaintCanvas();
	}
	
	private void togglePlay() {
		if (playTimer.isRunning()) {
			playTimer.stop();
			playButton.setText("Play");
		} else {
			playTimer.start();
			playButton.setText("Stop");
		}
		updateEnableds();
	}
	
	private void nextSprite() {
		app.viewSprite(currentSheet.moveSpriteRelative(1));
	}
	private void prevSprite() {
		app.viewSprite(currentSheet.moveSpriteRelative(-1));
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
	
	int getDelay() {
		return playTimer.getDelay();
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
			app.viewSprite(currentSheet.getCurrentSprite());
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
		
		updateEnableds();
		
		// update text entries
		setTextSpriteDim(s.getSpriteDim());
	}
	
	public Spritesheet getCurrentSheet() {
		return currentSheet;
	}
	
	/**
	 * Enables or disables scroll buttons, text fields, etc.
	 */
	private void updateEnableds() {
		enabler.updateEnableds();
	}
	
	/**
	 * Repaints the preview. Call this e.g. after editing the sprite.
	 */
	public void repaintPreview() {
		previewPanel.repaint();
	}
	
	
	
	private class PreviewPanel extends JPanel implements MouseListener, SizeLockable {
		private static final long serialVersionUID = 8611113602166572032L;
		
		public PreviewPanel() {
			addMouseListener(this);
			app.lockSizeAfterPack(this);
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
			app.viewSprite(currentSheet.moveSprite(spriteIndex));
		}

		@Override
		public void mouseClicked(MouseEvent e) { }
		@Override
		public void mousePressed(MouseEvent e) { }
		@Override
		public void mouseEntered(MouseEvent e) { }
		@Override
		public void mouseExited(MouseEvent e) { }


		@Override
		public void lockSize() {
			// make it a square
			var size = getSize();
			int length = Math.min(size.width, size.height);
			setMinimumSize(new Dimension(length, length));
		}
	}



	public void restoreState(State state) {
		if (state == null)
			return;
		setCurrentSheet(state.spritesheet());
		currentSheet.setSpriteIndex(state.spriteIndex());
		Dimension spriteSize = state.spriteSize();
		currentSheet.setSpriteDim(spriteSize);
		setTextSpriteDim(spriteSize);
		repaintPreview();
	}
	
	public State getState() {
		return new State(currentSheet, currentSheet.getActiveSpriteIndex(), currentSheet.getSpriteDim());
	}
	
	@SuppressWarnings("preview")
	public static record State(Spritesheet spritesheet, Point spriteIndex, Dimension spriteSize) { }

}
