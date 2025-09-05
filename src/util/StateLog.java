package util;

import java.util.LinkedList;

public class StateLog<T> {

	public StateLog(int maxNumStates) {
		currentState = -1;
		this.maxNumStates = maxNumStates;
	}

	private final LinkedList<T> states = new LinkedList<>();
	private int currentState;
	private int maxNumStates;

	public void saveState(T state) {
		// delete states after current state
		while (states.size() - 1 > currentState)
			states.removeLast();

		states.add(state);
		currentState++;

		removeOldStates();
	}

	public T getState() {
		return states.get(currentState);
	}

	/**
	 * If the log has a previous state, goes back to that and returns it. Otherwise returns null.
	 * 
	 * @return state after undoing
	 */
	public T undo() {
		if (canUndo()) {
			currentState--;
			return getState();
		}
		return null;
	}

	/**
	 * If the log has a next state, goes to that and returns it. Otherwise returns null.
	 * 
	 * @return state after redoing
	 */
	public T redo() {
		if (canRedo()) {
			currentState++;
			return getState();
		}
		return null;
	}

	public boolean canUndo() {
		return currentState > 0;
	}

	public boolean canRedo() {
		return currentState < states.size() - 1;
	}

	private void removeOldStates() {
		if (maxNumStates < 1)
			return;
		while (states.size() > maxNumStates) {
			states.removeFirst();
			currentState--;
		}
		if (currentState < 0)
			throw new Error("removed current state somehow");
	}

}
