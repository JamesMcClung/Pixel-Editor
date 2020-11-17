package myawt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimpleButton extends MyButton {
	private static final long serialVersionUID = 396643403249988441L;
	
	public SimpleButton(String name, String hotkey, ActionListener action) {
		super(name, hotkey);
		this.action = action;
	}
	
	private final ActionListener action;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		action.actionPerformed(e);
	}
}
