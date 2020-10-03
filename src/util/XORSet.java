package util;

import java.util.HashSet;

public class XORSet<E> extends HashSet<E> {
	private static final long serialVersionUID = 6850192137074966506L;
	
	/**
	 * Adds the given element to the set, or if the element was already present, removes it.
	 * @return true if element was added, false if removed
	 */
	@Override
	public boolean add(E e) {
		if (!super.add(e)) {
			remove(e);
			return false;
		}
		return true;
	}

}
