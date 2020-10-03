package app;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public interface Renderable {
	
	void renderAt(Graphics2D g, AffineTransform tf);

}
