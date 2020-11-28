package piano;

import java.util.ArrayList;

public class Chord extends MusicSymbol {
	private ArrayList<Note> notes = new ArrayList<>();
	
	public Chord() {
		super(MusicSymbol.FOURTH);
	}
	
	public void add(Note note) {
		for(Note n : notes) {
			if(n.toString().equals(note.toString()))
				return;
		}
		notes.add(new Note(note.toString(), MusicSymbol.FOURTH));
	}
	
	public void add(String description) {
		notes.add(new Note(description, MusicSymbol.FOURTH));
	}
	
	public int size() { return notes.size(); }
	
	public Note get(int i) {
		if(i >= 0 && i < notes.size())
			return notes.get(i);
		return null;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for(int i = 0; i < notes.size(); i++) {
			sb.append(notes.get(i).toString());
			if(i != notes.size()-1)
				sb.append("_");
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public void startPlaying() {
		for(Note note: notes)
			MidiPlayer.play(NoteMaps.StringToInteger.get(note.toString()));
	}

	@Override
	public void stopPlaying() {
		for(Note note: notes)
			MidiPlayer.release(NoteMaps.StringToInteger.get(note.toString()));
	}

	@Override
	public void pressOnPiano(Piano piano) {
		for(Note note : notes)
			piano.showNote(note);
	}

	@Override
	public void releaseOnPiano(Piano piano) {
		for(Note note : notes)
			piano.removeNote(note);
	}
}
