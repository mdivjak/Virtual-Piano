package piano;

abstract public class MusicSymbol {
	protected double duration;
//	mogla je da se napravi klasa Duration,
//	ali posto su sve note ili osmine ili cetvrtine ovako mi je bilo lakse
//	(a i da bude malo razlicito od Barjaka, Anje i Vukasina)
	public static final double EIGHT = 1.0 / 8;
	public static final double FOURTH = 1.0 / 4;
	
	public MusicSymbol(double duration) {
		super();
		this.duration = duration;
	}

	public double getDuration() {
		return duration;
	}
	
	abstract public String toString();
	abstract public void startPlaying();
	abstract public void stopPlaying();
//	Klasa klavir ima metodu koja boji jednu dirku koja je pritisnuta
//	Nota, akord pauza u metodama pressOnPiano() i releaseOnPiano()
//	pozivaju ovu metodu odgovarajuci broj puta
//	pauza - nijednom, nota - jednom, akord - vise puta
//	Ove metode mogu da budu u klasi Piano, ali ovako je vise u duhu OOP :)
	abstract public void pressOnPiano(Piano piano);
	abstract public void releaseOnPiano(Piano piano);
}
