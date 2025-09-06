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
			var input_stream = new ObjectInputStream(new FileInputStream(PATH_TO_MEMORY));
			var memory = (Memory) input_stream.readObject();
			input_stream.close();
			return memory;
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
	public int activeIndex = 0;

	// methods

	/**
	 * Returns a saved color palette, or if none exists, a new color palette of the given dimensions.
	 * 
	 * @param default_n_rows number of rows in created palette
	 * @param default_n_cols number of columns in created palette
	 * @return the palette
	 */
	public ColorPalette getPaletteOrDefault(int default_n_rows, int default_n_cols) {
		if (colorPalettes.isEmpty()) {
			colorPalettes.add(new ColorPalette(default_n_rows, default_n_cols));
		}
		return colorPalettes.get(activeIndex);
	}

	public void save() throws IOException {
		ObjectOutputStream output_stream = null;
		try {
			output_stream = new ObjectOutputStream(new FileOutputStream(PATH_TO_MEMORY));
			output_stream.writeObject(this);
			output_stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output_stream != null)
				output_stream.close();
		}
	}
}
