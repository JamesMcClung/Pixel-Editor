package io;

import java.awt.Color;
import java.io.Serializable;

public class ColorPalette implements Serializable {
	private static final long serialVersionUID = -4663115192278287110L;
	
	private static String getDefaultName() {
		final String base = "Palette ";
		int n = 1;
		String name;
		do {
			name = base + n++;
		} while(isNameTaken(name));
		return name;
	}
	
	/**
	 * Determines whether the given name is already associated with a color palette in memory.
	 * @param name the name
	 * @return whether the name is taken
	 */
	public static boolean isNameTaken(String name) {
		for (ColorPalette palette : Memory.memory.colorPalettes) {
			if (palette.name.equals(name))
				return true;
		}
		return false;
	}
	
	
	// Constructors
	
	public ColorPalette(String name, Color[][] colors) {
		this.name = name;
		this.colors = colors;
	}
	
	public ColorPalette(Color[][] colors) {
		this(getDefaultName(), colors);
	}
	
	public ColorPalette(int rows, int cols) {
		this(getBlankColors(rows, cols));
	}
	
	public ColorPalette(ColorPalette palette) {
		name = palette.name + " copy";
		
		for (int n = 2; isNameTaken(name); n++)
			name = palette.name + " copy " + n;
		
		int rows = palette.getRows(), cols = palette.getCols();
		colors = new Color[rows][cols];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				colors[i][j] = palette.colors[i][j];
	}
	
	/**
	 * Default color for unassigned colors in the palette.
	 */
	public static Color defaultColor = Color.WHITE;
	private static Color[][] getBlankColors(int rows, int cols) {
		Color[][] colors = new Color[rows][cols];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				colors[i][j] = defaultColor;
		return colors;
	}
	
	
	// Fields
	
	private String name;
	public final Color[][] colors;
	
	
	// Methods
	
	public String getName() {
		return name;
	}
	
	public int getRows() {
		return colors.length;
	}
	
	public int getCols() {
		return colors[0].length;
	}
	
	/**
	 * Tries to set this palette's name to the given name. Fails if name is already taken.
	 * @param name given name
	 * @return false iff name was taken
	 */
	public boolean setName(String name) {
		if (isNameTaken(name)) 
			return false;
		this.name = name;
		return true;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public ColorPalette getCopy() {
		return new ColorPalette(this);
	}

}
