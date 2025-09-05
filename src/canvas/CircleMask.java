package canvas;

public class CircleMask implements BitMask {

	public CircleMask(int diameter) {
		radSq = Math.pow(diameter / 2d, 2);
		center = diameter / 2;

		boolean even = diameter % 2 == 0;
		size = even ? diameter + 1 : diameter;
	}

	private final double center;
	private final double radSq;
	private final int size;

	@Override
	public boolean get(int x, int y) {
		return Math.pow(x - center, 2) + Math.pow(y - center, 2) <= radSq;
	}

	@Override
	public int getWidth() {
		return size;
	}

	@Override
	public int getHeight() {
		return size;
	}

}
