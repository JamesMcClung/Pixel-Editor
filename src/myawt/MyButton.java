package myawt;

import java.awt.event.ActionListener;

import javax.swing.JButton;

import util.Util;

public abstract class MyButton extends JButton implements ActionListener {
	private static final long serialVersionUID = -808304620864819645L;

	public MyButton(String name, String hotkey) {
		super(name);

		addActionListener(this);

		if (hotkey != null)
			Util.addKeyBinding(this, hotkey, this);
	}

}
