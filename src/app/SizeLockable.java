package app;

/**
 * Used to set preferred (or minimum) sizes of components <i>after</i> frame has been packed, so that preferred size may
 * be determined by the packing process.
 */
public interface SizeLockable {

	void lockSize();

}
