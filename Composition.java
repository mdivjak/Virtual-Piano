package piano;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Composition {
	private ArrayList<MusicSymbol> symbols = new ArrayList<>();
	
//	klasa izuzetka koji se baca
	public class NoSymbolFound extends Exception {}
	
	public int size() { return symbols.size(); }
	
	public void add(MusicSymbol symbol) { symbols.add(symbol); }
	
	public MusicSymbol get(int i) throws NoSymbolFound {
		if(i < 0 || i >= symbols.size()) throw new NoSymbolFound();
		return symbols.get(i);
	}
	
	public MusicSymbol remove(int i) throws NoSymbolFound {
		if(i < 0 || i >= symbols.size()) throw new NoSymbolFound();
		MusicSymbol symbol = symbols.get(i);
		symbols.remove(i);
		return symbol;
	}
	
//	Metoda prima string koji predstavlja simbole koji nisu u uglastim zagradama
//	Znaci, oni mogu biti pauze (osminske ili cetvrtinske) i cetvrtinske note
	private void parseNotesAndPauses(String line) {
		for(int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			//razmak i '|' oznacavaju pauze
			if(c == '|') symbols.add(new Pause(MusicSymbol.FOURTH));
			else if(c == ' ') symbols.add(new Pause(MusicSymbol.EIGHT));
			//u suprotnom izvuci opis visine note iz mape
			//trajanje je uvek cetvrtina jer string koji se prosledi se ne nalazi u []
			else symbols.add(new Note(NoteMaps.CharacterToString.get(c), MusicSymbol.FOURTH));
		}
	}
	
//	Metoda prima string koji se nalazi u uglastim zagradama
//	Simboli koji mogu biti u ovom stringu su akordi ili osminske note
	private void parseChordsAndEights(String line) {
		//ako je u uglastim zagradama samo jedan karakter onda je to jedna osmina
		if(line.length() == 1) {
			symbols.add(new Note(NoteMaps.CharacterToString.get(line.charAt(0)), MusicSymbol.EIGHT));
			return;
		}
		//u suprotnom proveri drugi karakter
		//ako je drugi karakter razmak onda su u uglastim zagradama sve osmine
		//svaki drugi karakter je slovo koje predstavlja neku notu
		if(line.charAt(1) == ' ') {
			for(int i = 0; i < line.length(); i += 2) {
				symbols.add(new Note(NoteMaps.CharacterToString.get(line.charAt(i)), MusicSymbol.EIGHT));
			}
		//u suprotnom je akord
		} else {
			Chord chord = new Chord();
			for(int i = 0; i < line.length(); i++)
				chord.add(NoteMaps.CharacterToString.get(line.charAt(i)));
			symbols.add(chord);
		}
	}
	
//	Metoda ucitava kompoziciju iz nekog fajla
	public void loadFromFile(String fileName) throws FileNotFoundException {
//		prvo obrisemo sve iz kompozicije jer ucitavamo novu kompoziciju
		symbols.clear();
		File file = new File(fileName);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Stream<String> stringStream = reader.lines();
		
//		Slicno kao ucitavanje mapa u NoteMaps
//		Procita se fajl i napravi se stream stringova koji predstavljaju redove fajla
//		Na svaki red se primeni lambda funkcija koja pretrazuje po ovom regexu napisan u Pattern.compile()
//		Taj regex deli red na tri dela: sve do prve [, [ i sve sto je u [], ] i ono sto ostane posle ]
//		Posle se uzima onaj treci deo koji je ostatak i ponovo se poziva isti regex koji sad 
//		deli taj ostatak na tri dela i sve tako u petlji dok se ne prodje ceo red
		
		stringStream.forEach( line -> {
			Pattern pattern = Pattern.compile("([^\\[]*)([^\\]]*)(.*)");
			Matcher matcher = pattern.matcher(line);
			
			if (matcher.matches()) {
				//sve sto nije u [] mogu biti cetvrtine kao i cetvrtinske i osminske pauze
				String notesAndPauses = matcher.group(1);
				if (notesAndPauses.length() > 0) {
					parseNotesAndPauses(notesAndPauses);
				}
				//sve sto je u [] moze biti ili akord ili grupa osmina
				String chordsAndEights = matcher.group(2);
				String leftover = matcher.group(3);
				//ako ima neki string koji je u [] onda
				//u stringovima ima [ i ] pa one treba da se izbace
				if (chordsAndEights.length() > 0) {
					//preskoci [, substring vraca string od karaktera 1 pa do kraja
					chordsAndEights = chordsAndEights.substring(1);
					//preskoci ]
					leftover = leftover.substring(1);
				}
				
				if (chordsAndEights.length() > 0) {
					parseChordsAndEights(chordsAndEights);
				}
				
				while (leftover.length() > 0) {
					String newLine = leftover;
					pattern = Pattern.compile("([^\\[]*)([^\\]]*)(.*)");
					matcher = pattern.matcher(newLine);
					if (matcher.matches()) {
						notesAndPauses = matcher.group(1);
						if (notesAndPauses.length() > 0) {
							parseNotesAndPauses(notesAndPauses);
						}
						chordsAndEights = matcher.group(2);
						leftover = matcher.group(3);
						if (chordsAndEights.length() > 0) {
							chordsAndEights = chordsAndEights.substring(1);
							leftover = leftover.substring(1);
						}
						if (chordsAndEights.length() > 0) {
							parseChordsAndEights(chordsAndEights);
						}
					}
				}
			}
		});
		try {
			reader.close();
		} catch (IOException e) {
			System.err.println("Greska pri zatvaranju readera");
		}	
	}
	
//	Nije potrebna vec je sluzila za testiranje, da vidim sta se ucitalo iz fajla
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(MusicSymbol ms : symbols) {
			sb.append(ms.toString());
		}
		return sb.toString();
	}
}
