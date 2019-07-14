package piano;

public class Pause extends MusicSymbol {

	public Pause(double duration) {
		super(duration);
	}
	
	public String toString() {
		if(duration == MusicSymbol.FOURTH)
			return " ";
		return "|";
	}

	@Override
	public void startPlaying() {}
	@Override
	public void stopPlaying() {}
	@Override
	public void pressOnPiano(Piano piano) {}
	@Override
	public void releaseOnPiano(Piano piano) {}
}
