package util;

import java.util.ArrayList;

public class Enabler {

	@FunctionalInterface
	public static interface Enableable {
		void setEnabled(boolean b);
	}

	@FunctionalInterface
	public static interface Condition {
		boolean check();
	}

	private ArrayList<Enableable[]> enableables = new ArrayList<>();
	private ArrayList<Condition[]> conditions = new ArrayList<>();

	/**
	 * Adds the given enableables and conditions as a single pair. When {@link Enabler#updateEnableds()} is called, all
	 * of the given conditions will be checked. Then, all of the given enableables will be set to the conjunction of the
	 * conditions.
	 * 
	 * @param es list of enableables, which all share the given conditions
	 * @param cs list of conditions, all of which must be met for enableables to be enabled
	 */
	public void add(Enableable[] es, Condition[] cs) {
		enableables.add(es);
		conditions.add(cs);
	}

	public void updateEnableds() {
		for (int i = 0; i < enableables.size(); i++) {
			boolean isEnabled = true;
			for (Condition c : conditions.get(i))
				isEnabled = isEnabled && c.check();
			for (Enableable e : enableables.get(i))
				e.setEnabled(isEnabled);
		}
	}

	// convenience methods

	public void add(Enableable e, Condition... cs) {
		add(new Enableable[] { e }, cs);
	}

	public void add(Condition c, Enableable... es) {
		add(es, new Condition[] { c });
	}
}
