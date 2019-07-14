package piano;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public interface Formatter {
	void export(String fileName, Composition composition) throws FileNotFoundException, UnsupportedEncodingException;
}
