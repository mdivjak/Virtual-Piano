package piano;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.FileNotFoundException;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import piano.Composition.NoSymbolFound;

public class VisualComposition extends Canvas {
	private Composition composition = new Composition();
	private PlayingThread playingThread = null;
	private Piano piano;
	
	private int index = 0;
	private int offset = 0;
	private boolean writeType = true;
	private static final Color NOTE_RED = new Color(255, 193, 7);
	private static final Color NOTE_GREEN = new Color(201, 201, 201);
	private static final Color PAUSE_RED = new Color(255,131,7);
	private static final Color PAUSE_GREEN = new Color(150, 150, 150);
	private static final Color TEXT_COLOR = new Color(86, 86, 86);
	private static final Color LINE_COLOR = Color.WHITE;
	
	private static final int FOURTH_WIDTH = 40;
	private static final int EIGHT_WIDTH = FOURTH_WIDTH / 2;
	private static final int SYMBOL_HEIGHT = 30;
	private static final int FONT_SIZE = 14;
	private static final int Y_FONT = (SYMBOL_HEIGHT - FONT_SIZE) / 2;
	
	public VisualComposition(Piano piano) {
		this.piano = piano;
	}
	
	@Override
	public void paint(Graphics g) {
		drawMusicSymbols(g);
		drawLines(g);
	}
	
	private void drawMusicSymbols(Graphics g) {
		g.setFont(new Font(null, Font.BOLD, FONT_SIZE));
		int xl = 0, width, yu = 0;
		for(int i = index; i < composition.size(); i++) {
			MusicSymbol symbol = null;
			try { symbol = composition.get(i); } catch (NoSymbolFound e) { continue; }
			double duration = symbol.getDuration();
			Color symbolColor = null;
			if(duration == MusicSymbol.EIGHT)
				width = EIGHT_WIDTH;
			else
				width = FOURTH_WIDTH;
			if(xl + width >= getWidth()) break;
			
			if(symbol instanceof Pause) {
				if(duration == MusicSymbol.EIGHT)
					symbolColor = PAUSE_GREEN;
				else
					symbolColor = PAUSE_RED;
			} else if(duration == MusicSymbol.EIGHT)
				symbolColor = NOTE_GREEN;
			else symbolColor = NOTE_RED;
			
			yu = getHeight() / 2 - SYMBOL_HEIGHT / 2;
			g.setColor(symbolColor);
			if(symbol instanceof Chord) {
				yu = getHeight() / 2 - SYMBOL_HEIGHT * ((Chord) symbol).size() / 2;
				for(int j = 0; j < ((Chord) symbol).size(); j++) {
					g.fillRect(xl, yu, width, SYMBOL_HEIGHT);
					g.setColor(TEXT_COLOR);
					String description = getDescription(((Chord) symbol).get(j));
					g.drawString(description, xl + getXOffset(description, width), yu + SYMBOL_HEIGHT - Y_FONT);
					g.setColor(symbolColor);
					yu = yu + SYMBOL_HEIGHT;
				}
			} else {
				g.fillRect(xl, yu, width, SYMBOL_HEIGHT);
				if(symbol instanceof Note) {
					g.setColor(TEXT_COLOR);
					String description = getDescription((Note) symbol);
					g.drawString(description, xl + getXOffset(description, width), yu + SYMBOL_HEIGHT - Y_FONT);
				}
			}
			xl += width;
		}
	}
	
	private void drawLines(Graphics g) {
		if(playingThread == null || (playingThread != null && !playingThread.isAlive()))
			return;
		g.setColor(LINE_COLOR);
		for(int i = 0 + offset * EIGHT_WIDTH; i < getWidth(); i += FOURTH_WIDTH) {
			g.fillRect(i, getHeight() * 7 / 10, 2, getHeight() * 2 / 10);
		}
	}
	
	private String getDescription(Note note) {
		String description = note.toString();
		if(!writeType) description = String.valueOf(NoteMaps.StringToCharacter.get(description));
		return description;
	}

	private int getXOffset(String description, int width) {
		int xoff = description.length() * FONT_SIZE * 3 / 5;
		xoff = (width - xoff) / 2;
		return xoff;
	}
	
	private class PlayingThread extends Thread {
		private boolean running = false;
		
		private PlayingThread() {
			setDaemon(true);
			start();
		}
		
		public synchronized void play() { running = true; notify(); }
		public synchronized void pause() { running = false; }
		public synchronized void halt() { interrupt(); }
		
		public void run() {
			try {
				for(int i = 0; i < composition.size(); i++) {
					while(!running) synchronized(this) { wait(); }
					if(interrupted()) break;
					playMusicSymbol(composition.get(i));
					index = i + 1;
					if(composition.get(i).getDuration() == MusicSymbol.EIGHT)
						offset = (offset + 1) % 2;
					else offset = (offset + 2) % 2;
					repaint();
				}
			} catch(InterruptedException | NoSymbolFound e) {}
		}
		
		private void playMusicSymbol(MusicSymbol symbol) throws InterruptedException {
			symbol.startPlaying();
			symbol.pressOnPiano(piano);
			Thread.sleep((symbol.getDuration() == MusicSymbol.EIGHT) ? MidiPlayer.EIGHT_LENGTH : MidiPlayer.FOURTH_LENGTH);
			symbol.stopPlaying();
			symbol.releaseOnPiano(piano);
		}
	}
	
	public void play() {
		if(playingThread != null && playingThread.isAlive()) {
			playingThread.play();
		} else {
			playingThread = new PlayingThread();
			playingThread.play();
		}
	}
	
	public void pause() {
		if(playingThread != null)
			playingThread.pause();
	}
	
	public void stop() {
		if(playingThread != null)
			playingThread.halt();
	}
	
	public void loadFromFile(String fileName) throws FileNotFoundException {
		composition.loadFromFile(fileName);
		playingThread = new PlayingThread();
		offset = 0;
		index = 0;
		repaint();
	}
	
	public void showNotes() { writeType = true; repaint(); }
	public void showLetters() { writeType = false; repaint(); }
	
	public void join() throws InterruptedException {
		if(playingThread != null)
			playingThread.join();
	}
}
