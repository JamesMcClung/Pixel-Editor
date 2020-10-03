package util;

import java.awt.geom.Line2D;

public class Line extends Line2D.Float {
	private static final long serialVersionUID = 6594807073545204928L;

	public Line(int x1, int y1, int x2, int y2) {
		super(x1, y1, x2, y2);
	}

	@SuppressWarnings("preview")
	@Override
	public boolean equals(Object other) {
		if (other instanceof Line l)
			return l.x1 == x1 && l.x2 == x2 && l.y1 == y1 && l.y2 == y2;
		return false;
	}
	
	@Override
	public int hashCode() {
		return (int) x1  + (int) x2 << 8 + (int) y1 << 16 + (int) y2 << 24;
	}
}
