package display;


import static display.Constants.hpad;
import static display.Constants.pad;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;

import io.ColorPalette;
import io.Memory;
import util.Button2Field;
import util.GBC;
import util.LabeledSlider;
import util.MyDefaultListModel;
import util.Util;

public class ColorPanel extends JPanel {
	private static final long serialVersionUID = 4161506380850747936L;
	
	@FunctionalInterface
	private interface ColorListener {
		void colorChanged(Color newColor);
	}
	
	public static final int paletteCols = 8, paletetRows = 3;
	
	public ColorPanel() {
		setLayout(new GridBagLayout());
		addMouseMotionListener(mouseHandler);
		
		// panels
		colorMakerPanel = new ColorMakerPanel();
		colorPalettePanel = new ColorPalettePanel(Memory.memory.getDefaultPalette(paletetRows, paletteCols));
		JPanel swatchPanel = new JPanel(new GridBagLayout());
		
		// swatches
		currentColor = new ColorSwatch(initialColor);
		prevColor = new FixedColorSwatch();
		currentColor.addColorListener((c) -> prevColor.setColor(currentColor.color));
		
		GBC.addComp(swatchPanel::add, 0, 0, new JLabel("Current"), new GBC().insets(pad, pad, 0, pad));
		GBC.addComp(swatchPanel::add, 0, 1, currentColor, new GBC().insets(0, pad, hpad, pad));
		GBC.addComp(swatchPanel::add, 0, 2, new JLabel("Previous"), new GBC().insets(hpad, pad, 0, pad));
		GBC.addComp(swatchPanel::add, 0, 3, prevColor, new GBC().insets(0, pad, pad, pad));
		
		GBC.addComp(this::add, 0, 0, colorPalettePanel, new GBC());
		GBC.addComp(this::add, 1, 0, swatchPanel, new GBC());
		GBC.addComp(this::add, 2, 0, new JSeparator(SwingConstants.VERTICAL), new GBC().weight(0, 1).fill(GBC.VERTICAL).insets(hpad, 0, hpad, 0));
		GBC.addComp(this::add, 3, 0, colorMakerPanel, new GBC().insets(hpad, hpad, hpad, hpad));
	}
	

	// fields
	
	public static final Color initialColor = Color.BLACK;
	
	private final ColorSwatch currentColor, prevColor;
	private final ColorMakerPanel colorMakerPanel;
	private final ColorPalettePanel colorPalettePanel;
	
	
	// methods
	
	public Color getCurrentColor() {
		return currentColor.color;
	}
	
	public void setCurrentColor(Color c) {
		currentColor.setColor(c);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		// render dragged color swatch on top of everything else
		if (isDragging()) {
			draggedSwatch.renderSpot((Graphics2D) g, draggedSwatchLoc);
		}
	}
	
	
	
	// Color Palette
	
	private class ColorPalettePanel extends JPanel {
		private static final long serialVersionUID = -3155302686044119321L;
		
		public ColorPalettePanel(ColorPalette palette) {
			setLayout(new GridBagLayout());
			
			// make the button/title
			button = new JButton(palette.getName());
			button.addActionListener(this::openPaletteEditor);
			
			GBC.addComp(this::add, 0, 0, button, new GBC().fill(GBC.HORIZONTAL));
			setColorPalette(palette);
		}
		
		private final JButton button;
		private JPanel innerPanel;
		private ColorPalette currentPalette;
		
		public void setColorPalette(ColorPalette palette) {
			currentPalette = palette;
			if (innerPanel != null)
				remove(innerPanel);
			innerPanel = makeInnerPanel(palette);
			GBC.addComp(this::add, 0, 1, innerPanel, new GBC());
			button.setText(palette.getName());
			validate();
			repaint();
		}
		
		/**
		 * Creates a JPanel that contains a grid of color swatches corresponding to the given palette
		 * @param palette a color palette
		 * @return the panel
		 */
		private JPanel makeInnerPanel(ColorPalette palette) {
			int rows = palette.getRows(), cols = palette.getCols();
			JPanel panel = new JPanel(new GridLayout(rows, cols, -1, -1));
			
			var swatches = new ColorSwatch[rows][cols];
			for (int i = 0; i < rows; i++) {
				for(int j = 0; j < cols; j++) {
					final int y = i, x = j;
					swatches[i][j] = new ColorSwatch(palette.colors[i][j]) {
						private static final long serialVersionUID = -9140800990816388643L;
						@Override
						public void setColor(Color c) {
							palette.colors[y][x] = c;
							super.setColor(c);
						}
					};
					panel.add(swatches[i][j]);
				}
			}
			
			return panel;
		}
		
		private void openPaletteEditor(ActionEvent e) {
			var pep = new PaletteEditorPanel();
			int result = JOptionPane.showConfirmDialog(null, pep, "Edit Color Palettes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				setColorPalette(pep.list.getSelectedValue());
				repaint();
			}
		}
		
		/**
		 * The panel that appears in the popup menu for editing palettes
		 */
		private class PaletteEditorPanel extends JPanel {
			private static final long serialVersionUID = -6446236988605940071L;
			
			public static final int listWidth = 256, listHeight = 128; 
			
			public PaletteEditorPanel() {
				setLayout(new GridBagLayout());
				
				// set up list inside scrollpanel
				listModel = new MyDefaultListModel<>(Memory.memory.colorPalettes);
				list = new JList<>(listModel);
				list.addListSelectionListener(this::updateButtonStates);
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				JScrollPane listScroller = new JScrollPane(list);
				listScroller.setPreferredSize(new Dimension(listWidth, listHeight));
				
				// buttons
				newButton = new JButton("New");
				newButton.addActionListener(this::newPalette);
				renameButton = new Button2Field("Rename", (comp) -> GBC.addComp(this::add, 1, 1, comp, new GBC().fill(GBC.HORIZONTAL).weight(0,1)));
				renameButton.addActionListenerToField(this::renamePalette);
				deleteButton = new JButton("Delete");
				deleteButton.addActionListener(this::deletePalette);
				copyButton = new JButton("Copy");
				copyButton.addActionListener(this::copyPalette);
				updateButtonStates(null);

				// add to panel
				GBC.addComp(this::add, 0, 0, listScroller, new GBC().dim(1, 4).fill(GBC.BOTH).weight(1, 0));
				GBC.addComp(this::add, 1, 0, newButton, new GBC().fill(GBC.HORIZONTAL).weight(0,1));
				renameButton.add();
				GBC.addComp(this::add, 1, 2, copyButton, new GBC().fill(GBC.HORIZONTAL).weight(0,1));
				GBC.addComp(this::add, 1, 3, deleteButton, new GBC().fill(GBC.HORIZONTAL).weight(0,1));
				
				list.setSelectedValue(currentPalette, true);
			}
			
			private final MyDefaultListModel<ColorPalette> listModel;
			private final JList<ColorPalette> list;
			private final JButton newButton, deleteButton, copyButton;
			private final Button2Field renameButton;
			
			private void updateButtonStates(ListSelectionEvent e) {
				boolean isSelection = list.getSelectedIndex() >= 0;
				renameButton.setEnabled(isSelection);
				copyButton.setEnabled(isSelection);
				deleteButton.setEnabled(isSelection && listModel.getSize() > 1);
			}
			
			private void newPalette(ActionEvent e) {
				listModel.addElement(new ColorPalette(paletetRows, paletteCols));
				list.setSelectedIndex(listModel.getSize()-1);
			}
			private void renamePalette(ActionEvent e) {
				list.getSelectedValue().setName(renameButton.getText());
			}
			private void copyPalette(ActionEvent e) {
				ColorPalette copied = list.getSelectedValue();
				listModel.addElement(copied.getCopy());
				updateButtonStates(null);
			}
			private void deletePalette(ActionEvent e) {
				int index = list.getSelectedIndex();
				listModel.removeIndex(index);
				if (index == listModel.getSize())
					index--;
				list.setSelectedIndex(index);
			}
		}
	}
	
	
	
	// inner classes: Color Maker
	
	private class ColorMakerPanel extends JPanel implements ColorListener {
		private static final long serialVersionUID = -4768183321498178089L;
	
		public static final int nrows = 2, ncols = 4;

		public ColorMakerPanel() {
			setLayout(new GridBagLayout());
			
			// panels
			adjusterPanel = new RGBAdjuster(this, initialColor);
			JPanel variantPanel = new JPanel(new GridLayout(nrows, ncols, -1, -1));
			
			previewColor = new ColorSwatch(initialColor) {
				private static final long serialVersionUID = -2511399194072976234L;
				@Override
				public void setColorFromDrag(Color c) {
					super.setColorFromDrag(c);
					adjusterPanel.updateToMatchColor(c);
				}
			};
			previewColor.addColorListener(this::updateVariants);
			
			// initialize variants
			variants = new ColorSwatch[nrows][ncols];
			for (int i = 0; i < nrows; i++) {
				for (int j = 0; j < ncols; j++) {
					variants[i][j] = new FixedColorSwatch();
					variantPanel.add(variants[i][j]);
				}
			}
			updateVariants(previewColor.color);
			
			// add things
			
			GBC.addComp(this::add, 0, 0, new JLabel("Preview"), new GBC().anchor(GBC.SOUTH).weight(0, .5).insets(0, hpad, 0, hpad));
			GBC.addComp(this::add, 0, 1, previewColor, new GBC().anchor(GBC.NORTH).weight(0, .5).insets(0, hpad, hpad, hpad));
			GBC.addComp(this::add, 0, 2, variantPanel, new GBC().insets(hpad, hpad, 0, hpad));
			GBC.addComp(this::add, 1, 0, adjusterPanel, new GBC().dim(1, 3).fill(GBC.BOTH).weight(1, 1).insets(0, hpad, 0, 0), BorderFactory.createRaisedSoftBevelBorder());
		}
		
		
		private final ColorSwatch previewColor;
		private AdjusterPanel adjusterPanel;
		
		private final ColorSwatch[][] variants;
		private double maxCoef = getCoef(0,0) + 1;
		
		
		/**
		 * Updates the variant color swatch colors based on the given color
		 * @param c the color 
		 */
		private void updateVariants(Color c) {
			for (int i = 0; i < nrows; i++) {
				for (int j = 0; j < ncols; j++) {
					variants[i][j].setColor(getVariantColor(c, i, j));
				}
			}
		}
		
		// helper method
		private Color getVariantColor(Color c, int row, int col) {
			double coef = getCoef(row, col);
			return new Color(coef2val(c.getRed(), coef), coef2val(c.getGreen(), coef), coef2val(c.getBlue(), coef));
		}
		
		// helper method
		private double getCoef(int row, int col) {
			double k = (nrows * ncols) / 2.0 - (ncols * row + col);
			return Math.abs(Math.pow(k, 1)) * Math.signum(k);
		}
		
		// helper method
		private int coef2val(int val, double coef) {
			if (coef < 0)
				return (int) (val + (val - 0) * coef/maxCoef);
			return (int) (val + (255-val) * coef/maxCoef);
		}
		
		@Override
		public void colorChanged(Color newColor) {
			previewColor.setColor(newColor);
		}
	}
	
	/**
	 * The panel that contains sliders, etc. used to edit a color. Might be RGB, CMYK, etc. 
	 */
	private abstract class AdjusterPanel extends JPanel {
		private static final long serialVersionUID = 1657689513663429215L;
		
		public AdjusterPanel(ColorListener cl) {
			this.cl = cl;
		}

		protected final ColorListener cl;
		public abstract Color getColor();
		public abstract void updateToMatchColor(Color c);
	}
	
	private class RGBAdjuster extends AdjusterPanel implements ChangeListener {
		private static final long serialVersionUID = -4014548699223975565L;

		public RGBAdjuster(ColorListener cl, Color initialColor) {
			super(cl);
			
			setLayout(new GridBagLayout());
			
			redSlider = new LabeledSlider(0, 255, initialColor.getRed());
			greenSlider = new LabeledSlider(0, 255, initialColor.getGreen());
			blueSlider = new LabeledSlider(0, 255, initialColor.getBlue());
			
			redSlider.addChangeListener(this);
			greenSlider.addChangeListener(this);
			blueSlider.addChangeListener(this);
			
			GBC.addComp(this::add, 0, 0, new JLabel("R:"), new GBC().insets(0, pad, 0, hpad));
			GBC.addComp(this::add, 0, 1, new JLabel("G:"), new GBC().insets(0, pad, 0, hpad));
			GBC.addComp(this::add, 0, 2, new JLabel("B:"), new GBC().insets(0, pad, 0, hpad));
			GBC.addComp(this::add, 1, 0, redSlider.label, new GBC().anchor(GBC.EAST));
			GBC.addComp(this::add, 1, 1, greenSlider.label, new GBC().anchor(GBC.EAST));
			GBC.addComp(this::add, 1, 2, blueSlider.label, new GBC().anchor(GBC.EAST));
			GBC.addComp(this::add, 2, 0, redSlider, new GBC());
			GBC.addComp(this::add, 2, 1, greenSlider, new GBC());
			GBC.addComp(this::add, 2, 2, blueSlider, new GBC());
		}
		
		private final LabeledSlider redSlider, greenSlider, blueSlider;

		@Override
		public Color getColor() {
			return new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			cl.colorChanged(getColor());
		}

		@Override
		public void updateToMatchColor(Color c) {
			redSlider.setValue(c.getRed());
			greenSlider.setValue(c.getGreen());
			blueSlider.setValue(c.getBlue());
		}
		
	}
	
	// Color Swatches
	
	private ColorSwatch draggedSwatch = null;
	private final Point draggedSwatchLoc = new Point();
	private boolean hasDragged = false;
	
	private void setDraggedSwatch(ColorSwatch cs) {
		draggedSwatch = cs;
	}
	
	private boolean isDragging() {
		return draggedSwatch != null & hasDragged;
	}
	
	private void setDraggedLoc(MouseEvent e) {
		e = SwingUtilities.convertMouseEvent(e.getComponent(), e, this);
		draggedSwatchLoc.setLocation(e.getPoint());
	}
	
	private MouseAdapter mouseHandler = new MouseAdapter() {
		private ColorSwatch lastEntered = null;

		@Override
		public void mouseEntered(MouseEvent e) {
			if (isDragging() && e.getComponent() instanceof ColorSwatch)
				lastEntered = (ColorSwatch) e.getComponent();
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			if (isDragging() && e.getComponent().equals(ColorPanel.this)) {
				lastEntered = null;
				setDraggedSwatch(null);
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getComponent() instanceof ColorSwatch) {
				var swatch = (ColorSwatch) e.getComponent();
				hasDragged = false;
				setDraggedSwatch(swatch);
				lastEntered = swatch;
				setDraggedLoc(e);
				repaint();
				e.consume();
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getComponent() instanceof ColorSwatch) {
				setCurrentColor(((ColorSwatch) e.getComponent()).color);
				e.consume();
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (isDragging() && lastEntered != null && e.getComponent() instanceof ColorSwatch) {
				lastEntered.setColorFromDrag(draggedSwatch.color);
				lastEntered = null;
				setDraggedSwatch(null);
				repaint();
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			hasDragged = true;
			if (isDragging()) {
				setDraggedLoc(e);
				repaint();
			}
		}
	};
	
	
	public class ColorSwatch extends JPanel {
		private static final long serialVersionUID = 6420350469275944970L;
		
		public static final int width = 16, height = 16;
		static final int spotDiam = 16;
		
		public ColorSwatch(Color color) {
			this.color = color;
			setMinimumSize(new Dimension(width, height));
			setPreferredSize(new Dimension(width, height));
			addMouseListener(mouseHandler);
			addMouseMotionListener(mouseHandler);
		}

		public ColorSwatch() {
			this(Color.WHITE);
		}
		
		
		// Fields
		
		private Color color;
		private List<ColorListener> colorListeners = new ArrayList<>();
		
		
		// Methods
		
		public void setColor(Color c) {
			if (c.equals(color))
				return;
			for (var cl : colorListeners)
				cl.colorChanged(c);
			color = c;
			repaint();
		}
		public void setColorFromDrag(Color c) {
			setColor(c);
		}
		
		public void addColorListener(ColorListener cl) {
			colorListeners.add(cl);
		}
		
		/**
		 * Draws a circle centered at the given point. The color is this swatches' color.  
		 * @param g graphics to draw on
		 * @param loc center of circle
		 */
		void renderSpot(Graphics2D g, Point loc) {
			Util.enableAntiAliasing(g);
			g.setColor(color);
			g.fillOval(loc.x - spotDiam/2, loc.y - spotDiam/2, spotDiam, spotDiam);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(color);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, getWidth(), getHeight());
		}
	}
	
	/**
	 * A color swatch that cannot be set by dragging a color to it.
	 */
	private class FixedColorSwatch extends ColorSwatch {
		private static final long serialVersionUID = -1470567223847526048L;

		@Override
		public void setColorFromDrag(Color c) { } // does nothing
	}

}
