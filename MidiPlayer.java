package piano;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

//Ovu klasu koristi i nacrtana kompozicija i klavir
//Napravio sam je da se ne bi kopirao isti kod za pustanje tona u obe klase
public class MidiPlayer {
	//ove dve promenljive su potrebne, to se kopira iz teksta projekta
	private static MidiChannel channel;
	private static final int INSTRUMENT = 1;
	
	//sluze da odrede tempo sviranja
	//govore koliko ms traje cetvrtina tj osmina
	public static long FOURTH_LENGTH = 200;
	public static long EIGHT_LENGTH = FOURTH_LENGTH / 2;
	
	//staticki inicijalizacioni blok da se samo
	//pri ucitavanju klase trazi midi kanal
	//posle se uvek koristi taj isti midi kanal
	static {
		try {
			channel = getChannel(INSTRUMENT);
		} catch(MidiUnavailableException e) {}
	}
	
	//prepisano iz teksta projekta
	private static MidiChannel getChannel(int instrument) throws MidiUnavailableException {
		Synthesizer synthesizer = MidiSystem.getSynthesizer();
		synthesizer.open();
		return synthesizer.getChannels()[instrument];
	}
	
	//prepisano iz teksta projekta
	//metoda sluzi da odsvira notu koja ima
	//midi kod 'note'
	public static void play(final int note) {
		channel.noteOn(note, 50);
	}
	
	//prepisano iz teksta projekta
	//metoda sluzi da prestane da se svira
	//nota koja ima midi kod 'note'
	public static void release(final int note) {
		channel.noteOff(note, 50);
	}
}
