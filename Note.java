package piano;

public class Note extends MusicSymbol {
	private int octave;
	private char height;
	private boolean sharp = false;
	
//	Ovom konstruktoru se prosledjuje opis visine note i trajanje note
//	Npr ("C#4", MusicSymbol.FOURTH)
	public Note(String description, double duration) {
		super(duration);
		//visina je uvek prvo slovo
		height = description.charAt(0);
		//ako opis ima 2 slova onda nota nije povisena
		//broj oktave je drugi karakter
		if(description.length() == 2) {
//			string.charAt() vraca tip char koji je primitivan i ne moze da se kastuje u string
//			Mora da se pozove String.valueOf() koji ce da konvertuje char u String
//			pa se onda parsira sa Integer.parseInt() da bi se dobio broj oktave
//			posto parseInt prima striktno String, ne moze char
			octave = Integer.parseInt(String.valueOf(description.charAt(1)));
		} else {
			sharp = true;
			//ako je povisena broj oktave je treci karakter
			octave = Integer.parseInt(String.valueOf(description.charAt(2)));
		}
	}
	
	public String toString() {
		String description = String.valueOf(Character.toUpperCase(height));
		if(sharp) description = description + "#";
		description = description + octave;
		return description;
	}

	public int getOctave() {
		return octave;
	}

	public char getHeight() {
		return height;
	}

	public boolean isSharp() {
		return sharp;
	}

//	Ova metoda pocinje da svira ton note
//	MidiPlayeru se prosledjuje midi kod note
//	Midi kod se izvlaci iz mape
	@Override
	public void startPlaying() {
		MidiPlayer.play(NoteMaps.StringToInteger.get(toString()));
	}

//	Ova metoda prestaje da svira ton note
	@Override
	public void stopPlaying() {
		MidiPlayer.release(NoteMaps.StringToInteger.get(toString()));
	}
//pritiska dirku koja predstavlja ovu notu na klaviru piano
	@Override
	public void pressOnPiano(Piano piano) {
		piano.showNote(this);
	}

	@Override
	public void releaseOnPiano(Piano piano) {
		piano.removeNote(this);
	}
	
}
