package piano;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NoteMaps {
	public static Map<Character, String> CharacterToString = new HashMap<>();
	public static Map<String, Integer> StringToInteger = new HashMap<>();
	public static Map<String, Character> StringToCharacter = new HashMap<>();
	public static Map<Integer, String> IntegerToString = new HashMap<>();
	
//	Ovo je klasa koja ucita map.csv koji sadrzi mapiranje nota u midi kodove i u visine
//	Svima je ovo bilo u klasi kompozicija, ali gotovo sve druge klase koriste ove gore hes mape
//	i onda posle mora da se neka kompozicija prosledi nekom objektu da bi on imao pristup ovim mapama
//	Pametnije je da se to ucita jednom u statickom inicijalizacionom bloku
//	Prvi put kad se klasa NoteMaps pojavi negde u kodu ona ce se ucitati u memoriju i izvrsice se
//	staticki inicijalizacioni blok i samo ce se jednom citati fajl
//	Svim ostalim klasama su uvek dostupne mape, i onda ne mora svaki objekat da ima referencu
//	na neku kompoziciju da bi mogao da pristupa mapama
	static {
		try {
			File csvMap = new File ("map.csv");
			BufferedReader reader = new BufferedReader(new FileReader(csvMap));
			Stream<String> stringStream = reader.lines();
			
//			stringStream sadrzi niz/listu stringova koji predstavljaju redove fajla
//			forEach prima lambda funkciju koju poziva za svaki element iz liste stringStream
//			Ova lambda funkcija regexom podeli red na 3 dela koji predstavljaju
//			slovo na tastaturi kojim se svira taj ton, visinu tona, midi kod tona
			stringStream.forEach(str -> {
				Pattern pattern;
				Matcher matcher;
				
				pattern = Pattern.compile("([^,]*),([^,]*),([^\n]*)");
				matcher = pattern.matcher(str);
				
				if(matcher.matches()) {
					String str1 = matcher.group(1);
					String str2 = matcher.group(2);
					String str3 = matcher.group(3);
					
					CharacterToString.put(str1.charAt(0), str2);
					StringToCharacter.put(str2, str1.charAt(0));
					StringToInteger.put(str2, Integer.parseInt(str3));
					IntegerToString.put(Integer.parseInt(str3), str2);
				}
			});
			reader.close();
		}catch (IOException e) {
			System.err.println("Nema Fajla");
		}
	}
}
