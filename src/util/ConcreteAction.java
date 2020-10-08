package util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;

public class ConcreteAction extends AbstractAction {
	private static final long serialVersionUID = 4234875737781825742L;

	public ConcreteAction(ActionListener al) {
		this.al = al;
	}
	
	private final ActionListener al;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		al.actionPerformed(e);
	}

}
