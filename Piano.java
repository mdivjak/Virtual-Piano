package piano;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import piano.Composition.NoSymbolFound;

public class Piano extends Canvas {
	private static final Color PRESSED_KEY = new Color(255, 193, 7);
	private static final Color BACKGROUND = new Color(86, 86, 86);
	
	private static final int NUM_KEYS = 35;
	private static final int NUM_BLACK_KEYS = 25;
	
	private static final String[] noteHeights = new String[] {"C", "D", "E", "F", "G", "A", "B"};
	
	private ArrayList<Integer> currentlyPressed = new ArrayList<>();
	private MouseEvent click = null;
	private Key[] blackKeys = new Key[NUM_BLACK_KEYS];
	private Key[] whiteKeys = new Key[NUM_KEYS];
	private boolean showHelp = false;
	private boolean recording = false;
	private Composition recordedComposition;
	private long lastTimeRecorded;
	private long lastPlayed;
	private static final long MAX_CHORD_TIMEOUT = 30;
	private static final long MAX_FOURTH_TIMEOUT = 500;
	private static final long MAX_EIGHT_TIMEOUT = MAX_FOURTH_TIMEOUT / 2;
	private static final long MIN_EIGHT_TIMEOUT = MAX_EIGHT_TIMEOUT / 3;
	
	private static class NoKeyException extends Exception {}
	
	private static class Key {
		private int xl, xr, yu, yd, midiCode;
		
		public Key(int xl, int xr, int yu, int yd, int midiCode) {
			super();
			this.xl = xl;
			this.xr = xr;
			this.yu = yu;
			this.yd = yd;
			this.midiCode = midiCode;
		}
		
		private int getMidiCode() { return midiCode; }

		private boolean inside(int x, int y) {
			return x >= xl && x <= xr && y >= yu && y <= yd;
		}
	}
	
	public Piano() {
		setBackground(Color.WHITE);
		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				click = e;
				try {
					int midiCode = pressedNote(click);
					MidiPlayer.play(midiCode);
					if(!currentlyPressed.contains(Integer.valueOf(midiCode)))
						currentlyPressed.add(Integer.valueOf(midiCode));
					recordPauses();
					repaint();
				} catch(NoKeyException err) {}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					int midiCode = pressedNote(click);
					MidiPlayer.release(midiCode);
					while(currentlyPressed.remove(Integer.valueOf(midiCode)));
					recordNotes(midiCode);
					repaint();
				} catch(NoKeyException err) {}
			}
			
		});
		
		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				try {
					Character characterPressed = e.getKeyChar();
					int midiCode = convertCharacterToInteger(characterPressed);
					MidiPlayer.play(midiCode);
					if(!currentlyPressed.contains(Integer.valueOf(midiCode)))
						currentlyPressed.add(Integer.valueOf(midiCode));
					recordPauses();
					repaint();
				} catch(NoKeyException err) {} 
			}

			@Override
			public void keyReleased(KeyEvent e) {
				try {
					Character characterPressed = e.getKeyChar();
					int midiCode = convertCharacterToInteger(characterPressed);
					MidiPlayer.release(midiCode);
					while(currentlyPressed.remove(Integer.valueOf(midiCode)));
					recordNotes(midiCode);
					repaint();
				} catch(NoKeyException err) {}
			}
			
		});
	}
	
	private int convertCharacterToInteger(Character c) throws NoKeyException {
		String description = NoteMaps.CharacterToString.get(c);
		if(description == null) throw new NoKeyException();
		return NoteMaps.StringToInteger.get(description);
	}
	
	public void showNote(Note note) {
		int midiCode = NoteMaps.StringToInteger.get(note.toString());
		if(!currentlyPressed.contains(Integer.valueOf(midiCode)))
			currentlyPressed.add(Integer.valueOf(midiCode));
		repaint();
	}
	
	public void removeNote(Note note) {
		int midiCode = NoteMaps.StringToInteger.get(note.toString());
		while(currentlyPressed.remove(Integer.valueOf(midiCode)));
		repaint();
	}
	
	private int pressedNote(MouseEvent e) throws NoKeyException {
		for(int i = 0; i < NUM_BLACK_KEYS; i++) {
			if(blackKeys[i].inside(e.getX(), e.getY())) {
				return blackKeys[i].getMidiCode();
			}
		}
		for(int i = 0; i < NUM_KEYS; i++) {
			if(whiteKeys[i].inside(e.getX(), e.getY())) {
				return whiteKeys[i].getMidiCode();
			}
		}
		throw new NoKeyException();
	}
	
	@Override
	public void paint(Graphics g) {
		int quarterWidth = (getWidth() / NUM_KEYS) / 4;
		int keyWidth = quarterWidth * 4;
		int offset = (getWidth() - NUM_KEYS * keyWidth) / 2;
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, offset, getHeight());
		g.fillRect(NUM_KEYS * keyWidth + offset, 0, offset + 1, getHeight());
		g.setColor(Color.BLACK);
		for(int i = 0; i < NUM_KEYS; i++) {
			int xl, xr, yu, yd;
			xl = i * keyWidth + offset;
			yu = 0;
			xr = xl + keyWidth;
			yd = getHeight() - 1;
			String description = noteHeights[i % 7] + (i / 7 + 2);
			int midiCode = NoteMaps.StringToInteger.get(description);
			whiteKeys[i] = new Key(xl, xr, yu, yd, midiCode);
			
			g.drawRect(xl, yu, keyWidth, getHeight() - 1);
			if(currentlyPressed.contains(Integer.valueOf(midiCode))) {
				g.setColor(PRESSED_KEY);
				g.fillRect(xl + 1, yu + 1, xr - xl - 1, yd - yu - 1);
				g.setColor(Color.BLACK);
			}
			if(showHelp) {
				int helpOffset = (xr - xl) / 2 - 5;
				Character c = NoteMaps.StringToCharacter.get(NoteMaps.IntegerToString.get(midiCode));
				g.drawString(c.toString(), xl+ helpOffset, yd - 5);
			}
		}
		
		for(int i = 0, j = 0; i < NUM_KEYS; i++) {
			if(i % 7 == 2 || i % 7 == 6) continue;
			int xl, xr, yu, yd;
			xl = i * keyWidth + 3 * quarterWidth + offset;
			yu = 0;
			xr = xl + quarterWidth * 2;
			yd = getHeight() / 2;
			String description = noteHeights[i % 7] + "#" + (i / 7 + 2);
			int midiCode = NoteMaps.StringToInteger.get(description);
			blackKeys[j++] = new Key(xl, xr, yu, yd, midiCode);
			g.fillRect(xl, 0, quarterWidth * 2 , getHeight() / 2);
			
			if(currentlyPressed.contains(Integer.valueOf(midiCode))) {
				g.setColor(PRESSED_KEY);
				g.fillRect(xl, yu + 1, xr - xl, yd - yu - 1);
				g.setColor(Color.BLACK);
			}
			if(showHelp) {
				g.setColor(Color.WHITE);
				int helpOffset = (xr - xl) / 2 - 5;
				Character c = NoteMaps.StringToCharacter.get(NoteMaps.IntegerToString.get(midiCode));
				g.drawString(c.toString(), xl+ helpOffset, yd - 5);
				g.setColor(Color.BLACK);
			}
		}
	}
	
	public void startRecording() {
		recording = true;
		recordedComposition = new Composition();
		lastTimeRecorded = System.currentTimeMillis();
	}
	
	public void stopRecording() { recording = false; }
	
	public void recordPauses() {
		if(!recording) return;
		lastPlayed = System.currentTimeMillis();
		long currentTime = lastPlayed;
		long silence = currentTime - lastTimeRecorded;
		for(int i = 0; i < silence / MAX_FOURTH_TIMEOUT; i++) {
			recordedComposition.add(new Pause(MusicSymbol.FOURTH));
		}
		if(silence % MAX_FOURTH_TIMEOUT >= MIN_EIGHT_TIMEOUT && silence % MAX_FOURTH_TIMEOUT <= MAX_EIGHT_TIMEOUT)
			recordedComposition.add(new Pause(MusicSymbol.EIGHT));
	}
	
	public void recordNotes(int midiCode) {
		if(!recording) return;
		long currentTime = System.currentTimeMillis();
		long duration = currentTime - lastPlayed;
		if(currentTime - lastTimeRecorded < MAX_CHORD_TIMEOUT) {
			try {
				MusicSymbol symbol = recordedComposition.remove(recordedComposition.size() - 1);
				if(symbol instanceof Pause) {
					recordedComposition.add(symbol);
					addNotes(midiCode, duration);
				} else if(symbol instanceof Note) {
					Chord chord = new Chord();
					chord.add((Note) symbol);
					chord.add(new Note(NoteMaps.IntegerToString.get(Integer.valueOf(midiCode)), MusicSymbol.FOURTH));
					recordedComposition.add(chord);
				} else if(symbol instanceof Chord){
					((Chord) symbol).add(new Note(NoteMaps.IntegerToString.get(Integer.valueOf(midiCode)), MusicSymbol.FOURTH));
					recordedComposition.add(symbol);
				}
			} catch(NoSymbolFound e) {
				addNotes(midiCode, duration);
			}
		} else addNotes(midiCode, duration);
		lastTimeRecorded = System.currentTimeMillis();
	}
	
	private void addNotes(int midiCode, long duration) {
		if(duration < MAX_EIGHT_TIMEOUT)
			addEightNotes(midiCode);
		else addFourthNotes(midiCode, duration);
	}
	
	private void addEightNotes(int midiCode) {
		recordedComposition.add(new Note(NoteMaps.IntegerToString.get(Integer.valueOf(midiCode)), MusicSymbol.EIGHT));
	}
	
	private void addFourthNotes(int midiCode, long duration) {
		for(int i = 0; i < duration / MAX_FOURTH_TIMEOUT; i++)
			recordedComposition.add(new Note(NoteMaps.IntegerToString.get(Integer.valueOf(midiCode)), MusicSymbol.FOURTH));
		if(duration % MAX_FOURTH_TIMEOUT >= MIN_EIGHT_TIMEOUT && duration % MAX_FOURTH_TIMEOUT <= MAX_EIGHT_TIMEOUT)
			recordedComposition.add(new Note(NoteMaps.IntegerToString.get(Integer.valueOf(midiCode)), MusicSymbol.EIGHT));
	}
	
	public Composition getRecordedComposition() {
		stopRecording();
		return recordedComposition;
	}
	
	public void setShowHelp(boolean showHelp) {
		this.showHelp = showHelp;
		repaint();
	}
}
