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
	
	static {
		try {
			File csvMap = new File ("map.csv");
			BufferedReader reader = new BufferedReader(new FileReader(csvMap));
			Stream<String> stringStream = reader.lines();
			
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
