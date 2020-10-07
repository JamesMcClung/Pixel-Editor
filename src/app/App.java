package app;

import static app.Constants.hpad;
import static app.Constants.pad;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import canvas.Layer;
import canvas.Spritesheet;
import io.IOUtil;
import io.Memory;
import util.Enabler;
import util.GBC;
import util.StateLog;
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
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // handled by quit method
		frame.setLayout(new GridBagLayout());
		
		// prevent resizing to beloww minimum size
		frame.addComponentListener(new ComponentAdapter(){
			@Override
	        public void componentResized(ComponentEvent e){
	            Dimension d = frame.getSize();
	            Dimension minD = frame.getMinimumSize();
	            if(d.width < minD.width)
	                d.width = minD.width;
	            if(d.height < minD.height)
	                d.height = minD.height;
	            frame.setSize(d);
	        }
	    });
		frame.addWindowListener(new WindowAdapter() {
			@Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
		
		statelog = new StateLog<>(maxNumStates);
		
		// menu bar
		menuBar = new MenuBar();
		frame.setJMenuBar(menuBar);
		
		// other components
		spritesheetManager = new SpritesheetManager(this);
		canvasPanel = new CanvasPanel();
		colorPanel = new ColorPanel();
		toolPanel = new ToolPanel(this);
		
		GBC.addComp(frame::add, 0, 0, spritesheetManager, new GBC().dim(2, 1).weight(1, 0).fill(GBC.BOTH).insets(pad, hpad, hpad, 0), BorderFactory.createRaisedSoftBevelBorder());
		GBC.addComp(frame::add, 2, 0, colorPanel, new GBC().fill(GBC.BOTH).insets(pad, 0, hpad, hpad), BorderFactory.createRaisedSoftBevelBorder());
		GBC.addComp(frame::add, 0, 1, toolPanel, new GBC().fill(GBC.BOTH).insets(hpad, pad, pad, hpad), BorderFactory.createRaisedSoftBevelBorder());
		GBC.addComp(frame::add, 1, 1, canvasPanel, new GBC().dim(2, 1).weight(1, 1).insets(hpad, hpad, pad, pad).fill(GBC.BOTH), BorderFactory.createLoweredSoftBevelBorder());
		
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
	
	private final ArrayList<SizeLockable> compsToLockSizeOfAfterPacking = new ArrayList<>(); // only used after frame.pack() is called in constructor
	
	
	// Methods
	void lockSizeAfterPack(SizeLockable comp) {
		compsToLockSizeOfAfterPacking.add(comp);
	}
	
	
	// update enabledness
	public void updateEnableds() {
		menuBar.updateEnableds();
		toolPanel.updateEnableds();
	}
	
	
	// State Saving
	public static final int maxNumStates = 100;
	private final StateLog<SaveableState> statelog;
	private SaveableState transientState = null; // not saved to the log unless an edit is made; represents the initial state of a layer before edits are made
	
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
		return new SaveableState(canvasPanel.getState(),
				spritesheetManager.getState(),
				isTransient);
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
	 * @param s given spritesheet
	 */
	public void setSpritesheet(Spritesheet s) {
		spritesheetManager.setCurrentSheet(s);
		viewSprite(s.getSprite());
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
	
	
	/**
	 * Binds the given key to the given action.
	 * @param key a character, e.g. "A" (case sensitive!)
	 * @param action what happens when key is pressed
	 */
	public void addKeyBinding(String key, Action action) {
		var comp = frame.getRootPane(); // doesnt really matter what this is
		var inputMap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		var actionMap = comp.getActionMap();
		
		inputMap.put(KeyStroke.getKeyStroke(key), key);
		actionMap.put(key, action);
	}
	
	
	
	private void makeNewSpritesheet(ActionEvent e) {
		NewSpritesheetPanel nssp = new NewSpritesheetPanel();
		int result = JOptionPane.showConfirmDialog(null, nssp, "Configure New Spritesheet", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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
			quitMenuItem.addActionListener((e) -> quit());
			
			// add file menu items
			fileMenu.add(newMenuItem);
			fileMenu.add(openMenuItem);
			fileMenu.addSeparator();
			fileMenu.add(saveMenuItem);
			fileMenu.add(saveAsMenuItem);
			fileMenu.addSeparator();
			fileMenu.add(quitMenuItem);
			
			// View Menu
			JMenu viewMenu = new JMenu("View");
			ButtonGroup group = new ButtonGroup();
			
			var tilesButton = new JRadioButtonMenuItem("Tiles");
			tilesButton.addActionListener((e) -> canvasPanel.setRenderStyle(Layer.RENDER_TILES));
			group.add(tilesButton);

			var whiteButton = new JRadioButtonMenuItem("White");
			whiteButton.addActionListener((e) -> canvasPanel.setRenderStyle(Layer.RENDER_WHITE));
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
			add(viewMenu);
			
			// enabler
			Enabler.Condition isSpritesheet = () -> spritesheetManager.getCurrentSheet() != null;
			Enabler.Condition isSavedSpritesheet = () -> spritesheetManager.getCurrentSheet().getFile() != null;
			enabler.add(saveAsMenuItem::setEnabled, isSpritesheet);
			enabler.add(saveMenuItem::setEnabled, isSpritesheet, isSavedSpritesheet);
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
			}
		}
		
		private void saveAsAction(ActionEvent e) {
			int result = fileChooser.showSaveDialog(frame);
			
			if (result == JFileChooser.APPROVE_OPTION) {
	            File file = fileChooser.getSelectedFile();
	            IOUtil.saveSpritesheetAs(spritesheetManager.getCurrentSheet(), file);
	            updateTitle();
	            updateEnableds();
	        }
		}
		
		public void updateEnableds() {
			enabler.updateEnableds();
		}
		
	}

}
