package io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

public class Memory implements Serializable {
	private static final long serialVersionUID = -5014472746949786889L;

	public static final String PATH_TO_MEMORY = new JFileChooser().getFileSystemView().getHomeDirectory()
			.getAbsolutePath() + "/Library/Application Support/PixelEditor/memory.ser";

	/**
	 * The stored memory, e.g. saved color palettes.
	 */
	public static final Memory memory = getMemory();

	/**
	 * Loads and the Memory in memory.
	 * 
	 * @return the memory
	 */
	private static Memory getMemory() {
		try {
			var oos = new ObjectInputStream(new FileInputStream(PATH_TO_MEMORY));
			Memory mem = (Memory) oos.readObject();
			oos.close();
			return mem;
		} catch (IOException e) {
			return new Memory();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	// no other classes can instantiate a memory
	private Memory() {
	}

	// Fields (all public)

	public final List<ColorPalette> colorPalettes = new ArrayList<>();

	// methods

	/**
	 * Returns a saved color palette, or if none exist, a new color palette of the given dimensions.
	 * 
	 * @param rows number of rows in created palette
	 * @param cols number of columns in created palette
	 * @return the palette
	 */
	public ColorPalette getDefaultPalette(int rows, int cols) {
		if (colorPalettes.isEmpty()) {
			colorPalettes.add(new ColorPalette(rows, cols));
		}
		return colorPalettes.get(0);
	}

	public void save() throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(PATH_TO_MEMORY));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null)
				oos.close();
		}
	}
}
