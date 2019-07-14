package piano;

import java.util.ArrayList;

public class Chord extends MusicSymbol {
	private ArrayList<Note> notes = new ArrayList<>();
	
	//akordi uvek traju 1/4
	public Chord() {
		super(MusicSymbol.FOURTH);
	}
	
	public void add(Note note) {
//		prodjem kroz sve note i ako neka nota ima isti opis kao ova sto se dodaje
//		samo se vratim da ne bih dva puta dodavao istu notu
		for(Note n : notes) {
			if(n.toString().equals(note.toString()))
				return;
		}
//		u listu dodajem kopiju note sa istom visinom, samo sto ima trajanje cetvrtine
//		jer akordi traju samo cetvrtinu (mrzelo me da proveravam koliko traje nota pa zato pravim kopiju)
		notes.add(new Note(note.toString(), MusicSymbol.FOURTH));
	}
	
//	Ovom konstruktoru se prosledjuje opis visine note (A#4)
//	on u akord ubacuje notu te visine koja traje jednu cetvrtinu
	public void add(String description) {
		notes.add(new Note(description, MusicSymbol.FOURTH));
	}
	
	public int size() { return notes.size(); }
	public Note get(int i) {
		if(i >= 0 && i < notes.size())
			return notes.get(i);
		return null;
	}
	
//	ovaj ispis je beskoristan, koristio sam ga za proveru kad
//	sam proveravao da li se ispravno ucitava fajl da ga ispise u konzoli
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

//	metoda pocinje da svira sve note koje sadrzi akord
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

//	metoda pritiska sve dirke koje oznacavaju note u akordu
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
