package piano;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import piano.Composition.NoSymbolFound;


public class MidiFormatter implements Formatter {

	@Override
	public void export(String fileName, Composition composition) throws FileNotFoundException, UnsupportedEncodingException {
		try {
			Sequence sequence = new Sequence(Sequence.PPQ, 24);
			Track track = sequence.createTrack();
			byte[] header = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
			SysexMessage sysexMessage = new SysexMessage();
			sysexMessage.setMessage(header, 6);
			long actionTime=0, tpq=48;
			
			MidiEvent midiEvent = new MidiEvent(sysexMessage, actionTime);
			track.add(midiEvent);
			
			MetaMessage metaMessage = new MetaMessage();
			byte[] metaHeader = {0x02, (byte)0x00, 0x00};
			metaMessage.setMessage(0x51, metaHeader, 3);
			
			midiEvent = new MidiEvent(metaMessage, actionTime);
			track.add(midiEvent);
			
			ShortMessage shortMessage = new ShortMessage();
			shortMessage.setMessage(0xC0, 0x00, 0x00);
			
			midiEvent = new MidiEvent(shortMessage, (long)0);
			track.add(midiEvent);
			actionTime = 1;
			
//			Ovaj deo do for petlje je uvek isti tu se postavljaju neka zaglavlja
			for(int i = 0; i < composition.size(); i++) {
				MusicSymbol symbol = null;
				try { symbol = composition.get(i); } catch(NoSymbolFound e) { continue; }
				int rhythm;
				if(symbol.getDuration() == MusicSymbol.FOURTH) rhythm = 2;
				else rhythm = 1;
				
				if(symbol instanceof Note) {
					Note note = (Note) symbol;
					int midiCode = NoteMaps.StringToInteger.get(note.toString());
					
					shortMessage = new ShortMessage();
//					0x90 je kod za pustanje tona
					shortMessage.setMessage(0x90, midiCode, 100);
					midiEvent = new MidiEvent(shortMessage, actionTime);
					track.add(midiEvent);
					shortMessage = new ShortMessage();
//					0x80 je kod za zaustavljanje tona
					shortMessage.setMessage(0x80, midiCode, 100);
					midiEvent = new MidiEvent(shortMessage, actionTime + tpq * rhythm);
					track.add(midiEvent);
				} else if(symbol instanceof Chord) {
					for(int j = 0; j < ((Chord) symbol).size(); j++) {
						Note note = ((Chord) symbol).get(j);
						int midiCode = NoteMaps.StringToInteger.get(note.toString());
						long action = actionTime;
						shortMessage = new ShortMessage();
						shortMessage.setMessage(0x90, midiCode, 100);
						midiEvent = new MidiEvent(shortMessage, action);
						track.add(midiEvent);
						action += tpq * rhythm;
						shortMessage = new ShortMessage();
						shortMessage.setMessage(0x80, midiCode, 100);
						midiEvent = new MidiEvent(shortMessage, action);
						track.add(midiEvent);
					}
				}
				actionTime += tpq * rhythm;
			}
			actionTime += tpq;
			metaMessage = new MetaMessage();
			byte[] ending = {};
			metaMessage.setMessage(0x2F, ending, 0);
			midiEvent = new MidiEvent(metaMessage, actionTime);
			track.add(midiEvent);
			File file = new File(fileName);
			MidiSystem.write(sequence, 1, file);
		} catch (InvalidMidiDataException | IOException e) {}
	}			
}
