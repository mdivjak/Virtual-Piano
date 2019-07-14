package piano;

abstract public class MusicSymbol {
	protected double duration;
	public static final double EIGHT = 1.0 / 8;
	public static final double FOURTH = 1.0 / 4;
	
	public MusicSymbol(double duration) {
		super();
		this.duration = duration;
	}

	public double getDuration() { return duration; }
	
	abstract public String toString();
	abstract public void startPlaying();
	abstract public void stopPlaying();
	abstract public void pressOnPiano(Piano piano);
	abstract public void releaseOnPiano(Piano piano);
}
