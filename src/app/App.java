package app;

import static app.Constants.hpad;
import static app.Constants.pad;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import canvas.Layer;
import canvas.Spritesheet;
import io.IOUtil;
import io.Memory;
import myawt.GBC;
import util.Enabler;
import util.StateLog;
import util.Util;

public class App {

	// constants

	public static final int width = 600, height = 500;
	public static final String title = "Pixel Editor";

	public static void main(String[] args) {
		new App();
	}

	// constructors

	public App() {
		frame = new JFrame(title);
		frame.setLayout(new GridBagLayout());

		// prevent resizing to below minimum size
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension new_size = frame.getSize();

				Dimension min_size = frame.getMinimumSize();
				new_size.width = Math.max(new_size.width, min_size.width);
				new_size.height = Math.max(new_size.height, min_size.height);

				frame.setSize(new_size);
			}
		});

		// handle closing the program
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});

		statelog = new StateLog<>(maxNumStates);

		// panels
		spritesheetManager = new SpritesheetManager(this);
		canvasPanel = new CanvasPanel();
		colorPanel = new ColorPanel();
		toolPanel = new ToolPanel(this);

		// menu bar
		menuBar = new MenuBar();
		frame.setJMenuBar(menuBar);

		GBC.addComp(frame::add, 0, 0, spritesheetManager,
				new GBC().dim(2, 1).weight(1, 0).fill(GBC.BOTH).insets(pad, hpad, hpad, 0),
				BorderFactory.createRaisedSoftBevelBorder());
		GBC.addComp(frame::add, 2, 0, colorPanel, new GBC().fill(GBC.BOTH).insets(pad, 0, hpad, hpad),
				BorderFactory.createRaisedSoftBevelBorder());
		GBC.addComp(frame::add, 0, 1, toolPanel, new GBC().fill(GBC.BOTH).insets(hpad, pad, pad, hpad),
				BorderFactory.createRaisedSoftBevelBorder());
		GBC.addComp(frame::add, 1, 1, canvasPanel,
				new GBC().dim(2, 1).weight(1, 1).insets(hpad, hpad, pad, pad).fill(GBC.BOTH),
				BorderFactory.createLoweredSoftBevelBorder());

		// add listeners
		canvasPanel.addMouseListener(toolPanel);
		canvasPanel.addMouseMotionListener(toolPanel);

		// update various states once everything has been initialized
		updateEnableds();

		frame.pack();
		for (var comp : compsToLockSizeOfAfterPacking)
			comp.lockSize();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	// fields

	private final JFrame frame;
	private final MenuBar menuBar;
	public final CanvasPanel canvasPanel;
	public final SpritesheetManager spritesheetManager;
	public final ColorPanel colorPanel;
	public final ToolPanel toolPanel;

	private final ArrayList<SizeLockable> compsToLockSizeOfAfterPacking = new ArrayList<>(); // only used after
																								// frame.pack() is
																								// called in constructor

	// Methods
	void lockSizeAfterPack(SizeLockable comp) {
		compsToLockSizeOfAfterPacking.add(comp);
	}

	// update enabledness
	public void updateEnableds() {
		menuBar.enabler.updateEnableds();
		toolPanel.updateEnableds();
	}

	// State Saving
	public static final int maxNumStates = 100;
	private final StateLog<SaveableState> statelog;
	private SaveableState transientState = null; // not saved to the log unless an edit is made; represents the initial
													// state of a layer before edits are made

	public boolean canUndo() {
		return statelog.canUndo();
	}

	public boolean canRedo() {
		return statelog.canRedo();
	}

	public void undo() {
		restoreState(statelog.undo());
	}

	public void redo() {
		restoreState(statelog.redo());
	}

	/**
	 * Saves the current state of the layer in the changelog. Undo can then recover the state.
	 */
	public void saveState() {
		if (transientState != null) {
			statelog.saveState(transientState);
			transientState = null;
		}
		statelog.saveState(getState(false));
		updateEnableds();
	}

	private void restoreState(SaveableState state) {
		canvasPanel.restoreState(state.canvasState());
		spritesheetManager.restoreState(state.ssmState());
		updateEnableds();
	}

	private SaveableState getState(boolean isTransient) {
		return new SaveableState(canvasPanel.getState(), spritesheetManager.getState(), isTransient);
	}

	/**
	 * Does anything that needs to be done before quitting, and then exits the program.
	 */
	public void quit() {
		try {
			Memory.memory.save();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Master method for choosing a sprite to edit in the canvas.
	 * 
	 * @param layer a sprite
	 */
	public void viewSprite(Layer layer) {
		canvasPanel.setLayer(layer);
		transientState = getState(true);
		updateEnableds();
		repaintCanvas();
	}

	/**
	 * Master method for changing current spritesheet to given. Updates everything else to accommodate change.
	 * 
	 * @param s given spritesheet
	 */
	public void setSpritesheet(Spritesheet s) {
		spritesheetManager.setCurrentSheet(s);
		viewSprite(s.getCurrentSprite());
		updateTitle();
	}

	public Spritesheet getSpritesheet() {
		return spritesheetManager.getCurrentSheet();
	}

	private void updateTitle() {
		Spritesheet s = spritesheetManager.getCurrentSheet();
		if (s == null)
			frame.setTitle(title);
		else
			frame.setTitle(title + " - " + s.getName());
	}

	public Color getCurrentColor() {
		return colorPanel.getCurrentColor();
	}

	public Layer getTopLayer() {
		return canvasPanel.getTopLayer();
	}

	/**
	 * Repaints everything that visually depends on the sprite, e.g. the editing canvas or preview panel
	 */
	public void repaintCanvas() {
		canvasPanel.repaint();
		spritesheetManager.repaintPreview();
	}

	private void makeNewSpritesheet(ActionEvent e) {
		NewSpritesheetPanel nssp = new NewSpritesheetPanel();
		int result = JOptionPane.showConfirmDialog(null, nssp, "Configure New Spritesheet",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			setSpritesheet(new Spritesheet(nssp.getSpriteDim(), nssp.getnSprites()));
		}
	}

	private class NewSpritesheetPanel extends JPanel {
		private static final long serialVersionUID = -7019862296402700192L;

		public NewSpritesheetPanel() {
			setLayout(new GridBagLayout());

			int nCols = 3;
			spriteWidthField = new JTextField(nCols);
			spriteHeightField = new JTextField(nCols);
			nSpritesWideField = new JTextField(nCols);
			nSpritesTallField = new JTextField(nCols);

			GBC.addComp(this::add, 0, 0, new JLabel("Sprite dimensions (pixels):"), new GBC());
			GBC.addComp(this::add, 1, 0, spriteWidthField, new GBC());
			GBC.addComp(this::add, 2, 0, new JLabel("x"), new GBC());
			GBC.addComp(this::add, 3, 0, spriteHeightField, new GBC());

			GBC.addComp(this::add, 0, 1, new JLabel("Sheet dimensions (sprites):"), new GBC());
			GBC.addComp(this::add, 1, 1, nSpritesWideField, new GBC());
			GBC.addComp(this::add, 2, 1, new JLabel("x"), new GBC());
			GBC.addComp(this::add, 3, 1, nSpritesTallField, new GBC());

		}

		JTextField spriteWidthField, spriteHeightField, nSpritesWideField, nSpritesTallField;

		private Dimension getDim(JTextField fx, JTextField fy) {
			int width = Integer.parseInt(fx.getText());
			int height = Integer.parseInt(fy.getText());
			return new Dimension(width, height);
		}

		public Dimension getSpriteDim() {
			return getDim(spriteWidthField, spriteHeightField);
		}

		public Dimension getnSprites() {
			return getDim(nSpritesWideField, nSpritesTallField);
		}

	}

	// inner classes

	private class MenuBar extends JMenuBar {
		private static final long serialVersionUID = 6660574487043364044L;

		public MenuBar() {
			fileChooser.setFileFilter(new FileNameExtensionFilter("Image", IOUtil.VALID_EXTENSIONS));
			// File Menu
			JMenu fileMenu = new JMenu("File");

			var newMenuItem = new JMenuItem("New");
			newMenuItem.addActionListener(App.this::makeNewSpritesheet);

			var openMenuItem = new JMenuItem("Open...");
			openMenuItem.addActionListener(this::openAction);

			var saveMenuItem = new JMenuItem("Save");
			saveMenuItem.addActionListener(this::saveAction);

			var saveAsMenuItem = new JMenuItem("Save As...");
			saveAsMenuItem.addActionListener(this::saveAsAction);

			var quitMenuItem = new JMenuItem("Quit");
			quitMenuItem.addActionListener(_ -> quit());

			var exportMenuItem = new JMenuItem("Export...");
			exportMenuItem.addActionListener(this::exportAction);

			var exportAnimatedMenuItem = new JMenuItem("Export GIF");
			exportAnimatedMenuItem.addActionListener(this::exportAnimatedAction);

			// add file menu items
			fileMenu.add(newMenuItem);
			fileMenu.add(openMenuItem);
			fileMenu.addSeparator();
			fileMenu.add(saveMenuItem);
			fileMenu.add(saveAsMenuItem);
			fileMenu.add(exportMenuItem);
			fileMenu.add(exportAnimatedMenuItem);
			fileMenu.addSeparator();
			fileMenu.add(quitMenuItem);

			// Edit Menu
			JMenu editMenu = new JMenu("Edit");

			var rotateCCWButton = new JMenuItem("Rotate Left");
			rotateCCWButton.addActionListener(_ -> {
				canvasPanel.rotate(-1);
				repaintCanvas();
				saveState();
			});
			var rotateCWButton = new JMenuItem("Rotate Right");
			rotateCWButton.addActionListener(_ -> {
				canvasPanel.rotate(1);
				repaintCanvas();
				saveState();
			});
			var reflectLRButton = new JMenuItem("Flip Left-Right");
			reflectLRButton.addActionListener(_ -> {
				canvasPanel.reflect(false);
				repaintCanvas();
				saveState();
			});
			var reflectUDButton = new JMenuItem("Flip Up-Down");
			reflectUDButton.addActionListener(_ -> {
				canvasPanel.reflect(true);
				repaintCanvas();
				saveState();
			});
			var reduceNColorsButton = new JMenuItem("Reduce # Colors");
			reduceNColorsButton.addActionListener(this::reduceNColorsAction);

			editMenu.add(rotateCCWButton);
			editMenu.add(rotateCWButton);
			editMenu.add(reflectLRButton);
			editMenu.add(reflectUDButton);
			editMenu.addSeparator();
			editMenu.add(reduceNColorsButton);

			// View Menu
			JMenu viewMenu = new JMenu("View");
			ButtonGroup group = new ButtonGroup();

			var tilesButton = new JRadioButtonMenuItem("Tiles");
			tilesButton.addActionListener(_ -> canvasPanel.setRenderStyle(Layer.RENDER_TILES));
			group.add(tilesButton);

			var whiteButton = new JRadioButtonMenuItem("White");
			whiteButton.addActionListener(_ -> canvasPanel.setRenderStyle(Layer.RENDER_WHITE));
			group.add(whiteButton);

			var selectedButton = switch (CanvasPanel.initialRenderStyle) {
			case Layer.RENDER_TILES -> tilesButton;
			case Layer.RENDER_WHITE -> whiteButton;
			default -> null;
			};
			selectedButton.setSelected(true);

			// add view menu items
			viewMenu.add(tilesButton);
			viewMenu.add(whiteButton);

			// add menus
			add(fileMenu);
			add(editMenu);
			add(viewMenu);

			// enabler
			Enabler.Condition isSpritesheet = () -> spritesheetManager.getCurrentSheet() != null;
			Enabler.Condition isSavedSpritesheet = () -> spritesheetManager.getCurrentSheet().getFile() != null;
			enabler.add(saveAsMenuItem::setEnabled, isSpritesheet);
			enabler.add(saveMenuItem::setEnabled, isSpritesheet, isSavedSpritesheet);
			enabler.add(App.this.canvasPanel::hasLayer, rotateCWButton::setEnabled, rotateCCWButton::setEnabled,
					reflectLRButton::setEnabled, reflectUDButton::setEnabled, reduceNColorsButton::setEnabled,
					exportMenuItem::setEnabled, exportAnimatedMenuItem::setEnabled);
		}

		// Fields

		private final Enabler enabler = new Enabler();
		private final JFileChooser fileChooser = new JFileChooser();

		// Methods

		private void openAction(ActionEvent e) {
			int result = fileChooser.showOpenDialog(frame);

			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				setSpritesheet(IOUtil.loadSpritesheat(file));
			}
		}

		private void saveAction(ActionEvent e) {
			if (spritesheetManager.getCurrentSheet().getFile() != null) {
				IOUtil.saveSpritesheet(spritesheetManager.getCurrentSheet());
				updateEnableds();
			}
		}

		private void saveAsAction(ActionEvent e) {
			int result = fileChooser.showSaveDialog(frame);

			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				var ss = spritesheetManager.getCurrentSheet();
				IOUtil.saveLayerAs(ss, file);
				updateTitle();
				updateEnableds();
			}
		}

		private void reduceNColorsAction(ActionEvent e) {
			var rncp = new ReduceNColorsPanel();
			int result = JOptionPane.showConfirmDialog(null, rncp, "Reduce Number of Colors",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				rncp.doReduce();
			}
		}

		private void exportAction(ActionEvent e) {
			var ep = new ExportPanel();
			int result = JOptionPane.showConfirmDialog(null, ep, "Export", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				ep.doExport();
			}
		}

		/**
		 * Exports the spritesheet as an animated gif
		 * 
		 * @param e not used
		 */
		private void exportAnimatedAction(ActionEvent e) {
			var eap = new ExportAnimatedPanel();
			int result = JOptionPane.showConfirmDialog(null, eap, "Export as Animation", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (result == JFileChooser.APPROVE_OPTION) {
				eap.doExport();
			}
		}

		private class ExportAnimatedPanel extends JPanel {
			private static final long serialVersionUID = -2074959697018112578L;

			public ExportAnimatedPanel() {
				super(new GridBagLayout());

				scaleXField = new JTextField("1", 2);
				scaleYField = new JTextField("1", 2);
				skipBlank = new JCheckBox("Skip blank sprites");
				skipBlank.setSelected(true);

				GBC.addComp(this::add, 0, 0, new JLabel("Scale x:"), new GBC().anchor(GBC.EAST));
				GBC.addComp(this::add, 1, 0, scaleXField, new GBC().anchor(GBC.WEST));
				GBC.addComp(this::add, 0, 1, new JLabel("Scale y:"), new GBC().anchor(GBC.EAST));
				GBC.addComp(this::add, 1, 1, scaleYField, new GBC().anchor(GBC.WEST));
				GBC.addComp(this::add, 0, 2, skipBlank, new GBC().dim(2, 1));
			}

			private final JTextField scaleXField, scaleYField;
			private final JCheckBox skipBlank;

			/**
			 * Exports the spritesheet as an animated gif according to the specifications given by the user
			 */
			public void doExport() {
				Util.changeExtension(fileChooser, "gif");
				int result = fileChooser.showSaveDialog(frame);

				if (result == JFileChooser.APPROVE_OPTION) {
					float sx = Float.parseFloat(scaleXField.getText());
					float sy = Float.parseFloat(scaleYField.getText());

					File file = fileChooser.getSelectedFile();
					var ss = spritesheetManager.getCurrentSheet();

					ArrayList<BufferedImage> images = new ArrayList<>();
					for (Layer sprite : ss) {
						if (!skipBlank.isSelected() || sprite.hasVisibleContent())
							images.add(sprite.scaled(sx, sy).getImage());
					}
					IOUtil.saveImagesAsGIF(images, file, spritesheetManager.getDelay());
				}
			}
		}

		/**
		 * The panel that goes in the popup that appears when the user selects Export in the menu
		 */
		private class ExportPanel extends JPanel {
			private static final long serialVersionUID = -3162898818982016576L;

			public ExportPanel() {
				super(new GridBagLayout());

				// section containing scale fields
				var scaleBox = new JPanel(new GridBagLayout());
				scaleXField = new JTextField("1", 2);
				scaleYField = new JTextField("1", 2);
				scaleXField.addActionListener(_ -> updateDimLabels());
				scaleYField.addActionListener(_ -> updateDimLabels());

				GBC.addComp(scaleBox::add, 0, 0, new JLabel("Scale x:"), new GBC().anchor(GBC.EAST).weight(1, 0));
				GBC.addComp(scaleBox::add, 1, 0, scaleXField, new GBC().anchor(GBC.WEST).weight(1, 0));
				GBC.addComp(scaleBox::add, 0, 1, new JLabel("Scale y:"), new GBC().anchor(GBC.EAST).weight(1, 0));
				GBC.addComp(scaleBox::add, 1, 1, scaleYField, new GBC().anchor(GBC.WEST).weight(1, 0));

				// section containing preview panel and labels for dimensions
				var previewBox = new JPanel(new GridBagLayout());
				previewPanel = new JPanel() {
					private static final long serialVersionUID = 3640189680951807331L;

					@Override
					public void paintComponent(Graphics g) {
						previewLayer.renderAt((Graphics2D) g, new Point(), getSize(), Layer.RENDER_TRANSPARENT);
					}
				};
				previewPanel.setPreferredSize(new Dimension(100, 100));
				GBC.addComp(previewBox::add, 0, 0, new JLabel("Preview"), new GBC().anchor(GBC.CENTER).dim(2, 1));
				GBC.addComp(previewBox::add, 0, 1, heightLabel, new GBC().anchor(GBC.EAST));
				GBC.addComp(previewBox::add, 1, 1, previewPanel, new GBC().fill(GBC.BOTH).weight(1, 1));
				GBC.addComp(previewBox::add, 1, 2, widthLabel, new GBC().anchor(GBC.NORTH));

				// section containing options for what to export
				var exportBox = new JPanel(new GridBagLayout());
				var exportOptionGroup = new ButtonGroup();

				var selectionOption = new JRadioButton("Selection");
				selectionOption.addActionListener(_ -> setPreviewLayer(canvasPanel.getTopLayer(false).shrinkwrapped()));
				selectionOption.setEnabled(canvasPanel.hasSelection());
				var spriteOption = new JRadioButton("Sprite");
				spriteOption.addActionListener(_ -> setPreviewLayer(canvasPanel.getTopLayer(true)));
				var spritesheetOption = new JRadioButton("Spritesheet");
				spritesheetOption.addActionListener(_ -> setPreviewLayer(spritesheetManager.getCurrentSheet()));

				exportOptionGroup.add(selectionOption);
				exportOptionGroup.add(spriteOption);
				exportOptionGroup.add(spritesheetOption);

				GBC.addComp(exportBox::add, 0, 0, new JLabel("Export"), new GBC().anchor(GBC.SOUTH));
				GBC.addComp(exportBox::add, 0, 1, selectionOption, new GBC().anchor(GBC.WEST));
				GBC.addComp(exportBox::add, 0, 2, spriteOption, new GBC().anchor(GBC.WEST));
				GBC.addComp(exportBox::add, 0, 3, spritesheetOption, new GBC().anchor(GBC.WEST));

				// put it all together
				GBC.addComp(this::add, 0, 0, previewBox, new GBC().fill(GBC.BOTH).weight(1, 1));
				GBC.addComp(this::add, 0, 1, scaleBox, new GBC().fill(GBC.BOTH));
				GBC.addComp(this::add, 1, 0, exportBox, new GBC().fill(GBC.BOTH).dim(1, 2));

				// determine which option is initially clicked
				boolean selectSelection = selectionOption.isEnabled();
				selectionOption.setSelected(selectSelection);
				spriteOption.setSelected(!selectSelection);
				previewLayer = canvasPanel.getTopLayer(!selectSelection);
				if (selectSelection)
					previewLayer = previewLayer.shrinkwrapped();

				updateDimLabels();
			}

			private final JPanel previewPanel;
			private Layer previewLayer;
			private final JLabel heightLabel = new JLabel(), widthLabel = new JLabel();
			private final JTextField scaleXField, scaleYField;

			public void doExport() {
				int result = fileChooser.showSaveDialog(frame);

				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					float sx = Float.parseFloat(scaleXField.getText());
					float sy = Float.parseFloat(scaleYField.getText());
					IOUtil.saveLayerAs(previewLayer.scaled(sx, sy), file);
				}
			}

			/**
			 * Sets the layer to be shown in the preview panel, repaints the panel, and updates dimension labels.
			 * 
			 * @param l layer to show
			 */
			private void setPreviewLayer(Layer l) {
				previewLayer = l;
				previewPanel.repaint();
				updateDimLabels();
			}

			/**
			 * Updates the jlabels that display the width and height of the exported image.
			 */
			private void updateDimLabels() {
				float sx = Float.parseFloat(scaleXField.getText());
				float sy = Float.parseFloat(scaleYField.getText());
				Dimension size = previewLayer.getScaledSize(sx, sy);
				widthLabel.setText("" + size.width);
				heightLabel.setText("" + size.height);
			}

		}

		/**
		 * Panel handling the reduction of number of colors in the image.
		 */
		private class ReduceNColorsPanel extends JPanel {
			private static final long serialVersionUID = -457791697346635272L;

			private ReduceNColorsPanel() {
				super(new GridBagLayout());

				var group = new ButtonGroup();
				group.add(entireSpriteButton);
				group.add(selectionOnlyButton);
				entireSpriteButton.setSelected(true);
				selectionOnlyButton.setEnabled(canvasPanel.hasSelection());

				group = new ButtonGroup();
				group.add(rgbButton);
				group.add(hsbButton);
				hsbButton.setSelected(true);

				GBC.addComp(this::add, 0, 0, new JLabel("Number of colors:"), new GBC().anchor(GBC.EAST).dim(1, 3));
				GBC.addComp(this::add, 1, 0, inputField, new GBC().anchor(GBC.WEST).dim(1, 3).insets(0, hpad, 0, pad));

				GBC.addComp(this::add, 2, 0, new JLabel("Region to reduce:"), new GBC().anchor(GBC.SOUTHWEST));
				GBC.addComp(this::add, 2, 1, selectionOnlyButton, new GBC().anchor(GBC.WEST));
				GBC.addComp(this::add, 2, 2, entireSpriteButton, new GBC().anchor(GBC.WEST));

				GBC.addComp(this::add, 3, 0, new JLabel("Method to reduce:"), new GBC().anchor(GBC.SOUTHWEST));
				GBC.addComp(this::add, 3, 1, rgbButton, new GBC().anchor(GBC.WEST));
				GBC.addComp(this::add, 3, 2, hsbButton, new GBC().anchor(GBC.WEST));
			}

			private final JTextField inputField = new JTextField(3);
			private final JRadioButton selectionOnlyButton = new JRadioButton("Selection only");
			private final JRadioButton entireSpriteButton = new JRadioButton("Entire sprite");
			private final JRadioButton rgbButton = new JRadioButton("RGB");
			private final JRadioButton hsbButton = new JRadioButton("HSB");

			public void doReduce() {
				if (selectionOnlyButton.isSelected())
					Layer.reduceNColors(Integer.parseInt(inputField.getText()), rgbButton.isSelected(),
							canvasPanel.getSelection());
				else
					Layer.reduceNColors(Integer.parseInt(inputField.getText()), rgbButton.isSelected(),
							canvasPanel.getLayers(false));
				saveState();
				repaintCanvas();
			}
		}

	}

}
