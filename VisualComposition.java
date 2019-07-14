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
	
	//SVE STO JE POTREBNO ZA CRTANJE
	//index prvog simbola koji se iscrtava
	private int index = 0;
	//Sluzi da bi se znalo odakle se pocinje sa crtanjem linija koje oznacavaju cetvrtine.
	//Kada se odsvira osmina kompozicija se pomera za pola cetvrtine i
	//ta linija treba da bude na pola prve cetvrtine
	private int offset = 0;
	//sluzi da biramo da li se note crtaju sa ispisom visine (C#4) ili
	//sa ispisom slova na tastaturi koje svira taj ton
	private boolean writeType = true;
//	U tekstu pise da se cetvrtine boje crvenom, osmine zelenom pa se zato zovu
//	NOTE_RED i NOTE_GREEN. Posle sam izmenio boje, ali me mrzelo da menjam ime
	private static final Color NOTE_RED = new Color(255, 193, 7);
	private static final Color NOTE_GREEN = new Color(201, 201, 201);
	private static final Color PAUSE_RED = new Color(255,131,7);
	private static final Color PAUSE_GREEN = new Color(150, 150, 150);
	private static final Color TEXT_COLOR = new Color(86, 86, 86);
	private static final Color LINE_COLOR = Color.WHITE;
	
//	Sirina cetvrtine pri iscrtavanju
	private static final int FOURTH_WIDTH = 40;
	private static final int EIGHT_WIDTH = FOURTH_WIDTH / 2;
//	Visina simbola pri iscrtavanju
	private static final int SYMBOL_HEIGHT = 30;
	private static final int FONT_SIZE = 14;
//	offset po Y osi - visina celog polja - visina fonta pa se to podeli sa dva
//	jer treba da bude centrirano pa se taj ostatak deli jednako iznad i ispod simbola
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
		//x left, x right, y up, y down
		int xl = 0, width, yu = 0;
		for(int i = index; i < composition.size(); i++) {
			MusicSymbol symbol = null;
			try { symbol = composition.get(i); } catch (NoSymbolFound e) {}
			double duration = symbol.getDuration();
			Color symbolColor = null;
			//racunamo sirinu
			if(duration == MusicSymbol.EIGHT)
				width = EIGHT_WIDTH;
			else
				width = FOURTH_WIDTH;
			//crtamo sve dok polja mogu da stanu u Canvas
			if(xl + width >= getWidth()) break;
			
			//nalazimo boju
			if(symbol instanceof Pause) {
				if(duration == MusicSymbol.EIGHT)
					symbolColor = PAUSE_GREEN;
				else
					symbolColor = PAUSE_RED;
			} else if(duration == MusicSymbol.EIGHT)
				symbolColor = NOTE_GREEN;
			else symbolColor = NOTE_RED;
			
			//polja crtamo na sredini Canvasa
			//posto simbol ima visinu SYMBOL_HEIGHT
			//onda je sredina na visina / 2 - SYMBOL_HEIGHT / 2
			yu = getHeight() / 2 - SYMBOL_HEIGHT / 2;
			g.setColor(symbolColor);
			if(symbol instanceof Chord) {
				//ako je simbol akord onda ima visinu SYMBOL_HEIGHT * akord.size()
				//onda je sredina Canvasa na height / 2 - SYMBOL_HEIGHT * akord.size() / 2;
				yu = getHeight() / 2 - SYMBOL_HEIGHT * ((Chord) symbol).size() / 2;
				for(int j = 0; j < ((Chord) symbol).size(); j++) {
					g.fillRect(xl, yu, width, SYMBOL_HEIGHT);
					//posto crtamo akord moramo da napisemo ime note crnom bojom
					g.setColor(TEXT_COLOR);
					//biramo da li se ispisuje opis note ili slovo sa tastature
					String description = getDescription(((Chord) symbol).get(j));
					g.drawString(description, xl + getXOffset(description, width), yu + SYMBOL_HEIGHT - Y_FONT);
					//vracamo staru boju za naredni simbol
					g.setColor(symbolColor);
					//posto crtamo akord svako polje mora da je jedno ispod drugog
					yu = yu + SYMBOL_HEIGHT;
				}
			} else {
				//ako simbol nije akord nacrtamo polje i proverimo da li je nota
				g.fillRect(xl, yu, width, SYMBOL_HEIGHT);
				//ako je nota onda treba da iscrtamo i opis
				if(symbol instanceof Note) {
					g.setColor(TEXT_COLOR);
					String description = getDescription((Note) symbol);
					g.drawString(description, xl + getXOffset(description, width), yu + SYMBOL_HEIGHT - Y_FONT);
				}
			}
			//desna ivica trenutnog simbola postaje leva ivica narednog
			xl += width;
		}
	}
	
	private void drawLines(Graphics g) {
//		ako ne postoji nit koja svira, ili postoji nit ali nije ziva jer je odsvirala kompoziciju
//		onda ne treba crtati linije koje oznacavaju cetvrtine
		if(playingThread == null || (playingThread != null && !playingThread.isAlive()))
			return;
		g.setColor(LINE_COLOR);
		for(int i = 0 + offset * EIGHT_WIDTH; i < getWidth(); i += FOURTH_WIDTH) {
			g.fillRect(i, getHeight() * 7 / 10, 2, getHeight() * 2 / 10);
		}
	}
	
	//ova metoda vraca opis koji treba da se ispise na canvasu
	//opis je ili slovo sa tastature ili opis visine
	private String getDescription(Note note) {
		String description = note.toString();
//		ako je izabran drugi tip ispisa onda se ispisuje karakter, ne opis note
		if(!writeType) description = String.valueOf(NoteMaps.StringToCharacter.get(description));
		return description;
	}
	
	//metoda vraca offset koliko treba opis da bude pomeren
	//od leve ivice da bi bio centriran
	//pretpostavka je da je sirina jednog slova 3/5 velicine fonta
	//(ovo je nebitno, to sam se ja glupirao da slova budu centrirana)
	private int getXOffset(String description, int width) {
		int xoff = description.length() * FONT_SIZE * 3 / 5;
		xoff = (width - xoff) / 2;
		return xoff;
	}
	
	//unutrasnja klasa koja predstavlja nit koja svira kompoziciju
	private class PlayingThread extends Thread {
		private boolean running = false;
		
		private PlayingThread() {
			//ova nit je demonska da bi se sama ugasila kad se ugasi program, jer onda
			//ne moram da brinem i vodim racuna da li postoji neka aktivna nit i ko ce da je gasi
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
					//ovo se radi jer se kompozicija iscrtava pocevsi od
					//simbola na indeksu index + 1
					index = i + 1;
					//ako je odsvirana osmina onda ce offset biti postavljen na 1
					//ako je odsvirana cetvrtina offset ce biti postavljen na 0
					//u paintu() se linije iscrtavaju pocevsi od koordinate offset * EIGHT_WIDTH
					//znaci ako je odsvirana cetvrtina crtace se od x = 0,
					//ako je odsvirana osmina crtace se od x = EIGHT_WIDTH
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
		//ako ima neka nit koja je ziva onda nastavi s njom
		//(mozda je nit pauzirana pa sad hoce da nastavi od tog trenutka)
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
	
	//poziva se repaint() da bi se videlo da se sad nalaze slova / note
	public void showNotes() { writeType = true; repaint(); }
	public void showLetters() { writeType = false; repaint(); }
	
	public void join() throws InterruptedException {
		if(playingThread != null)
			playingThread.join();
	}
}
