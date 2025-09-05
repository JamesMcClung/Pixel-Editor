package app;

public record SaveableState(CanvasPanel.State canvasState, SpritesheetManager.State ssmState, boolean isTransient) { 
	
	public SaveableState(CanvasPanel.State canvasState, SpritesheetManager.State ssmState) {
		this(canvasState, ssmState, false);
	}
	
}