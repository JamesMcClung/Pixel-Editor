package util;

import java.util.List;

import javax.swing.AbstractListModel;

public class MyDefaultListModel<E> extends AbstractListModel<E> {
	private static final long serialVersionUID = -3373424590165782348L;

	public MyDefaultListModel(List<E> list) {
		this.list = list;
	}
	
	private final List<E> list;

	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public E getElementAt(int index) {
		return list.get(index);
	}
	
	public void addElement(E element) {
		list.add(element);
		fireIntervalAdded(this, list.size(), list.size());
	}
	
	public void removeIndex(int i) {
		list.remove(i);
		this.fireIntervalRemoved(this, i, i);
	}

}
