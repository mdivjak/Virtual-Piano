package piano;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class MidiPlayer {
	private static MidiChannel channel;
	private static final int INSTRUMENT = 1;
	
	public static long FOURTH_LENGTH = 200;
	public static long EIGHT_LENGTH = FOURTH_LENGTH / 2;
	
	static {
		try {
			channel = getChannel(INSTRUMENT);
		} catch(MidiUnavailableException e) {}
	}
	
	private static MidiChannel getChannel(int instrument) throws MidiUnavailableException {
		Synthesizer synthesizer = MidiSystem.getSynthesizer();
		synthesizer.open();
		return synthesizer.getChannels()[instrument];
	}
	
	public static void play(final int note) {
		channel.noteOn(note, 50);
	}
	
	public static void release(final int note) {
		channel.noteOff(note, 50);
	}
}
