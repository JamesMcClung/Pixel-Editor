package canvas;

import java.awt.Color;

public class ToolParams {
	
	public ToolParams(Color color, int alpha, int size) {
		this.color = color;
		this.alpha = alpha;
		this.size = size;
	}
	
	public final Color color;
	public final int alpha;
	public final int size;
	
}
