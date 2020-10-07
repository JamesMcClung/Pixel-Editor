package util;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LabeledSlider extends JSlider implements ChangeListener {
	private static final long serialVersionUID = 7937015603321589214L;
	
	public static final int width = 128, height = 18; // dimensions of slider
//	public static final int labelWidth = 32, labelHeight = 16; // dimensions of label

	public LabeledSlider(int min, int max, int initialVal) {
		super(min, max, initialVal);
		label = new JLabel("" + initialVal);
		Util.configureLabel(label, "" + max, true);
		addChangeListener(this);
		setMinimumSize(new Dimension(width, height));
		setPreferredSize(new Dimension(width, height));
		
		setFocusable(false); // because it's so freakin annoying
	}
	
	public final JLabel label;
	
	

	@Override
	public void stateChanged(ChangeEvent e) {
		label.setText("" + this.getValue());
	}
	
	@Override
	public void setVisible(boolean b) {
		label.setVisible(b);
		super.setVisible(b);
	}
	
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		if (label != null)
			label.setEnabled(b);
	}
}
