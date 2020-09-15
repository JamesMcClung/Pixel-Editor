package util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * A class to handle a button that transforms into a text field when clicked.
 */
public class Button2Field {
	@FunctionalInterface
	public interface Adder {
		void add(JComponent c);
	}
	
	public Button2Field(String buttonText, Adder adder) {
		field = new JTextField();
		button = new JButton(buttonText);
		button.addActionListener(this::change);
		field.addActionListener(this::change);
		
		this.adder = adder;
		
		active = button;
		inactive = field;
	}
	
	private final Adder adder;
	private final JButton button;
	private final JTextField field;
	
	private JComponent active, inactive;
	
	/**
	 * Switches which component—field or button—is active.
	 * @param e this parameter is only included for use as an actionlistener
	 */
	private void change(ActionEvent e) {
		inactive.setPreferredSize(active.getSize());
		
		active.getParent().remove(active);
		adder.add(inactive);
		
		var temp = inactive;
		inactive = active;
		active = temp;
		
		active.getParent().validate();
		active.requestFocusInWindow();
		if (active.equals(field)) {
			field.setText("");
		}
		active.getParent().repaint();
	}
	
	/**
	 * Adds the active to component to a container for the first time. Only call this once.
	 */
	public void add() {
		adder.add(active);
	}
	
	public String getText() {
		return field.getText();
	}
	
	public void addActionListenerToField(ActionListener al) {
		field.addActionListener(al);
	}
	
	public void addActionListenerToButton(ActionListener al) {
		button.addActionListener(al);
	}
	
	/**
	 * Enables or disables both the field and the button. 
	 * @param b true to enable, false to disable
	 */
	public void setEnabled(boolean b) {
		button.setEnabled(b);
		field.setEnabled(b);
	}
	
}
