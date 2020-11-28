package piano;

public class Note extends MusicSymbol {
	private int octave;
	private char height;
	private boolean sharp = false;
	
	public Note(String description, double duration) {
		super(duration);
		height = description.charAt(0);
		if(description.length() == 2) {
			octave = Integer.parseInt(String.valueOf(description.charAt(1)));
		} else {
			sharp = true;
			octave = Integer.parseInt(String.valueOf(description.charAt(2)));
		}
	}
	
	public String toString() {
		String description = String.valueOf(Character.toUpperCase(height));
		if(sharp) description = description + "#";
		description = description + octave;
		return description;
	}

	public int getOctave() { return octave;	}
	public char getHeight() { return height; }
	public boolean isSharp() { return sharp; }

	@Override
	public void startPlaying() {
		MidiPlayer.play(NoteMaps.StringToInteger.get(toString()));
	}

	@Override
	public void stopPlaying() {
		MidiPlayer.release(NoteMaps.StringToInteger.get(toString()));
	}

	@Override
	public void pressOnPiano(Piano piano) {
		piano.showNote(this);
	}

	@Override
	public void releaseOnPiano(Piano piano) {
		piano.removeNote(this);
	}
	
}
