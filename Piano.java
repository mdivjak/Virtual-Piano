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
	
//	broj belih dirki
	private static final int NUM_KEYS = 35;
//	broj crnih dirki
	private static final int NUM_BLACK_KEYS = 25;
	
	private static final String[] noteHeights = new String[] {"C", "D", "E", "F", "G", "A", "B"};
	
//	midi kodovi tonova koji su trenutno pritisnuti na klaviru
	private ArrayList<Integer> currentlyPressed = new ArrayList<>();
	private MouseEvent click = null;
	private Key[] blackKeys = new Key[NUM_BLACK_KEYS];
	private Key[] whiteKeys = new Key[NUM_KEYS];
	private boolean showHelp = false;
	//SVE STO JE POTREBNO ZA SNIMANJE KOMPOZICIJE
	private boolean recording = false;
	private Composition recordedComposition;
	private long lastTimeRecorded;
	private long lastPlayed;
	private static final long MAX_CHORD_TIMEOUT = 30;
	private static final long MAX_FOURTH_TIMEOUT = 500;
	private static final long MAX_EIGHT_TIMEOUT = MAX_FOURTH_TIMEOUT / 2;
	private static final long MIN_EIGHT_TIMEOUT = MAX_EIGHT_TIMEOUT / 3;
	
	//tip greske koja se baca kada se klikne misem van klavira
	private static class NoKeyException extends Exception {}
	
	//Ova klasa sluzi da bih prepoznao gde je kliknut mis
	//Klasa cuva koordinate leve, desne, gornje, donje ivice dirke i midi kod tona koji predstavlja ta dirka
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
//				Potrebno je da sacuvam ovaj MouseEvent
//				Kada se otpusti mis MouseEvent ce imati koordinate mesta gde je otpusten mis
//				Ne smeju da se koriste te koordinate jer je neko mozda kliknuo, pomerio misa i onda odpustio
//				Onda se nece zaustaviti ton koji je pritisnut nego ton gde je otpusten mis
//				sto nema efekta jer taj ton nije pritisnut i ne svira se
				click = e;
				try {
					int midiCode = pressedNote(click);
					MidiPlayer.play(midiCode);
					//ne zelimo da dvaput dodamo isti ton pa ga ubacujemo samo ako vec
					//nije u listi pritisnutih tonova
					if(!currentlyPressed.contains(Integer.valueOf(midiCode)))
						currentlyPressed.add(Integer.valueOf(midiCode));
//					snimi sve pauze koje su prosle izmedju poslednje dodate note i 
//					ovog trenutka kada je odsvirana prva sledeca nota
					recordPauses();
					repaint();
				} catch(NoKeyException err) {}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					int midiCode = pressedNote(click);
					MidiPlayer.release(midiCode);
//					remove() vraca true ako je nesto obrisano
//					sve dok brisemo ovaj midi kod iz liste vrtimo se u petlji
//					ne mora da se napise petlja, ali za svaki slucaj, ako se nekako
//					desi da se ubace dva ista tona u listu
					while(currentlyPressed.remove(Integer.valueOf(midiCode)));
//					snimi notu koja je svirana u trajanju lastPlayed - trenutno_vreme 
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
	
//	NoteMaps klasa sadrzi mapu CharacterToString i StringToInteger, nema CharacterToInteger
//	Prvo se nadje string koji odgovara karakteru a ako on ne postoji baca se izuzetak
//	Ovo se desava kada je kliknut taster koji ne oznacava nijedan ton na klaviru (enter, space itd)
//	Ako u mapi nadjemo string onda se vraca midi kod koji odgovara tom stringu
	private int convertCharacterToInteger(Character c) throws NoKeyException {
		String description = NoteMaps.CharacterToString.get(c);
		if(description == null) throw new NoKeyException();
		return NoteMaps.StringToInteger.get(description);
	}
	
//	Prosledjuje se nota koja se iscrtava na klaviru
	public void showNote(Note note) {
		int midiCode = NoteMaps.StringToInteger.get(note.toString());
		if(!currentlyPressed.contains(Integer.valueOf(midiCode)))
			currentlyPressed.add(Integer.valueOf(midiCode));
		repaint();
	}
	
//	Prosledjuje se nota koja se uklanja sa klavira
	public void removeNote(Note note) {
		int midiCode = NoteMaps.StringToInteger.get(note.toString());
		while(currentlyPressed.remove(Integer.valueOf(midiCode)));
		repaint();
	}
	
//	Prodje kroz sve crne dirke pa kroz sve bele dirke da proveri da li je neka od njih pritisnuta
//	Ako nijedna nije pritisnuta baca se izuzetak jer je mis kliknut van klavira
//	Prvo se proveravaju crne dirke jer se one nalaze preklopljene preko belih
//	Ako je pritisnuta crna dirka istovremeno se mis nalazi i u okviru bele, pa ako
//	bi se prvo proveravale bele dirke ne bi se registrovao klik na crnu dirku
//	Ako nadje dirku koja je pritisnuta vraca njen midi kod
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
//		Kad smo ja i Barjak pravili imali smo problem kod registrovanja klika na dirku
//		Treba na osnovu x i y koordinate klika zakljuciti koja je dirka kliknuta
//		Posto ima 35 belih dirki problem je ako sirina Canvasa nije deljiva sa 35
//		Tada ce sirina jedne dirke biti jednaka nekom broju i bice neki ostatak
//		Svaka dirka ce se crtati sa sirinom jednakom celom broju (odbacivace se ostatak iza zareza)
//		Problem je sto ako je ostatak npr 1 onda sa crtanjem svake dirke gubi se jedan piksel sirine
//		Posle 35 nacrtanih dirki izgubice se 35 piksela. Klavir ce biti nacrtan i onda ce biti praznina 35 piksela
//		Problem je kad se klikne na neku dirku i racuna koja je kliknuta nece se poklapati ono sto je pritisnuto i
//		tamo gde je pritisnuto. Desavalo se da kliknemo na jednu dirku a odsvira se ova pored ili dve-tri pored
//		Kada se pritiskaju najlevlje dirke sve radi lepo ali onda sto se svira desnija dirka veci je gubitak zbog zaokruzivanja
//		i onda je veca razlika izmedju dirke koja je pritisnuta i one koja je odsvirana
		
//		Zato sirinu podelimo sa brojem dirki i onda sa jos 4 posto je to najmanji deo sirine dirke koji se koristi pri racunanju
//		Crne dirke pocinju na 3/4 bele dirke i siroke su pola bele dirke.
//		Prvo izracunam cetvrtinu sirine dirke i onda tu cetvrtinu pomnozim sa 4 da bih dobio koliko treba da bude siroka dirka
//		Time zanemarim onaj ostatak jer kad ceo broj podelim sa 4 pa pomnozim sa 4 ne dobije se isto ako sirina nije deljiva sa 4
//		Onda izracunam offset - to je koliko je klavijatura pomerena u odnosu na levu ivicu
//		Ovo sam dodao da bi klavijatura bila centrirana i da bi se taj ostatak jednako podelio na levu i desnu stranu
		int quarterWidth = (getWidth() / NUM_KEYS) / 4;
		int keyWidth = quarterWidth * 4;
//		Izracunam offset (koliko ostaje prostora sa leve i desne strane klavira)
//		Obojim u boju pozadine levi ostatak i desni ostatak
		int offset = (getWidth() - NUM_KEYS * keyWidth) / 2;
		g.setColor(BACKGROUND);
//		levi ostatak
		g.fillRect(0, 0, offset, getHeight());
//		desni ostatak pocinje na kraju poslednje dirke
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
		
//		Kad se crtaju crne dirke isto prolazim sa i=0 do 35 samo preskacem
//		ako je ostatak pri deljenju sa 7 jednak 2 ili 6
//		Posto ima 25 crnih dirki onda imam poseban brojac j za broj crne dirke
		for(int i = 0, j = 0; i < NUM_KEYS; i++) {
			if(i % 7 == 2 || i % 7 == 6) continue;
			int xl, xr, yu, yd;
//			Klavir je pomeren za offset od leve ivice i sve crne dirke su pomerene za 3/4 sirine dirke
//			zato sto prva crna dirka pocinje na 3/4 sirine prve bele dirke
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
	
	public void stopRecording() {
		recording = false;
	}
	
	public void recordPauses() {
		if(!recording) return;
//		azuriraj vreme kad je poceo da se sviraj trenutni ton
		lastPlayed = System.currentTimeMillis();
		long currentTime = lastPlayed;
		long silence = currentTime - lastTimeRecorded;
//		silence je vreme proslo od poslednjeg vremena kad je 
//		dodat neki simbol u kompoziciju i trenutnog vremena
//		To vreme podelimo sa trajanjem cetvrtine i dodamo toliko cetvrtinskih pauza
		for(int i = 0; i < silence / MAX_FOURTH_TIMEOUT; i++) {
			recordedComposition.add(new Pause(MusicSymbol.FOURTH));
		}
//		ako je ostatak dovoljno dug dodamo i jednu osminsku pauzu
		if(silence % MAX_FOURTH_TIMEOUT >= MIN_EIGHT_TIMEOUT && silence % MAX_FOURTH_TIMEOUT <= MAX_EIGHT_TIMEOUT)
			recordedComposition.add(new Pause(MusicSymbol.EIGHT));
	}
	
	public void recordNotes(int midiCode) {
		if(!recording) return;
		long currentTime = System.currentTimeMillis();
		long duration = currentTime - lastPlayed;
//		Ako je vreme izmedju ovog tona i poslednjeg dodatog tona manje od MAX_CHORD_TIMEOUT
//		Probaj da napravis akord od poslednjeg dodatog simbola i ove note
		if(currentTime - lastTimeRecorded < MAX_CHORD_TIMEOUT) {
			try {
				MusicSymbol symbol = recordedComposition.remove(recordedComposition.size() - 1);
				if(symbol instanceof Pause) {
//					ako je poslednji simbol pauza vrati je i dodaj notu koja je odsvirana u trajanju duration
					recordedComposition.add(symbol);
					addNotes(midiCode, duration);
				} else if(symbol instanceof Note) {
//					ako je poslednji simbol nota napravi novi akord i dodaj je, dodaj notu koja je sad odsvirana
					Chord chord = new Chord();
					chord.add((Note) symbol);
					chord.add(new Note(NoteMaps.IntegerToString.get(Integer.valueOf(midiCode)), MusicSymbol.FOURTH));
					recordedComposition.add(chord);
				} else if(symbol instanceof Chord){
//					ako je poslednji simbol akord dodaj novu notu i vrati je u kompoziciju
					((Chord) symbol).add(new Note(NoteMaps.IntegerToString.get(Integer.valueOf(midiCode)), MusicSymbol.FOURTH));
					recordedComposition.add(symbol);
				}
			} catch(NoSymbolFound e) {
//				ovaj izuzetak se desava kad se prvi put pritisne neki ton pri snimanju
//				tada u snimljenoj kompoziciji nema nicega i remove(recordedComposition.size() - 1)
//				pokusava da ukloni simbol sa indeksa -1
//				Posto taj simbol ne postoji samo dodaj notu koja je upravo odsvirana
				addNotes(midiCode, duration);
			}
//			ako nije proslo dovoljno kratko vremena izmedju dodavanja poslednje note i trenutne note
//			onda dodaj note u zavisnosti od njihovog trajanja duration
		} else addNotes(midiCode, duration);
//		azuriraj vreme kad je dodat poslednji simbol u kompoziciju
		lastTimeRecorded = System.currentTimeMillis();
	}
	
	private void addNotes(int midiCode, long duration) {
//		ako je nota trajala krace od max vremena trajanja osmine onda dodaj osminu
//		u suprotnom dodaj cetvrtinu
		if(duration < MAX_EIGHT_TIMEOUT)
			addEightNotes(midiCode);
		else addFourthNotes(midiCode, duration);
	}
	
	private void addEightNotes(int midiCode) {
//		na osnovu midi koda izvuci opis note koji se prosledjuje konstruktoru note zajedno sa trajanjem
		recordedComposition.add(new Note(NoteMaps.IntegerToString.get(Integer.valueOf(midiCode)), MusicSymbol.EIGHT));
	}
	
	private void addFourthNotes(int midiCode, long duration) {
//		dodajemo cetvrtine duration / MAX_FOURTH_TIMEOUT puta
		for(int i = 0; i < duration / MAX_FOURTH_TIMEOUT; i++)
			recordedComposition.add(new Note(NoteMaps.IntegerToString.get(Integer.valueOf(midiCode)), MusicSymbol.FOURTH));
//		ako je ono sto je ostalo preko trajanja cetvrtine jednako trajanju osmine onda dodajemo i jednu osminu
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
