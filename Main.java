package piano;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class Main extends Frame {
	private VisualComposition visualComposition;
	private Piano piano;
	
	private TextField loadFileName;
	private Button loadFileButton;
	private Button play, pause, stop;
	private Checkbox notes, letters;
	private Checkbox keyHelp;
	
	private TextField exportFileName;
	private Button exportFileButton;
	private Button startRecording, stopRecording;
	private Checkbox midi, text;
	
	private static final Color PRIMARY_COLOR = new Color(255, 193, 7);
	private static final Color SECONDARY_COLOR = new Color(86, 86, 86);
	
	public Main() {
		super("Piano Player");
		setSize(1420, 850);
		
		addComponents();
		addListeners();
		
		setForeground(PRIMARY_COLOR);
		setBackground(SECONDARY_COLOR);
		paintButtons();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		setVisible(true);
	}
	
	private void addListeners() {
		loadFileButton.addActionListener(e -> {
			try {
				visualComposition.loadFromFile(loadFileName.getText());
			} catch (FileNotFoundException e1) {
				loadFileName.setText("ERROR: Failed loading file");
			}
		});
		
		play.addActionListener(e -> { visualComposition.play(); });
		pause.addActionListener(e -> { visualComposition.pause(); });
		stop.addActionListener(e -> { visualComposition.stop(); });
		
		notes.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				visualComposition.showNotes();
		});
		
		letters.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				visualComposition.showLetters();
		});
		
		keyHelp.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				piano.setShowHelp(true);
			else piano.setShowHelp(false);
		});
		
		startRecording.addActionListener(e -> {
			piano.startRecording();
			startRecording.setEnabled(false);
			stopRecording.setEnabled(true);
		});
		stopRecording.addActionListener(e -> {
			piano.stopRecording();
			stopRecording.setEnabled(false);
			startRecording.setEnabled(true);
		});
		stopRecording.setEnabled(false);
		
		exportFileButton.addActionListener(e -> {
			String fileName = exportFileName.getText();
			Composition composition = piano.getRecordedComposition();
			if(composition == null) return;
			boolean textFormat = text.getState();
			Formatter formatter = textFormat ? new TextFormatter() : new MidiFormatter();
			try {
				formatter.export(fileName, composition);
				startRecording.setEnabled(true);
				stopRecording.setEnabled(false);
			} catch (FileNotFoundException e1) {
				exportFileName.setText("ERROR: Failed creating file");
			} catch (UnsupportedEncodingException e1) {
				exportFileName.setText("ERROR: Failed exporting file");
			}
		});
	}
	
	private void paintButtons() {
		loadFileName.setForeground(SECONDARY_COLOR);
		exportFileName.setForeground(SECONDARY_COLOR);
		
		loadFileButton.setBackground(PRIMARY_COLOR);
		exportFileButton.setBackground(PRIMARY_COLOR);
		play.setBackground(PRIMARY_COLOR);
		pause.setBackground(PRIMARY_COLOR);
		stop.setBackground(PRIMARY_COLOR);
		startRecording.setBackground(PRIMARY_COLOR);
		stopRecording.setBackground(PRIMARY_COLOR);
		
		loadFileButton.setForeground(SECONDARY_COLOR);
		exportFileButton.setForeground(SECONDARY_COLOR);
		play.setForeground(SECONDARY_COLOR);
		pause.setForeground(SECONDARY_COLOR);
		stop.setForeground(SECONDARY_COLOR);
		startRecording.setForeground(SECONDARY_COLOR);
		stopRecording.setForeground(SECONDARY_COLOR);
		
	}
	
	private void addComponents() {
//		Program se sastoji od panela fullWindow koji ima 3 reda
//			U prvom redu je kontrolna tabla koja ima dve kolone tj Grid 1x2
//				U levom polju se nalazi panel od tri reda
//					Prvi red ima labelu, textfield za unos putanje do fajla za citanje i dugme za ucitavanje
//					Drugi red ima checkboxove kojima se bira kako se ispisuju note kompozicije
//					Treci red ima 3 dugmeta za pustanje kompozicije
//				Desni deo kontrolne table ima isto tri reda
//					Prvi red ima labelu, textfield za putanje do fajla u koji se eksportuje
//					Drugi red ima checkboxove kojima se bira u kom formatu se eksportuje
//					Treci red ima 2 dugmeta za snimanje kompozicije
//			Drugi red sadrzi vizuelni prikaz kompozicije
//			Treci red sadrzi klavir
		Panel fullWindow = new Panel(new GridLayout(3, 1));
		
		Panel controlPanel = new Panel(new GridLayout(1, 2));
		
		Panel leftControlPanel = new Panel(new GridLayout(3, 1));
		
		Panel loadFilePanel = new Panel();
		loadFilePanel.add(new Label("Composition filepath:"));
		loadFilePanel.add(loadFileName = new TextField("jingle_bells.txt", 50));
		loadFilePanel.add(loadFileButton = new Button("Load"));
		
		leftControlPanel.add(loadFilePanel);
		
		Panel checkboxes = new Panel();
		CheckboxGroup cb = new CheckboxGroup();
		checkboxes.add(notes = new Checkbox("Notes", true, cb));
		checkboxes.add(letters = new Checkbox("Letters", false, cb));
		checkboxes.add(keyHelp = new Checkbox("Print help on piano keys"));
		
		leftControlPanel.add(checkboxes);
		
		Panel playerButtons = new Panel();
		
		playerButtons.add(play = new Button("Play"));
		playerButtons.add(pause = new Button("Pause"));
		playerButtons.add(stop = new Button("Stop"));
		
		leftControlPanel.add(playerButtons);
		
		controlPanel.add(leftControlPanel);
		
		Panel rightControlPanel = new Panel(new GridLayout(3, 1));
		
		Panel exportFilePanel = new Panel();
		exportFilePanel.add(new Label("Export file filepath:"));
		exportFilePanel.add(exportFileName = new TextField("export.txt", 50));
		exportFilePanel.add(exportFileButton = new Button("Export"));
		
		rightControlPanel.add(exportFilePanel);
		
		checkboxes = new Panel();
		cb = new CheckboxGroup();
		checkboxes.add(text = new Checkbox("Text", true, cb));
		checkboxes.add(midi = new Checkbox("Midi", false, cb));
		
		rightControlPanel.add(checkboxes);
		
		Panel recordingPanel = new Panel();
		recordingPanel.add(startRecording = new Button("Start recording"));
		recordingPanel.add(stopRecording = new Button("Stop recording"));

		rightControlPanel.add(recordingPanel);
		
		controlPanel.add(rightControlPanel);
		
		fullWindow.add(controlPanel);
		
		piano = new Piano();
		fullWindow.add(visualComposition = new VisualComposition(piano));
		fullWindow.add(piano);

		add(fullWindow);
	}
	
	public static void main(String[] args) {
		new Main();
	}
}
